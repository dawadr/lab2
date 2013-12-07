package net;

import java.net.Socket;

/**
 * Creates an IServerConnection.
 * @author Alex
 *
 */
public interface IServerConnectionFactory {

	public IServerConnection create(Socket socket);
	
}
