package net.channel;


import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import net.ILogAdapter;

/**
 * Serialisiert/deserialisiert Objekte beim Lesen/Schreiben
 * @author Alex
 *
 */
public class ObjectChannel implements IObjectChannel {

	private ObjectOutputStream out;
	private ObjectInputStream in;
	private ILogAdapter log;

	/**
	 * 
	 * @param out Der OutputStream, der zum Schreiben der Daten verwendet wird.
	 * @param in Der InputStream, der zum Lesen der Daten verwendet wird.
	 * @throws IOException
	 */
	public ObjectChannel(OutputStream out, InputStream in) throws IOException {
		this.out = new ObjectOutputStream(out);
		this.in  = new ObjectInputStream((in));
	}

	@Override
	public void writeObject(Object o) throws IOException {
		out.writeObject(o);
	}

	@Override
	public Object readObject() throws IOException, ClassNotFoundException {
		return in.readObject();
	}

	@Override
	public void setLogAdapter(ILogAdapter log) {
		this.log = log;
	}

	protected void log(String message) {
		if (log != null) log.log(message);
	}

}
