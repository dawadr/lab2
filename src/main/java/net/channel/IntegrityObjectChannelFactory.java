package net.channel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;


public class IntegrityObjectChannelFactory implements IObjectChannelFactory {

	private Key key;

	/**
	 * Initialisiert die Factory
	 * @param key Shared Key von Proxy/Fileserver
	 */
	public IntegrityObjectChannelFactory(Key key) {
		this.key = key;
	}


	@Override
	public IObjectChannel create(OutputStream out, InputStream in) throws IOException {
		// System.out.println("Secure TCP Channel erstellen");
		// Objekte serialisieren -> Base64 codieren -> Bytes (Hash + Data) schicken
		return new ObjectChannel(new Base64Channel(new IntegrityDataChannel(out, in, key)));     
	}


}