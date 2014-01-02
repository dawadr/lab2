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

	public IObjectChannel create(OutputStream out, InputStream in) throws IOException;
	
}
