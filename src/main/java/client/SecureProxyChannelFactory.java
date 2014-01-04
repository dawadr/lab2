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
		// Objekte serialisieren -> verschluesseln -> Base64 codieren -> Bytes schicken
		return new ObjectChannel(new SecureProxyChannel(new Base64Channel(new ByteChannel(out, in)), username, privateKey, publicKey));
	}

}