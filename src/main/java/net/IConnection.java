package net;

import java.io.IOException;
import java.net.InetAddress;

import message.Request;
import message.Response;

/**
 * Defines a connection to a host that can handle Requests.
 * @author Alex
 *
 */
public interface IConnection {
	
	/**
	 * Sends a Request to the host. (Re-)opens the connection automatically if necessary.
	 * @param request
	 * @return the Response by the host
	 * @throws IOException
	 */
	public Response send(Request request) throws IOException;	
	
	/**
	 * Closes the connection and releases all resources.
	 * @throws IOException
	 */
	public void close() throws IOException;
	
	public InetAddress getHost();
	
	public int getPort();
	
	public boolean isClosed();
	
}
