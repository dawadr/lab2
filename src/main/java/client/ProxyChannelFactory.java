package client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.channel.Base64Channel;
import net.channel.ByteChannel;
import net.channel.IObjectChannel;
import net.channel.IObjectChannelFactory;
import net.channel.ObjectChannel;
import net.channel.SecureClientChannel;

public class ProxyChannelFactory implements IObjectChannelFactory {

	@Override
	public IObjectChannel create(OutputStream out, InputStream in) throws IOException {
		return new ObjectChannel(new SecureClientChannel(new Base64Channel(new ByteChannel(out, in))));
	}

}
