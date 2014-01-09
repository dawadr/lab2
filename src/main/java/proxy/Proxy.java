package proxy;

import cli.Command;
import cli.Shell;

import java.io.IOException;
import java.net.SocketException;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
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
import net.channel.ITamperedMessageOutput;
import net.channel.VerifiedObjectChannelFactory;
import message.Response;
import message.response.FileServerInfoResponse;
import message.response.MessageResponse;
import message.response.UserInfoResponse;
import util.Config;
import util.KeyProvider;

public class Proxy implements Runnable {

	private Config config;
	private UserConfig userConfig;
	private Config mcConfig;

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
		String mc = "mc";
		if (args.length > 2) {
			config = args[0];
			user = args[1];
			mc = args[2];
		}
		new Proxy(new Config(config), new UserConfig(user), new Config(mc), new Shell(config, System.out, System.in)).run();
	}

	public Proxy(Config config, UserConfig userConfig, Config mcConfig, Shell shell) {	
		this.config = config;
		this.userConfig = userConfig;
		this.mcConfig = mcConfig;
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
		threadPool = Executors.newCachedThreadPool();

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

		// Output fuer tampered messages
		ITamperedMessageOutput messageOutput = new ITamperedMessageOutput() {
			@Override
			public void write(String message) {
				try {
					shell.writeLine(message);
				} catch (IOException e) {
				}
			}	
		};

		// Start fileServerManager
		int checkPeriod = config.getInt("fileserver.checkPeriod");
		int timeout = config.getInt("fileserver.timeout");
		VerifiedObjectChannelFactory channelFactory = new VerifiedObjectChannelFactory(sharedSecretKey, true, 10, messageOutput);
		fileServerManager = new FileServerManager(datagramReceiver, checkPeriod, timeout, log, channelFactory);	
		fileServerManager.start();

		// Start managementServiceServer
		PublicKey proxyPublicKey;
		try {
			proxyPublicKey = keyProvider.getPublicKey("proxy");
		} catch (IOException e1) {
			e1.printStackTrace();
			return;
		}
		managementServiceServer = new ManagementServiceServer(uac, keyProvider, fileServerManager,
				mcConfig.getInt("proxy.rmi.port"), mcConfig.getString("binding.name"), proxyPublicKey);
		managementServiceServer.start();

		// Run server in own thread
		try {
			IServerConnectionHandlerFactory handlerFactory = new ProxyHandlerFactory(uac, fileServerManager);
			IObjectChannelFactory secureChannelFactory = new SecureClientChannelFactory(keyProvider, privateKey);
			IServerConnectionFactory connectionFactory = new TcpServerConnectionFactory(handlerFactory, secureChannelFactory);
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
