package net.channel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;

import net.ILogAdapter;


public class VerifiedObjectChannelFactory implements IObjectChannelFactory {

	private Key key;
	private boolean repeat;

	/**
	 * Initialisiert die Factory
	 * @param key Shared Key von Proxy/Fileserver
	 */
	public VerifiedObjectChannelFactory(Key key, boolean repeat) {
		this.key = key;
		this.repeat = repeat;
	}


	@Override
	public IObjectChannel create(OutputStream out, InputStream in) throws IOException {
		// System.out.println("Secure TCP Channel erstellen");
		IObjectChannel c = new VerifiedObjectChannel(out, in, key, repeat);     
		return c;
	}


}