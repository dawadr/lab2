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

	PrivateKey privateKey;
	PublicKey publicKey;
	String username;
	
	public ProxyChannelFactory(String username, PrivateKey privateKey, PublicKey publicKey) {
		this.privateKey = privateKey;
		this.publicKey = publicKey;
		this.username = username;
	}

	
	@Override
	public IObjectChannel create(OutputStream out, InputStream in) throws IOException {
		return new ObjectChannel(new SecureClientChannel(new Base64Channel(new ByteChannel(out, in)), username, privateKey, publicKey));
	}

}
