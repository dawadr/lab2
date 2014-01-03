package net.channel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class ChannelDecorator implements IChannel {

	protected IChannel decoratedChannel;
	
	public ChannelDecorator (IChannel decoratedChannel) {
        this.decoratedChannel = decoratedChannel;
    }
	

	@Override
	public void writeBytes(byte[] data) throws IOException {
		decoratedChannel.writeBytes(data);
	}

	@Override
	public byte[] readBytes() throws IOException {
		return decoratedChannel.readBytes();
	}

}
