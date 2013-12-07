package net;

import java.util.List;

/**
 * Defines a Server that listens for incoming connections and handles each one in a separate IServerConnection.
 * @author Alex
 *
 */
public interface IServer extends Runnable {

	@Override
	public void run();

	/**
	 * Stops the server; closes all connections and threads and releases all resources.
	 */
	public void stop();
	
	public int getPort();
	
	public void setLogAdapter(ILogAdapter log);
	
	/**
	 * Returns the server's connections.
	 * @return
	 */
	public List<IServerConnection> getConnections();

}
