package net.channel;


import java.io.IOException;

import org.bouncycastle.util.encoders.Base64;


/**
 * Ein Channel, der die Daten beim Schreiben Base64-verschluesselt und beim Lesen wieder entschluesselt.
 * @author Alex
 *
 */
public class Base64Channel extends ChannelDecorator {

	public Base64Channel(IChannel decoratedChannel) {
		super(decoratedChannel);
	}

	@Override
	public void writeBytes(byte[] data) throws IOException {
		// base64 encode
		byte[] base64 = Base64.encode(data);
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
