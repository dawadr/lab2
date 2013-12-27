package server;

import java.io.IOException;
import java.util.HashSet;

import util.FileManager;
import util.RequestMapper;
import net.ILogAdapter;
import net.IServerConnectionHandler;
import message.Request;
import message.Response;
import message.request.DownloadFileRequest;
import message.request.InfoRequest;
import message.request.UploadRequest;
import message.request.VersionRequest;
import message.response.FailedResponse;
import message.response.InfoResponse;
import message.response.ListResponse;
import message.response.MessageResponse;
import message.response.VersionResponse;
import message.response.DownloadFileResponse;
import model.DownloadTicket;

/**
 * Handles incoming Requests.
 * @author Alex
 *
 */
public class FileServerHandler implements IServerConnectionHandler, IFileServer {

	private ILogAdapter log;
	private FileManager fileManager;
	private RequestMapper mapper;

	public FileServerHandler(FileManager fileManager) {
		this.fileManager = fileManager;
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
				log(e.getClass().getSimpleName() + ": "+ e.getMessage());
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

	}

	@Override
	public void setLogAdapter(ILogAdapter log) {
		this.log = log;
	}

	@Override
	public boolean breakConnection() {
		return false;
	}

	private void log(String msg) {
		if (log != null) log.log(msg);
	}


	@Override
	public Response list() throws IOException {
		log("List requested");
		return new ListResponse(new HashSet<String>(fileManager.getFileList()));
	}

	@Override
	public Response download(DownloadFileRequest request)
			throws IOException {
		DownloadTicket ticket = request.getTicket();
		String filename = ticket.getFilename();
		long size = fileManager.getSize(filename);
		int version = fileManager.getVersion(filename);
		String checksum = util.ChecksumUtils.generateChecksum(ticket.getUsername(), filename, version, size);
		// Verify ticket
		if (!checksum.equals(ticket.getChecksum())) {
			log("Download failed - invalid ticket");
			return new FailedResponse("Invalid download ticket.");
		}
		// Return file
		byte[] content = fileManager.readFile(filename);
		DownloadFileResponse r = new DownloadFileResponse(ticket, content);
		log("Downloaded " + ticket.toString());
		return r;
	}

	@Override
	public Response info(InfoRequest request) throws IOException {
		log("Info requested");
		long size =	fileManager.getSize(request.getFilename());
		return new InfoResponse(request.getFilename(), size);
	}

	@Override
	public Response version(VersionRequest request) throws IOException {
		log("Version requested");
		int version = fileManager.getVersion(request.getFilename());
		return new VersionResponse(request.getFilename(), version);
	}

	@Override
	public MessageResponse upload(UploadRequest request) throws IOException {
		log("Uploading " + request.toString());
		fileManager.writeFile(request.getFilename(), request.getVersion(), request.getContent());
		return new MessageResponse("'" + request.getFilename() + "' with Version " + request.getVersion() + " successfully uploaded.");
	}

}