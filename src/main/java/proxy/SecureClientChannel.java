package proxy;


import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import net.channel.AESChannel;
import net.channel.ChannelDecorator;
import net.channel.IChannel;
import net.channel.RSAChannel;

import org.bouncycastle.util.encoders.Base64;

import util.KeyProvider;

/**
 * Ein Channel, der eine sichere Verbindung mit dem Client zur Verfuegung stellt.
 * Die Verbindung wird gemaess spezifiziertem Handshake-Protokoll initialisiert.
 * Hierzu verwendet der Channel intern RSA- und AESChannel.
 * @author Alex
 *
 */
public class SecureClientChannel extends ChannelDecorator {

	private boolean initialized = false;
	private PrivateKey privateKey;
	private KeyProvider keyProvider;
	private RSAChannel rsa;
	private AESChannel aes;

	/**
	 * 
	 * @param decoratedChannel
	 * @param privateKey Der Private Key zum RSA-Verschluesseln
	 * @param keyProvider Stellt die Public Keys der User zur Verfuegung
	 */
	public SecureClientChannel(IChannel decoratedChannel, PrivateKey privateKey, KeyProvider keyProvider) {
		super(decoratedChannel);
		this.privateKey = privateKey;
		this.keyProvider = keyProvider;
	}


	@Override
	public void writeBytes(byte[] data) throws IOException {
		if (!initialized) {
			throw new IOException("Secure channel not yet established.");
		}

		// Secure channel established - use AES channel
		aes.writeBytes(data);
	}

	@Override
	public byte[] readBytes() throws IOException {
		while (!initialized) {
			if (initialize() == false) log("Handshake failed");
		}

		// Secure channel established - use AES channel
		return aes.readBytes();
	}


	private boolean initialize() throws IOException {
		log("Waiting for handshake");

		// rsa channel erstellen
		this.rsa = new RSAChannel(decoratedChannel, null, privateKey); // public key des users noch nicht bekannt -> null


		/**
		 * Message 1 lesen
		 */
		byte[] b = rsa.readBytes();
		String response = new String(b);
		log("Received " + response);
		// Response parsen
		String[] parameters = response.split(" ");

		// checken ob "!login <user> <client-challenge>"
		if (parameters.length != 3 && !parameters[0].equals("!login")) {
			log("Invalid Response");
			return false;
		}

		String username = parameters[1];
		String clientChallenge_encoded = parameters[2];


		/**
		 * Proxy Challenge, Secret Key & IV Parameter erzeugen
		 */	
		// ProxyChallenge erstellen
		SecureRandom secureRandom = new SecureRandom(); 
		final byte[] ProxyChallenge = new byte[32]; 
		secureRandom.nextBytes(ProxyChallenge);
		String proxyChallenge_encoded = new String(Base64.encode(ProxyChallenge));

		// Secret Key
		KeyGenerator generator = null;
		try {
			generator = KeyGenerator.getInstance("AES");
		} catch (NoSuchAlgorithmException e) {
			throw new IOException(e);
		} 
		generator.init(256); 
		SecretKey secretKey = generator.generateKey(); 
		String secretKey_encoded = new String(Base64.encode(secretKey.getEncoded()));

		// IV parameter
		secureRandom = new SecureRandom(); 
		final byte[] ivParameter = new byte[16]; 
		secureRandom.nextBytes(ivParameter);
		String ivParameter_encoded = new String(Base64.encode(ivParameter));


		/**
		 * RSA neu initialisieren
		 */
		PublicKey publicKey = keyProvider.getPublicKey(username);
		this.rsa = new RSAChannel(decoratedChannel, publicKey, privateKey);


		/**
		 * Message 2 senden
		 */
		String msg2 = "!ok " + clientChallenge_encoded + " " + proxyChallenge_encoded + " " + secretKey_encoded + " " + ivParameter_encoded;
		rsa.writeBytes(msg2.getBytes());
		log("Sent " + msg2);


		/**
		 * AES aufbauen & Message 3 lesen
		 */
		aes = new AESChannel(decoratedChannel, secretKey, new IvParameterSpec(ivParameter));
		String msg3 = new String(aes.readBytes());
		log("Received " + msg3);
		if (!msg3.equals(proxyChallenge_encoded)) {
			log("Invalid Proxy Challenge");
			return false;
		}

		initialized = true;
		log("AES channel established");
		return true;
	}
}


