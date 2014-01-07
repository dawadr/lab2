package net.channel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.ILogAdapter;

/**
 * Ein Channel, ueber den Daten geschickt und gelesen werden
 * @author Alex
 *
 */
public interface IChannel {
	public void writeBytes(byte[] data) throws IOException;
	public byte[] readBytes() throws IOException;
	public void setLogAdapter(ILogAdapter log);
}
