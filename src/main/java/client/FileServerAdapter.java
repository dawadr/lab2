package client;

import java.io.IOException;

import net.IConnection;
import server.IFileServer;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import message.Response;
import message.request.*;
import message.response.MessageResponse;

/**
 * Provides methods processed by a remote fileserver.
 * @author Alex
 *
 */
public class FileServerAdapter implements IFileServer {

	private IConnection conn;
	public FileServerAdapter(IConnection connection) {
		this.conn = connection;
	}


	@Override
	public Response download(DownloadFileRequest request) throws IOException {
		Response r = conn.send(request);
		return r;
	}

	@Override
	public Response info(InfoRequest request) throws IOException {
		throw new NotImplementedException();
	}

	@Override
	public Response version(VersionRequest request) throws IOException {
		throw new NotImplementedException();
	}

	@Override
	public Response list() throws IOException {
		throw new NotImplementedException();
	}

	@Override
	public MessageResponse upload(UploadRequest request) throws IOException {
		throw new NotImplementedException();
	}

}
