package proxy;

import java.io.IOException;
import java.net.ConnectException;
import java.util.Queue;

import util.RequestMapper;
import message.Request;
import message.Response;
import message.request.UploadRequest;

public class FileServerProvider {

	private Queue<FileServerAdapter> q;
	private FileServerAdapter designated;

	public FileServerProvider(Queue<FileServerAdapter> q) {
		this.q = q;
	}

	/**
	 * If not done yet, determines the least used fileserver, and sends the request. Does not close the FileServerAdapter's connection afterwards.
	 * @param request
	 * @return
	 * @throws UnsupportedOperationException
	 * @throws IOException
	 */
	public Response processLeastUsed(Request request) throws UnsupportedOperationException, IOException {
		if (designated != null) return new RequestMapper(designated).invoke(request);

		boolean success = false;
		FileServerAdapter a = null;
		Response r = null;
		// try each fileserver until success
		while(!success) {
			a = q.poll();
			if (a == null) throw new IOException("No fileservers connected."); // q empty
			try {
				synchronized (a) {
					r = new RequestMapper(a).invoke(request);
				}
				success = true;		
				designated = a;
			} catch (ConnectException e) {} // Server not online
		}
		return r;
	}

	/**
	 * Sends the upload request to all available fileservers and closes each FileServerAdapter's connection afterwards.
	 * @param request
	 * @throws IOException
	 */
	public void sendAll(UploadRequest request) throws IOException {
		boolean success = false;
		// try each fileserver
		for (FileServerAdapter a : q) {
			try {
				synchronized (a) {
					new RequestMapper(a).invoke(request);
					success = true;		
					a.getConnection().close();
				}	
			} catch (ConnectException e) {} // Server not online
		}		
		if (!success) throw new IOException("No fileservers connected.");
	}

	/**
	 * Returns the designated least used fileserver. Requires processLeastUsed to be successfully called before.
	 * @return
	 */
	public FileServerAdapter getLeastUsed() {
		if (designated == null) throw new IllegalStateException("Least used fileserver not designated yet.");
		return designated;
	}

}
