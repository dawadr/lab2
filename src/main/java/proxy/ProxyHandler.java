package proxy;

import java.io.IOException;
import java.net.InetAddress;

import server.IFileServer;
import util.RequestMapper;
import net.ILogAdapter;
import net.IServerConnectionHandler;
import message.Request;
import message.Response;
import message.request.BuyRequest;
import message.request.DownloadTicketRequest;
import message.request.InfoRequest;
import message.request.ListRequest;
import message.request.LoginRequest;
import message.request.UploadRequest;
import message.request.VersionRequest;
import message.response.BuyResponse;
import message.response.CreditsResponse;
import message.response.DownloadTicketResponse;
import message.response.FailedResponse;
import message.response.InfoResponse;
import message.response.LoginResponse;
import message.response.MessageResponse;
import message.response.RefuseResponse;
import message.response.LoginResponse.Type;
import message.response.VersionResponse;
import model.DownloadTicket;
import model.User;

/**
 * Handles incoming Requests by clients.
 * @author Alex
 *
 */
public class ProxyHandler implements IServerConnectionHandler, IProxy {

	private Uac uac;
	private User user;
	private FileServerManager serverManager;
	private ILogAdapter log;
	private boolean loggedIn = false;
	private RequestMapper mapper;

	public ProxyHandler(Uac uac, FileServerManager serverManager) {
		this.uac = uac;
		this.serverManager = serverManager;
		this.mapper = new RequestMapper(this);
	}


	@Override
	public Object process(Object inputObject) {
		Response outputObject;  
		if (inputObject instanceof Request) {
			try {
				outputObject = mapper.invoke((Request)inputObject);
			} catch (UnsupportedOperationException e) {
				outputObject = new FailedResponse("Request not supported.");
			} catch (IOException e) {
				outputObject = new FailedResponse(e);
				log(e.getClass().getSimpleName() + ": " + e.getMessage());
				// e.printStackTrace();
			}
		} else {
			outputObject = new FailedResponse("Illegal Request.");
		}
		return outputObject;
	}

	@Override
	public Object getIllegalRequestResponse() {
		return new MessageResponse("Illegal Request.");
	}

	@Override
	public void close() {
		this.loggedIn = false;
		if (this.user != null) uac.logout(user, this);
		this.user = null;
	}

	@Override
	public void setLogAdapter(ILogAdapter log) {
		this.log = log;
	}

	@Override
	public boolean breakConnection() {
		return (!loggedIn);
	}

	private void initializeSession(User user) {
		this.user = user;
		this.loggedIn = true;
	}

	private void log(String msg) {
		if (log != null) {
			if (this.user != null) log.log("[" + this.user.getName() + "] " + msg);
			else log.log(msg);
		}
	}


	@Override
	public LoginResponse login(LoginRequest request) throws IOException {
		try {
			User u = uac.login(request.getUsername(), request.getPassword(), this);
			if (u == user) return new LoginResponse(Type.SUCCESS);
			User u2 = user;
			close();
			initializeSession(u);
			if (u2 != null) log(u2.getName() + " logged out.");
			log(user.getName() + " logged in.");
			return new LoginResponse(Type.SUCCESS);
		} catch (UacException e) {
			return new LoginResponse(Type.WRONG_CREDENTIALS);
		}
	}

	@Override
	public Response credits() throws IOException {
		if (!loggedIn) return new RefuseResponse();
		log("Credits requested");
		return new CreditsResponse(user.getCredits());
	}

	@Override
	public Response buy(BuyRequest credits) throws IOException {
		if (!loggedIn) return new RefuseResponse();
		log("Buy requested: " + credits);
		user.setCredits(user.getCredits() + credits.getCredits());
		return new BuyResponse(user.getCredits());
	}

	@Override
	public Response list() throws IOException {
		if (!loggedIn) return new RefuseResponse();	
		log("List requested");
		FileServerProvider provider = serverManager.getServerProvider();
		Response r = provider.processLeastUsed(new ListRequest());
		provider.getLeastUsed().getConnection().close();
		return r;
	}

	@Override
	public Response download(DownloadTicketRequest request) throws IOException {
		if (!loggedIn) return new RefuseResponse();
		log("DownloadTicket requested: '" + request.getFilename() + "'");
		FileServerProvider provider = serverManager.getServerProvider();
		String filename = request.getFilename();
		long size;
		int version;
		InfoResponse iResp;
		VersionResponse vResp;
		InetAddress address;
		int port;
		String checksum;

		FileServerAdapter usedAdapter = null;
		Response r1;
		Response r2;
		// Request size and version from fileserver
		try {
			r1 = provider.processLeastUsed(new InfoRequest(filename));
			if (r1 instanceof MessageResponse) throw new IOException(((FailedResponse)r1).getMessage());
			r2 = provider.processLeastUsed(new VersionRequest(filename));
			if (r2 instanceof MessageResponse) throw new IOException(((FailedResponse)r2).getMessage());
		} catch (IOException e) {
			throw e;
		} finally {
			try {
				usedAdapter = provider.getLeastUsed();
				usedAdapter.getConnection().close();
			} catch (IllegalStateException e){ }
		}

		if (r1 instanceof InfoResponse && r2 instanceof VersionResponse) {
			iResp = (InfoResponse)r1;
			vResp = (VersionResponse)r2;
		} else {
			throw new IOException("Illegal response by fileserver.");
		}

		// check user credits
		size = iResp.getSize();
		if (uac.checkAndChargeCredits(user, size) == false) return new FailedResponse("Not enough credits. Credits needed: " + size + "\nYou have " + user.getCredits() + " credits left.");
		log("Charged of " + size + " credits");

		// create ticket
		version = vResp.getVersion();
		address = provider.getLeastUsed().getConnection().getHost();
		port = provider.getLeastUsed().getConnection().getPort();
		checksum = util.ChecksumUtils.generateChecksum(user.getName(), filename, version, size);
		DownloadTicket ticket = new DownloadTicket(user.getName(), filename, checksum, address, port);
		log("Responding with download ticket: " + ticket.toString());

		// increase usage of fileserver
		serverManager.increaseUsage(usedAdapter.getFileServer(), size);
		return new DownloadTicketResponse(ticket);
	}

	@Override
	public MessageResponse upload(UploadRequest request) throws IOException {
		if (!loggedIn) return new RefuseResponse();
		long size = request.getContent().length;
		log("Uploading '" + request.getFilename() + "' with size " + size);
		// forward file to all servers
		FileServerProvider provider = serverManager.getServerProvider();
		provider.sendAll(request);
		// update user's credits
		uac.increaseCredits(user, size * 2);
		log("Earned " + size * 2 + " credits");
		return new MessageResponse("File successfully uploaded.\nYou now have " + user.getCredits() + " credits.");
	}

	@Override
	public MessageResponse logout() throws IOException {
		if (!loggedIn) return new RefuseResponse();
		User u = user;
		close();
		log(u.getName() + " logged out.");
		return new RefuseResponse("Successfully logged out.");
	}

}