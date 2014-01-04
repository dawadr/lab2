package net.channel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Creates an IObjectChannel.
 * @author Alex
 *
 */
public interface IObjectChannelFactory {
	/**
	 * 
	 * @param out Der OutputStream, der zum Schreiben der Daten verwendet wird.
	 * @param in Der InputStream, der zum Lesen der Daten verwendet wird.
	 * @return ein IObjectChannel der auf den uebergebenen Streams operiert
	 * @throws IOException
	 */
	public IObjectChannel create(OutputStream out, InputStream in) throws IOException;
}
