package net.channel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * 
 * @author Alex
 *
 */
public class ByteChannel implements IChannel {

	private PrintWriter out;
	private BufferedReader in;

	public ByteChannel(OutputStream out, InputStream in) {
		this.out = new PrintWriter(out, true);
		this.in  = new BufferedReader(new InputStreamReader(in));
	}


	@Override
	public void writeBytes(byte[] data) throws IOException {
		out.println(new String(data));
		//out.writeObject(o);
	}

	@Override
	public byte[] readBytes() throws IOException {
		return in.readLine().getBytes();
		//return in.readObject();
	}

}
