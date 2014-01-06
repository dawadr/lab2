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
import proxy.IProxy;
import proxy.ManagementService;
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

	private Config config;
	private Shell shell;
	private IClientCli cli;
	private IProxy proxy;
	private FileManager fileManager;
	private KeyProvider keyProvider;
	private ManagementService managementService;
	private INotifyCallback notifyCallback;

	public static void main(String... args) {
		String config = "client";
		if (args.length > 0) config = args[0];
		new Client(new Config(config), new Shell(config, System.out, System.in)).run();
	}

	public Client(Config config, Shell shell) {	
		this.config = config;
		this.shell = shell;
		this.cli = new ClientCli();
		this.shell.register(cli); 
	}

	@Override
	public void run() {
		fileManager = new FileManager(config.getString("download.dir"));
		keyProvider = new KeyProvider(config.getString("keys.dir"));
		PublicKey proxyPublicKey;
		try {
			proxyPublicKey = keyProvider.getPublicKey(config.getString("proxy.key"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		proxy = new ProxyAdapter(config.getString("proxy.host"), config.getInt("proxy.tcp.port"), keyProvider, proxyPublicKey);
		
		Config mc = new Config("mc");
		String host = mc.getString("proxy.host");
		String name = mc.getString("binding.name");
		int port = mc.getInt("proxy.rmi.port");
		
		try {
			//managementService = (ManagementService) Naming.lookup("rmi://" + host + "/" + name);
			Registry registry = LocateRegistry.getRegistry(host, port);
			this.managementService = (ManagementService) registry.lookup(name);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Init notifyCallback
		this.notifyCallback = new INotifyCallback() {
			
			private static final long serialVersionUID = -4994758755943921733L;
			
			@Override
			public void notify(Response r) {
				try {
					shell.writeLine(r.toString());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		
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
			IConnection fsCon = new TcpConnection(host.getHostAddress(), port, new FileserverChannelFactory());
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
				r = managementService.subscribe(filename, numberOfDownloads, Client.this.notifyCallback);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return r;
		}
	}

}