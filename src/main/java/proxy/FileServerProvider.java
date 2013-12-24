package proxy;

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import server.IFileServer;
import util.RequestMapper;
import message.Request;
import message.Response;
import message.request.UploadRequest;

public class FileServerProvider {

	private Queue<FileServerAdapter> q; // contains the FileServerAdapters in order of their usage
	private FileServerAdapter designated;
	private List<FileServerAdapter> readQuorum;
	private List<FileServerAdapter> writeQuorum;

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
		// try each fileserver till success
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
	
	
	private void determineQuorums() {
		if (writeQuorum != null) return;
		// Gifford's scheme
		int n = q.size();
		int nw = (n / 2) + 1;	
		int nr = (n / 2);	
		// return nw fileservers with least usage
		ArrayList<FileServerAdapter> read = new ArrayList<FileServerAdapter>();
		ArrayList<FileServerAdapter> write = new ArrayList<FileServerAdapter>();
		while (write.size() < nw) {
			FileServerAdapter elem = q.poll();
			write.add(elem);
			if (read.size() < nr) {
				read.add(elem);
			}
		}
		writeQuorum = write;
		readQuorum = read;
	}
	
	public List<FileServerAdapter> getWriteQuorum() {
		determineQuorums();
		return writeQuorum;	
	}
	
	public List<FileServerAdapter> getReadQuorum() {
		determineQuorums();
		return readQuorum;	
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
