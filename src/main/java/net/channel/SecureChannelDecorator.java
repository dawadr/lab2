package net.channel;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import message.request.SecureRequest;
import message.response.FailedResponse;
import message.response.SecureResponse;

import org.bouncycastle.util.encoders.Base64;

public class SecureChannelDecorator extends ChannelDecorator {

	public SecureChannelDecorator(IChannel decoratedChannel) {
		super(decoratedChannel);
	}

	@Override
	public void writeObject(Object o) throws IOException {	
		// Serialize
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(bos);
		out.writeObject(o);
		out.close();
		// base64
		byte[] oBytes = bos.toByteArray();
		byte[] base64 = Base64.encode(oBytes);
		// wrap in Request
		SecureRequest r = new SecureRequest(base64);
		//System.out.println("writing secureRequest");
		super.writeObject(r);
	}

	@Override
	public Object readObject() throws IOException, ClassNotFoundException {	
		Object o = null;
		while (!(o instanceof SecureRequest)) {
			o = super.readObject();
		}	
		//System.out.println("incoming secureRequest");
		SecureRequest r = (SecureRequest)o;
		// base64 decode
		byte[] decoded = Base64.decode(r.getEncryptedRequest());	
		// deserialize
		ByteArrayInputStream bis = new ByteArrayInputStream(decoded);
		ObjectInputStream in = new ObjectInputStream(bis);

		Object oDecoded = in.readObject();
		return oDecoded;
	}
}
