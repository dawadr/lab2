package net.channel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import net.ILogAdapter;

/**
 * Dieser Channel schreibt und liest Daten (diese werden dabei in ein DTO gewrappt)
 * @author Alex
 *
 */
public class DataChannel implements IChannel {

	private ObjectOutputStream out;
	private ObjectInputStream in;
	private ILogAdapter log;

	/**
	 * 
	 * @param out Der OutputStream, der zum Schreiben der Daten verwendet wird.
	 * @param in Der InputStream, der zum Lesen der Daten verwendet wird.
	 * @throws IOException
	 */
	public DataChannel(OutputStream out, InputStream in) throws IOException {
		this.out = new ObjectOutputStream(out);
		this.in  = new ObjectInputStream((in));
	}


	@Override
	public void writeBytes(byte[] data) throws IOException {
		out.writeObject(new DataMessage(data));;
	}

	@Override
	public byte[] readBytes() throws IOException {
		Object o;
		try {
			o = in.readObject();
		} catch (ClassNotFoundException e) {
			throw new IOException(e);
		}
		if (o == null) return null;
		DataMessage msg = null;
		if (o instanceof DataMessage) msg = (DataMessage)o;
		else throw new IOException("Receiving data failed");
		return msg.getData();
	}


	@Override
	public void setLogAdapter(ILogAdapter log) {
		this.log = log;
	}

	protected void log(String message) {
		if (log != null) log.log(message);
	}

}
