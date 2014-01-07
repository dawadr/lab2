package client;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.PublicKey;

import net.IConnection;
import net.ILogAdapter;
import net.TcpConnection;
import net.channel.ObjectChannelFactory;
import proxy.IProxy;
import proxy.mc.IManagementService;
import proxy.mc.INotifyCallback;
import message.Response;
import message.request.BuyRequest;
import message.request.DownloadFileRequest;
import message.request.DownloadTicketRequest;
import message.request.LoginRequest;
import message.request.UploadRequest;
import message.response.DownloadFileResponse;
import message.response.DownloadTicketResponse;
import message.response.LoginResponse;
import message.response.MessageResponse;
import model.DownloadTicket;
import cli.Command;
import cli.Shell;
import server.IFileServer;
import util.Config;
import util.FileManager;
import util.KeyProvider;
import util.RequestMapper;

public class Client implements Runnable {

	private Config clientConfig;
	private Config mcConfig;
	private Shell shell;
	private IClientCli cli;
	private IProxy proxy;
	private FileManager fileManager;
	private KeyProvider keyProvider;
	private IManagementService managementService;
	private INotifyCallback notifyCallback;

	public static void main(String... args) {
		String client = "client";
		String mc = "mc";
		if (args.length > 1) {
			client = args[0];
			mc = args[1];
		}
		new Client(new Config(client), new Config(mc), new Shell(client, System.out, System.in)).run();
	}

	public Client(Config clientConfig, Config mcConfig, Shell shell) {	
		this.clientConfig = clientConfig;
		this.mcConfig = mcConfig;
		this.shell = shell;
		this.cli = new ClientCli();
		this.shell.register(cli); 
	}

	@Override
	public void run() {
		fileManager = new FileManager(clientConfig.getString("download.dir"));
		keyProvider = new KeyProvider(clientConfig.getString("keys.dir"));
			
		// Init ProxyAdapter
		PublicKey proxyPublicKey;
		try {
			proxyPublicKey = KeyProvider.getPublicKey(clientConfig.getString("proxy.key"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		proxy = new ProxyAdapter(clientConfig.getString("proxy.host"), clientConfig.getInt("proxy.tcp.port"), keyProvider, proxyPublicKey);
			
		// Init managementService
		String host = mcConfig.getString("proxy.host");
		String name = mcConfig.getString("binding.name");
		int port = mcConfig.getInt("proxy.rmi.port");
		try {
			//managementService = (ManagementService) Naming.lookup("rmi://" + host + "/" + name);
			Registry registry = LocateRegistry.getRegistry(host, port);
			this.managementService = (IManagementService) registry.lookup(name);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		} catch (NotBoundException e) {
			throw new RuntimeException(e);
		}
		
		// Init notifyCallback
		try {
			this.notifyCallback = new NotifyCallbackImpl(shell);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
		
		this.shell.run();
	}

	public IClientCli getCli() {
		return cli;
	}


	private class ClientCli implements IClientCli {

		@Override
		@Command
		public Response login(String username, String password) throws IOException {
			LoginRequest req = new LoginRequest(username, password);
			Response r = proxy.login(req);	
			return r;
		}

		@Override
		@Command
		public Response credits() throws IOException {
			Response r = proxy.credits();	
			return r;
		}

		@Override
		@Command
		public Response buy(long credits) throws IOException {
			BuyRequest req = new BuyRequest(credits);
			Response r = proxy.buy(req);	
			return r;
		}

		@Override
		@Command
		public Response list() throws IOException {
			Response r = proxy.list();	
			return r;
		}

		@Override
		@Command
		public Response download(String filename) throws IOException {
			DownloadTicketResponse dtr;
			DownloadFileResponse dfr;

			// download ticket from proxy
			DownloadTicketRequest req = new DownloadTicketRequest(filename);
			Response r = proxy.download(req);	
			if (!(r instanceof DownloadTicketResponse)) return r;
			dtr = (DownloadTicketResponse)r;
			DownloadTicket ticket = dtr.getTicket();	

			// prepare download from fileserver
			InetAddress host = ticket.getAddress();
			int port = ticket.getPort();
			IConnection fsCon = new TcpConnection(host.getHostAddress(), port, new ObjectChannelFactory());
			IFileServer fsAdapter = new FileServerAdapter(fsCon);

			// download file
			try {			
				DownloadFileRequest dlReq = new DownloadFileRequest(ticket);
				Response r2 = new RequestMapper(fsAdapter).invoke(dlReq);
				if (!(r2 instanceof DownloadFileResponse)) return r; 
				dfr = (DownloadFileResponse)r2;	
				fileManager.writeFile(dfr.getTicket().getFilename(), 0, dfr.getContent());
				return dfr;
			} catch (IOException e) {
				fsCon.close();		
				throw e;
			} finally {
				fsCon.close();		
			}
		}

		@Override
		@Command
		public MessageResponse upload(String filename) throws IOException {
			UploadRequest req = new UploadRequest(filename, fileManager.getVersion(filename), fileManager.readFile(filename));
			MessageResponse r = proxy.upload(req);	
			return r;
		}

		@Override
		@Command
		public MessageResponse logout() throws IOException {
			MessageResponse r = proxy.logout();	
			return r;
		}

		@Override
		@Command
		public MessageResponse exit() throws IOException {
			try {
				logout();
			} catch (Exception e) {
			}
			shell.close();
			System.in.close();
			return new MessageResponse("Shutting down. Bye-bye");
		}
		
		@Command
		public Response topThreeDownloads() {		
			Response r = null;		
			try {
				r = managementService.getTopThree();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
			return r;
		}
		
		@Command
		public Response subscribe(String filename, int numberOfDownloads) {		
			Response r = null;
			try {
				r = managementService.subscribe(filename, numberOfDownloads, notifyCallback);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
			return r;
		}
	}

}