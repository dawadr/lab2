package proxy;

import java.io.IOException;

import net.IConnection;
import net.ILogAdapter;
import message.Response;
import message.request.DownloadFileRequest;
import message.request.InfoRequest;
import message.request.ListRequest;
import message.request.UploadRequest;
import message.request.VersionRequest;
import message.response.MessageResponse;
import model.FileServer;
import server.IFileServer;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Provides methods processed by a remote fileserver.
 * @author Alex
 *
 */
public class FileServerAdapter implements IFileServer {

	private IConnection conn;
	private ILogAdapter log;
	private FileServer fileServer;

	public FileServerAdapter(IConnection connection, FileServer fileServer, ILogAdapter log) {
		this.conn = connection;
		this.log = log;
		this.fileServer = fileServer;
	}

	public IConnection getConnection() {
		return conn;
	}

	public FileServer getFileServer() {
		return fileServer;
	}

	@Override
	public Response list() throws IOException {
		Response r;
		try {
			r = conn.send(new ListRequest());
			log("List response: " + r.toString());
		} catch (IOException e) {
			log("List failed");
			throw e;
		}
		return r;	
	}

	@Override
	public Response download(DownloadFileRequest request) throws IOException {
		throw new NotImplementedException();
	}

	@Override
	public Response info(InfoRequest request) throws IOException {
		Response r;
		try {
			r = conn.send(request);
			log("Info response: " + r.toString());
		} catch (IOException e) {
			log("Info failed");
			throw e;
		}
		return r;	
	}

	@Override
	public Response version(VersionRequest request) throws IOException {
		Response r;
		try {
			r = conn.send(request);
			log("Version response: " + r.toString());
		} catch (IOException e) {
			log("Version failed");
			throw e;
		}
		return r;	
	}

	@Override
	public MessageResponse upload(UploadRequest request) throws IOException {
		Response r;
		try {
			r = conn.send(request);
			log("Upload response: " + r.toString());
		} catch (IOException e) {
			log("Upload failed");
			throw e;
		}
		if (!(r instanceof MessageResponse)) throw new IOException("Illegal response by fileserver.");
		return (MessageResponse)r;	
	}

	private void log(String message) {
		if (log != null) log.log("[FileServerAdapter " + conn.getHost() + " Port " + conn.getPort() +  "] " + message);
	}

	@Override
	public String toString() {
		return "[FileServerAdapter " + conn.getHost() + " Port " + conn.getPort() +  "]";
	}
}
