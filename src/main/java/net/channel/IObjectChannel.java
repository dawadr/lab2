package net.channel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.ILogAdapter;

/**
 * Channel mit Methoden um Objekte zu senden/empfangen
 * @author Alex
 *
 */
public interface IObjectChannel {
	public void writeObject(Object o) throws IOException;
	public Object readObject() throws IOException, ClassNotFoundException;
	public void setLogAdapter(ILogAdapter log);
}
