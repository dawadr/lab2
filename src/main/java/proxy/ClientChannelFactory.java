package proxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.PrivateKey;
import java.security.PublicKey;

import util.KeyProvider;
import net.channel.Base64Channel;
import net.channel.ByteChannel;
import net.channel.IObjectChannel;
import net.channel.IObjectChannelFactory;
import net.channel.ObjectChannel;


public class ClientChannelFactory implements IObjectChannelFactory {

	@Override
	public IObjectChannel create(OutputStream out, InputStream in) throws IOException {
		
		KeyProvider kp = new KeyProvider("keys");
		PrivateKey prk = kp.getPrivateKey("proxy", "12345");
		
		return new ObjectChannel(new SecureServerChannel(new Base64Channel(new ByteChannel(out, in)), prk, null));
	}

}
