package proxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.PrivateKey;
import util.KeyProvider;
import net.channel.Base64Channel;
import net.channel.DataChannel;
import net.channel.IObjectChannel;
import net.channel.IObjectChannelFactory;
import net.channel.SerializedChannel;

/**
 * Erzeugt SecureClientChannel
 * @author Alex
 *
 */
public class SecureClientChannelFactory implements IObjectChannelFactory {

	private KeyProvider keyProvider;
	private PrivateKey privateKey;
	
	/**
	 * 
	 * @param keyProvider Der KeyProvider, der die User-PublicKeys fuer den Channel zur Verfuegung stellt
	 * @param privateKey Der eigene PrivateKey
	 */
	public SecureClientChannelFactory(KeyProvider keyProvider, PrivateKey privateKey) {
		this.keyProvider = keyProvider;
		this.privateKey = privateKey;
	}
	
	@Override
	public IObjectChannel create(OutputStream out, InputStream in) throws IOException {
		return new SerializedChannel(new SecureClientChannel(new Base64Channel(new DataChannel(out, in)), privateKey, keyProvider));
	}

}
