package net.channel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Erweitert IChannel um Methoden, um Objekte zu senden/empfangen
 * @author Alex
 *
 */
public interface IObjectChannel extends IChannel {
	public void writeObject(Object o) throws IOException;
	public Object readObject() throws IOException, ClassNotFoundException;
}
