package net;

import java.io.IOException;
import java.net.InetAddress;

/**
 * Sends data via the UDP protocol
 * @author Alex
 *
 */
public interface IDatagramSender {

	public void send(byte[] data) throws IOException;
	
	public void close() throws IOException;
	
	public InetAddress getHost();
	
	public int getPort();
	
	public boolean isClosed();
	
}
