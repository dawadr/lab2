package net.channel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;

import net.ILogAdapter;

public class IntegrityChannelFactory implements IObjectChannelFactory {

	private Key key;
	private ILogAdapter log;
	
	/**
	 * Initialisiert die Factory
	 * @param key Shared Key von Proxy/Fileserver
	 */
	public IntegrityChannelFactory(Key key) {
		this.key = key;
	}

	
	@Override
	public IObjectChannel create(OutputStream out, InputStream in) throws IOException {
		System.out.println("Secure TCP Channel erstellen");
		// Objekte serialisieren -> Base64 codieren -> Bytes (Hash + Data) schicken
		return new ObjectChannel(new Base64Channel(new IntegrityChannel(out, in, key)));
		
	}
	

}