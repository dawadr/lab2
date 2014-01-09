package server;

import cli.Command;
import cli.Shell;

import java.io.IOException;
import java.security.Key;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import net.DatagramSender;
import net.IDatagramSender;
import net.ILogAdapter;
import net.IServer;
import net.IServerConnectionFactory;
import net.IServerConnectionHandlerFactory;
import net.TcpServer;
import net.TcpServerConnectionFactory;
import net.channel.IObjectChannelFactory;
import net.channel.ITamperedMessageOutput;
import net.channel.VerifiedObjectChannelFactory;
import message.response.MessageResponse;
import util.Config;
import util.FileManager;
import util.KeyProvider;

public class FileServer implements Runnable {

	private Config config;
	private Shell shell;
	private IFileServerCli cli;
	private ExecutorService threadPool;
	private IServer server;
	private AliveSender aliveSender;
	private FileManager fileManager;
	private Key sharedSecretKey;


	public static void main(String... args) {
		String config = "fs1";
		if (args.length > 0) config = args[0];
		new FileServer(new Config(config), new Shell(config, System.out, System.in)).run();
	}

	public FileServer(Config config, Shell shell) {	
		this.config = config;
		this.shell = shell;
		this.cli = new FileServerCli();
		this.shell.register(cli);
	}

	@Override
	public void run() {
		// Init thread pool
		threadPool = Executors.newCachedThreadPool();

		// Init fileManager
		fileManager = new FileManager(config.getString("fileserver.dir"));
				
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

		// Shared secret key holen
		try {
			sharedSecretKey = KeyProvider.getSharedSecretKeyFrom(config.getString("hmac.key"));
		} catch (IOException e) {
			e.printStackTrace();
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
		
		// Run server in own thread
		try {
			IServerConnectionHandlerFactory handlerFactory = new FileServerHandlerFactory(fileManager);
			IObjectChannelFactory channelFactory = new VerifiedObjectChannelFactory(sharedSecretKey, false, 0, messageOutput);
			IServerConnectionFactory connectionFactory = new TcpServerConnectionFactory(handlerFactory, channelFactory);
			server = new TcpServer(config.getInt("tcp.port"), connectionFactory);
			server.setLogAdapter(log);
			threadPool.execute(server);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		// Start AliveSender
		try {
			IDatagramSender datagram = new DatagramSender(config.getString("proxy.host"), config.getInt("proxy.udp.port"));
			aliveSender = new AliveSender(datagram, config.getInt("fileserver.alive"), "!alive " + config.getInt("tcp.port"));
			aliveSender.setLogAdapter(log);
			aliveSender.activate();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		// Run shell
		shell.run();
	}

	public IFileServerCli getCli() {
		return cli;
	}
	
	private void shutdown() throws IOException {
		// Close & release all resources
		server.stop();
		aliveSender.deactivate();
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


	private class FileServerCli implements IFileServerCli {
		@Override
		@Command
		public MessageResponse exit() throws IOException {
			shutdown();
			return new MessageResponse("Shutting down. Bye-bye");
		}
	}

}