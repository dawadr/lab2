package proxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.channel.Base64Channel;
import net.channel.ByteChannel;
import net.channel.IObjectChannel;
import net.channel.IObjectChannelFactory;
import net.channel.ObjectChannel;

/**
 * Erzeugt die Channel fuer die Verbindung mit den Fileservern
 * @author Alex
 *
 */
public class FileserverChannelFactory implements IObjectChannelFactory {

	@Override
	public IObjectChannel create(OutputStream out, InputStream in) throws IOException {
		return new ObjectChannel((new Base64Channel(new ByteChannel(out, in))));
	}

}
