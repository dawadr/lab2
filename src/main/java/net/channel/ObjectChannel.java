package net.channel;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

/**
 * 
 * @author Alex
 *
 */
public class ObjectChannel implements IChannel {
	
	private ObjectInputStream in;
	private ObjectOutputStream out;
	
	@Override
	public void initialize(OutputStream out, InputStream in) throws IOException {
		this.out = new ObjectOutputStream(out);
		this.in = new ObjectInputStream(in);
	}

	@Override
	public void writeObject(Object o) throws IOException {
		out.writeObject(o);
	}

	@Override
	public Object readObject() throws IOException, ClassNotFoundException {
		return in.readObject();
	}

}
