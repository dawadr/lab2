package proxy;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import util.RequestMapper;
import net.ILogAdapter;
import net.IServerConnectionHandler;
import message.Request;
import message.Response;
import message.request.BuyRequest;
import message.request.DownloadTicketRequest;
import message.request.InfoRequest;
import message.request.LoginRequest;
import message.request.UploadRequest;
import message.request.VersionRequest;
import message.response.BuyResponse;
import message.response.CreditsResponse;
import message.response.DownloadTicketResponse;
import message.response.FailedResponse;
import message.response.InfoResponse;
import message.response.ListResponse;
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
		Queue<FileServerAdapter> nr = provider.getReadQuorum();
		log("Nr fileservers: " + nr.toString());
		Set<String> filenames = new HashSet<String>();

		// alle fileserver durchgehen und filenames mergen
		for (FileServerAdapter fs: nr) {
			Response r = null;
			try {
				r = fs.list();
				if (r instanceof ListResponse) {
					ListResponse lResp = (ListResponse)r;	
					// addAll(): "Adds all of the elements in the specified collection to this set if they're not already present"
					filenames.addAll(lResp.getFileNames());
				} else {
					log("Unexpected Response from " + fs.toString() + ": " + r.toString());
				}
			} catch (IOException e) {
				log("ListRequest from " + fs.toString() + " failed: " + e.toString());
			} finally {
				try {
					fs.getConnection().close();
				} catch (IllegalStateException e){ }
			}
		}

		ListResponse r = new ListResponse(filenames);
		return r;
	}


	@Override
	public Response download(DownloadTicketRequest request) throws IOException {
		if (!loggedIn) return new RefuseResponse();
		log("DownloadTicket requested: '" + request.getFilename() + "'");
		FileServerProvider provider = serverManager.getServerProvider();
		String filename = request.getFilename();

		/**
		 * Server mit hoechster Version bestimmen
		 */
		Queue<FileServerAdapter> nr = provider.getReadQuorum();
		log("Nr fileservers: " + nr.toString());
		VersionRequest vReq = new VersionRequest(filename);
		int version = 0;
		Queue<FileServerAdapter> candidates = new ConcurrentLinkedQueue<FileServerAdapter>();		
		for (FileServerAdapter fs: nr) {
			Response r = null;
			try {
				r = fs.version(vReq);
				if (r instanceof VersionResponse) {
					VersionResponse vResp = (VersionResponse)r;		
					// wenn die version groesser ist als bisherige -> alle bisherigen verwerfen & version erh�hen
					if (vResp.getVersion() > version) {
						version = vResp.getVersion();
						candidates.clear();
						candidates.add(fs);
					}
					// wenn version gleich ist, fileserver zu kandidaten hinzu
					else if (vResp.getVersion() == version) {
						candidates.add(fs);
					}
				} else {
					log("Unexpected Response from " + fs.toString() + ": " + r.toString());
				}
			} catch (IOException e) {
				log("VersionRequest from " + fs.toString() + " failed: " + e.toString());
			} finally {
				try {
					fs.getConnection().close();
				} catch (IllegalStateException e){ }
			}
		}

		if (candidates.isEmpty()) {
			throw new IOException("No fileservers available.");
		}

		/**
		 * Server mit niedrigster Usage
		 */
		FileServerAdapter selectedServer = candidates.poll();
		log("Selected server for download: " + selectedServer.toString());

		/**
		 * Info requesten
		 */
		Response r;
		InfoResponse iResp;
		try {
			r = selectedServer.info(new InfoRequest(filename));
			if (r instanceof MessageResponse) throw new IOException(((FailedResponse)r).getMessage());
		} catch (IOException e) {
			throw e;
		} finally {
			try {
				selectedServer.getConnection().close();
			} catch (IllegalStateException e){ }
		}
		if (r instanceof InfoResponse) {
			iResp = (InfoResponse)r;
		} else {
			throw new IOException("Illegal response by fileserver.");
		}

		/**
		 * Credits checken
		 */
		long size = iResp.getSize();
		if (uac.checkAndChargeCredits(user, size) == false) return new FailedResponse("Not enough credits. Credits needed: " + size + "\nYou have " + user.getCredits() + " credits left.");
		log("Charged of " + size + " credits");

		/**
		 * Ticket erstellen
		 */
		InetAddress address = selectedServer.getConnection().getHost();
		int port = selectedServer.getConnection().getPort();
		String checksum = util.ChecksumUtils.generateChecksum(user.getName(), filename, version, size);
		DownloadTicket ticket = new DownloadTicket(user.getName(), filename, checksum, address, port);
		log("Responding with download ticket: " + ticket.toString());

		// increase usage of fileserver
		serverManager.increaseUsage(selectedServer.getFileServer(), size);
		
		// adjust download statistics
		DownloadStatistics.getInstance().reportDownload(filename);
		log("Statistics adjusted: " + DownloadStatistics.getInstance().getTopThree().toString());
		
		return new DownloadTicketResponse(ticket);
	}


	@Override
	public MessageResponse upload(UploadRequest request) throws IOException {
		if (!loggedIn) return new RefuseResponse();

		FileServerProvider provider = serverManager.getServerProvider();
		Queue<FileServerAdapter> nr = provider.getReadQuorum();
		Queue<FileServerAdapter> nw = provider.getWriteQuorum();
		log("Nr fileservers: " + nr.toString());
		log("Nw fileservers: " + nw.toString());

		// determine highest version from Nr fileservers
		VersionRequest vRequest = new VersionRequest(request.getFilename());
		int highestVersion = 0;
		for (FileServerAdapter fs: nr) {
			Response r;
			try {
				r = fs.version(vRequest);
				if (r instanceof VersionResponse) {
					VersionResponse vr = (VersionResponse) r;
					if (vr.getVersion() > highestVersion) highestVersion = vr.getVersion();
				} else {
					log("Unexpected Response from " + fs.toString() + ": " + r.toString());
				}
			} catch (IOException e) {
				log("VersionRequest from " + fs.toString() + " failed: " + e.toString());
			} finally {
				try {
					fs.getConnection().close();
				} catch (IllegalStateException e){ }
			}
		}

		// Upload file to Nw fileservers
		UploadRequest uRequest = new UploadRequest(request.getFilename(), highestVersion + 1, request.getContent());
		long size = request.getContent().length;
		log("Uploading '" + request.getFilename() + "' with size " + size + " to Write Quorum");	
		for (FileServerAdapter fs: nw) {
			try {
				fs.upload(uRequest);
				log("Uploaded '" + uRequest.getFilename() + "' to " + fs.toString());	
			} catch (IOException e) {
				throw new IOException("Upload to " + fs.toString() + " failed.");
			} finally {
				try {
					fs.getConnection().close();
				} catch (IllegalStateException e){ }
			}
		}

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