package net.channel;


import java.io.IOException;

import message.DataMessage;

import org.bouncycastle.util.encoders.Base64;

import util.Serialization;

public class ObjectChannel extends ChannelDecorator implements IObjectChannel {

	public ObjectChannel(IChannel decoratedChannel) {
		super(decoratedChannel);
	}

	@Override
	public void writeObject(Object o) throws IOException {
		byte[] data = null;

		// Objekt serialisieren
		data = Serialization.serialize(o);

		super.writeBytes(data);
	}

	@Override
	public Object readObject() throws IOException, ClassNotFoundException {
		byte[] data = super.readBytes();
		if (data == null) return null;
		
		// deserialize
		Object o = Serialization.deserialize(data);
		return o;
	}
}
