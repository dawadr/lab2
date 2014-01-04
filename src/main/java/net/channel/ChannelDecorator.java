package net.channel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.ILogAdapter;

/**
 * 
 * @author Alex
 *
 */
public abstract class ChannelDecorator implements IChannel {

	protected IChannel decoratedChannel;
	private ILogAdapter log;
	
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

	
	@Override
	public void setLogAdapter(ILogAdapter log) {
		this.log = log;
	}

	protected void log(String message) {
		if (log != null) log.log(message);
		System.out.println(message);
	}
}
