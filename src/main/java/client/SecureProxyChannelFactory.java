package client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.PrivateKey;
import java.security.PublicKey;

import net.channel.Base64Channel;
import net.channel.DataChannel;
import net.channel.IObjectChannel;
import net.channel.IObjectChannelFactory;
import net.channel.SerializedChannel;

/**
 * 
 * @author Alex
 *
 */
public class SecureProxyChannelFactory implements IObjectChannelFactory {

	PrivateKey privateKey;
	PublicKey publicKey;
	String username;
	
	/**
	 * Initialisiert die Factory
	 * @param username Der User fuer den der Channel initialisiert werden soll
	 * @param privateKey Private Key des Users
	 * @param publicKey Public Key der Gegenstelle
	 */
	public SecureProxyChannelFactory(String username, PrivateKey privateKey, PublicKey publicKey) {
		this.privateKey = privateKey;
		this.publicKey = publicKey;
		this.username = username;
	}

	
	@Override
	public IObjectChannel create(OutputStream out, InputStream in) throws IOException {
		// Objekte serialisieren -> verschluesseln -> Base64 codieren -> Daten schicken
		return new SerializedChannel(new SecureProxyChannel(new Base64Channel(new DataChannel(out, in)), username, privateKey, publicKey));
	}

}
