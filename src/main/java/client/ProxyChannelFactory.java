package client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import util.KeyProvider;
import net.channel.Base64Channel;
import net.channel.ByteChannel;
import net.channel.IObjectChannel;
import net.channel.IObjectChannelFactory;
import net.channel.ObjectChannel;

public class ProxyChannelFactory implements IObjectChannelFactory {

	@Override
	public IObjectChannel create(OutputStream out, InputStream in) throws IOException {
		KeyProvider kp = new KeyProvider("keys");
		PrivateKey prk = kp.getPrivateKey("alice", "12345");
		PublicKey puk = kp.getPublicKey("proxy.pub");
		return new ObjectChannel(new SecureClientChannel(new Base64Channel(new ByteChannel(out, in)), "alice", prk, puk));
	}

}
