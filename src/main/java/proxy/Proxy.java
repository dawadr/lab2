package proxy;

import cli.Command;
import cli.Shell;

import java.io.IOException;
import java.net.SocketException;
import java.security.Key;
import java.security.PrivateKey;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import proxy.mc.ManagementServiceServer;
import net.DatagramReceiver;
import net.IDatagramReceiver;
import net.ILogAdapter;
import net.IServer;
import net.IServerConnectionFactory;
import net.IServerConnectionHandlerFactory;
import net.TcpServer;
import net.TcpServerConnectionFactory;
import net.channel.IObjectChannelFactory;
import message.Response;
import message.response.FileServerInfoResponse;
import message.response.MessageResponse;
import message.response.UserInfoResponse;
import util.Config;
import util.KeyProvider;

public class Proxy implements Runnable {

	private Config config;
	private UserConfig userConfig;
	private Shell shell;
	private IProxyCli cli;
	private ExecutorService threadPool;
	private IServer server;
	private IDatagramReceiver datagramReceiver;
	private FileServerManager fileServerManager;
	private KeyProvider keyProvider;
	private Uac uac;
	private ManagementServiceServer managementServiceServer;

	public static void main(String... args) {
		String config = "proxy";
		String user = "user";
		if (args.length > 1) {
			config = args[0];
			user = args[1];
		}
		new Proxy(new Config(config), new UserConfig(user), new Shell(config, System.out, System.in)).run();
	}

	public Proxy(Config config, UserConfig userConfig, Shell shell) {	
		this.config = config;
		this.userConfig = userConfig;
		this.shell = shell;
		this.cli = new ProxyCli();
		this.shell.register(cli);
	}

	@Override
	public void run() {

		// Passwort fuer private key einlesen
		String privateKeyLocation = config.getString("key");
		PrivateKey privateKey = null;		
		try {
			shell.writeLine("Enter passphrase for private key:");
			while (privateKey == null) {
				String input = shell.readLine();	
				try {
					privateKey = KeyProvider.getPrivateKeyFrom(privateKeyLocation, input);
				} catch (IOException e) {
					shell.writeLine(e.getMessage());
					shell.writeLine("Enter passphrase for private key:");
				}
			}
		} catch (IOException e3) {
			e3.printStackTrace();
			return;
		}

		// Init thread pool
		// TODO nicht fixed size
		threadPool = Executors.newFixedThreadPool(50);

		// Init log
		ILogAdapter log = new ILogAdapter() {
			@Override
			public void log(String message) {
				try {
					shell.writeLine(message);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};

		// Init Uac
		try {
			this.uac = new Uac(userConfig.getUsers());
		} catch (IOException e2) {
			e2.printStackTrace();
			return;
		}

		// Init KeyProvider
		String keysDir = config.getString("keys.dir");
		keyProvider = new KeyProvider(keysDir);

		// Run datagramReceiver in own thread
		try {
			datagramReceiver = new DatagramReceiver(config.getInt("udp.port"));
			datagramReceiver.setLogAdapter(log);
			threadPool.execute(datagramReceiver);
		} catch (SocketException e1) {
			e1.printStackTrace();
			return;
		}

		// Shared secret key einlesen
		String hmacLocation = config.getString("hmac.key");
		Key sharedSecretKey;
		try {
			sharedSecretKey = KeyProvider.getSharedSecretKeyFrom(hmacLocation);
		} catch (IOException e1) {
			e1.printStackTrace();
			return;
		}

		// Start fileServerManager
		int checkPeriod = config.getInt("fileserver.checkPeriod");
		int timeout = config.getInt("fileserver.timeout");
		fileServerManager = new FileServerManager(datagramReceiver, sharedSecretKey, checkPeriod, timeout, log);	
		fileServerManager.start();

		// Start managementServiceServer
		managementServiceServer = new ManagementServiceServer(uac, keyProvider, fileServerManager);
		managementServiceServer.start();

		// Run server in own thread
		try {
			IServerConnectionHandlerFactory handlerFactory = new ProxyHandlerFactory(uac, fileServerManager);
			IObjectChannelFactory channelFactory = new SecureClientChannelFactory(keyProvider, privateKey);
			IServerConnectionFactory connectionFactory = new TcpServerConnectionFactory(handlerFactory, channelFactory);
			server = new TcpServer(config.getInt("tcp.port"), connectionFactory);
			server.setLogAdapter(log);
			threadPool.execute(server);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		// Run shell
		shell.run();
	}

	public IProxyCli getCli() {
		return cli;
	}

	private void shutdown() throws IOException {
		// Close & release all resources
		server.stop();
		datagramReceiver.close();
		fileServerManager.stop();
		managementServiceServer.stop();
		shell.close();
		System.in.close();
		// Shutdown threads
		threadPool.shutdown(); 
		try {			
			if (!threadPool.awaitTermination(5, TimeUnit.SECONDS)) {
				threadPool.shutdownNow(); 
				if (!threadPool.awaitTermination(5, TimeUnit.SECONDS))
					System.err.println("Pool did not terminate");
			}
		} catch (InterruptedException ie) {		
			threadPool.shutdownNow();
			Thread.currentThread().interrupt();
		}
	}


	private class ProxyCli implements IProxyCli {
		@Override
		@Command
		public Response fileservers() throws IOException {	
			FileServerInfoResponse r = new FileServerInfoResponse(fileServerManager.getServerList());
			return r;
		}

		@Override
		@Command
		public Response users() throws IOException {
			UserInfoResponse r = new UserInfoResponse(uac.getUserList());
			return r;
		}

		@Override
		@Command
		public MessageResponse exit() throws IOException {
			shutdown();
			return new MessageResponse("Shutting down. Bye-bye");
		}
	}

}
