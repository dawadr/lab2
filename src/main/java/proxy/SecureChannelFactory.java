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

public class SecureChannelFactory implements IObjectChannelFactory {

	private KeyProvider keyProvider;
	private PrivateKey privateKey;
	
	public SecureChannelFactory(KeyProvider keyProvider, PrivateKey privateKey) {
		this.keyProvider = keyProvider;
		this.privateKey = privateKey;
	}
	
	@Override
	public IObjectChannel create(OutputStream out, InputStream in) throws IOException {
		return new ObjectChannel(new SecureChannel(new Base64Channel(new ByteChannel(out, in)), privateKey, keyProvider));
	}

}
