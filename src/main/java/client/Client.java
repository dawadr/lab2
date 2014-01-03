package client;

import java.io.IOException;
import java.net.InetAddress;
import java.security.PublicKey;

import net.IConnection;
import net.TcpConnection;
import proxy.IProxy;
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
		PublicKey publicKey;
		try {
			publicKey = keyProvider.getPublicKey("proxy.pub");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		proxy = new ProxyAdapter(config.getString("proxy.host"), config.getInt("proxy.tcp.port"), keyProvider, publicKey);
		this.shell.run();
	}

	public IClientCli getCli() {
		return cli;
	}


	private class ClientCli implements IClientCli {

		@Override
		@Command
		public LoginResponse login(String username, String password) throws IOException {
			LoginRequest req = new LoginRequest(username, password);
			LoginResponse r = proxy.login(req);	
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
	}

}