package net.channel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * Erzeugt die Channel fuer Client <-> Fileservern
 * @author Alex
 *
 */
public class ObjectChannelFactory implements IObjectChannelFactory {

	@Override
	public IObjectChannel create(OutputStream out, InputStream in) throws IOException {
		return new ObjectChannel(out, in);
	}

}
