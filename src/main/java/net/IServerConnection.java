package net;

import java.net.Socket;

/**
 * Defines a connection of an IServer.
 * @author Alex
 *
 */
public interface IServerConnection extends Runnable {

	@Override
	public void run();

	/**
	 * Closes the connection and releases all resources.
	 */
	public void close();

	public Socket getSocket();
	
	public void setLogAdapter(ILogAdapter log);

	public boolean isClosed();
	
}
