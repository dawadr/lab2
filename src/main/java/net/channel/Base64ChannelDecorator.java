package net.channel;


import java.io.IOException;
import message.DataMessage;

import org.bouncycastle.util.encoders.Base64;

import util.Serialization;

public class Base64ChannelDecorator extends ChannelDecorator {

	public Base64ChannelDecorator(IChannel decoratedChannel) {
		super(decoratedChannel);
	}

	@Override
	public void writeObject(Object o) throws IOException {	
		byte[] data = null;

		if (o instanceof DataMessage) {
			// DTO wurde von unterliegendem Channel schon serialisiert und in ein DataMessage-Objekt gepackt
			data = ((DataMessage)o).getData();
		} else {			
			// Objekt serialisieren
			data = Serialization.serialize(o);
		}

		// base64
		byte[] base64 = Base64.encode(data);
		// wrap in DataMessage
		DataMessage r = new DataMessage(base64);
		super.writeObject(r);
	}

	@Override
	public Object readObject() throws IOException, ClassNotFoundException {	
		Object o = null;
		while (!(o instanceof DataMessage)) {
			o = super.readObject();
		}	
		DataMessage r = (DataMessage)o;
		
		// base64 decode
		byte[] decoded = Base64.decode(r.getData());

		try {
			// deserialize wenn es sich um ein serialisiertes Objekt handelt
			Object oDecoded = Serialization.deserialize(decoded);
			return oDecoded;
		} catch (ClassNotFoundException e) {
			// bei den Daten handelt es sich nicht um ein serialisiertes Objekt -> daten in DataMessage weiterreichen
			return new DataMessage(decoded);	
		}
	}
}
