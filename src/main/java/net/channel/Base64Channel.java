package net.channel;


import java.io.IOException;
import message.DataMessage;

import org.bouncycastle.util.encoders.Base64;

import util.Serialization;

public class Base64Channel extends ChannelDecorator {

	public Base64Channel(IChannel decoratedChannel) {
		super(decoratedChannel);
	}

	@Override
	public void writeBytes(byte[] data) throws IOException {

		// base64
		byte[] base64 = Base64.encode(data);
		// wrap in DataMessage
		//DataMessage r = new DataMessage(base64);
		super.writeBytes(base64);
	}

	@Override
	public byte[] readBytes() throws IOException {

		byte[] data = super.readBytes();
		if (data == null) return null;

		// base64 decode
		byte[] decoded = Base64.decode(data);
		return decoded;	
	}
}
