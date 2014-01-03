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
import net.channel.DataMessage;
import net.channel.IChannel;
import net.channel.RSAChannel;

import org.bouncycastle.util.encoders.Base64;

import util.KeyProvider;
import util.Serialization;

public class SecureChannel extends ChannelDecorator {

	private boolean initialized = false;
	private PrivateKey privateKey;
	private KeyProvider keyProvider;
	private RSAChannel rsa;
	private AESChannel aes;

	public SecureChannel(IChannel decoratedChannel, PrivateKey privateKey, KeyProvider keyProvider) {
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
			initialize();
		}

		// Secure channel established - use AES channel
		return aes.readBytes();
	}


	private boolean initialize() throws IOException {
		System.out.println("Waiting for handshake");

		// rsa channel erstellen
		this.rsa = new RSAChannel(decoratedChannel, null, privateKey); // public key des users noch nicht bekannt -> null


		/**
		 * Message 1 lesen
		 */
		byte[] b = rsa.readBytes();
		String response = new String(b);
		System.out.println("Received " + response);
		// Response parsen
		String[] parameters = response.split(" ");

		// checken ob "!login <user> <client-challenge>"
		if (parameters.length != 3 && !parameters[0].equals("!login")) return false;

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
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		PublicKey publicKey = keyProvider.getPublicUserKey(username);
		this.rsa = new RSAChannel(decoratedChannel, publicKey, privateKey);


		/**
		 * Message 2 senden
		 */
		String msg2 = "!ok " + clientChallenge_encoded + " " + proxyChallenge_encoded + " " + secretKey_encoded + " " + ivParameter_encoded;
		rsa.writeBytes(msg2.getBytes());
		System.out.println("Sent " + msg2);


		/**
		 * AES aufbauen & Message 3 lesen
		 */
		aes = new AESChannel(decoratedChannel, secretKey, new IvParameterSpec(ivParameter));
		String msg3 = new String(aes.readBytes());
		System.out.println("Received " + msg3);
		if (!msg3.equals(proxyChallenge_encoded)) return false;

		initialized = true;
		System.out.println("AES channel established");
		return true;
	}
}


