package net.channel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 
 * @author Alex
 *
 */
public interface IChannel {
	public void writeBytes(byte[] data) throws IOException;
	public byte[] readBytes() throws IOException;
}
