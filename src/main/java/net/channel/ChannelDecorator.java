package net.channel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class ChannelDecorator implements IChannel {

	private IChannel decoratedChannel;
	
	public ChannelDecorator (IChannel decoratedChannel) {
        this.decoratedChannel = decoratedChannel;
    }
	
	@Override
	public void initialize(OutputStream out, InputStream in) throws IOException {
		decoratedChannel.initialize(out, in);
	}

	@Override
	public void writeObject(Object o) throws IOException {
		decoratedChannel.writeObject(o);
	}

	@Override
	public Object readObject() throws IOException, ClassNotFoundException {
		return decoratedChannel.readObject();
	}

}
