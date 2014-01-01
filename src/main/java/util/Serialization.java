package util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Provides methods to serialize and deserialize objects.
 * @author Alex
 *
 */
public class Serialization {

	public static byte[] serialize(Object o) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(bos);
		out.writeObject(o);
		out.close();
		return bos.toByteArray();
	}
	
	public static Object deserialize(byte[] data) throws IOException, ClassNotFoundException  {
		ByteArrayInputStream bis = new ByteArrayInputStream(data);
		ObjectInputStream in = new ObjectInputStream(bis);
		Object oDecoded = in.readObject();
		return oDecoded;
	}
	
}
