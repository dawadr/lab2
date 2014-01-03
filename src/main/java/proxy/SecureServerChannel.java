package proxy;


import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import net.channel.AESChannel;
import net.channel.ChannelDecorator;
import net.channel.DataMessage;
import net.channel.IChannel;
import net.channel.RSAChannel;

import org.bouncycastle.util.encoders.Base64;

import util.KeyProvider;
import util.Serialization;

public class SecureServerChannel extends ChannelDecorator {

	private boolean initialized = false;
	private PrivateKey privateKey;
	private RSAChannel rsa;
	private AESChannel aes;
	private Uac uac;

	public SecureServerChannel(IChannel decoratedChannel, PrivateKey privateKey, Uac uac) {
		super(decoratedChannel);
		this.privateKey = privateKey;
		this.uac = uac;
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
		this.rsa = new RSAChannel(decoratedChannel, null, privateKey);

		// !login lesen
		byte[] b = rsa.readBytes();
		String response = new String(b);
		System.out.println("Receiving " + response);
		// Response parsen
		String[] parameters = response.split(" ");
		// checken ob "!login <user> <client-challenge>."
		if (parameters.length != 3 && !parameters[0].equals("!login")) return false;

		String username = parameters[1];
		String clientChallenge = parameters[2];
		String proxyChallenge = "--proxyChallenge--";
		
		// Secret Key
		KeyGenerator generator = null;
		try {
			generator = KeyGenerator.getInstance("AES");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		generator.init(256); 
		SecretKey key = generator.generateKey(); 
		String secretKey = new String(key.getEncoded());
		
		// IV parameter
		SecureRandom secureRandom = new SecureRandom(); 
		final byte[] number = new byte[16]; 
		secureRandom.nextBytes(number);
		String ivParameter = new String(number);


		// user authentifizieren & Rsa mit public key initialisieren
		//		PublicKey key;
		//		try {
		//			key = uac.authenticate(username);
		//		} catch (UacException e) {
		//			throw new IOException(e);
		//		}
		KeyProvider kp = new KeyProvider("keys");
		PublicKey puk = kp.getPublicKey("alice.pub");
		this.rsa = new RSAChannel(decoratedChannel, puk, privateKey);

		String msg2 = "!ok " + clientChallenge + " " + proxyChallenge + " " + secretKey + " " + ivParameter;
		//DataMessage r = new DataMessage(s1.getBytes());
		rsa.writeBytes(msg2.getBytes());
		System.out.println("Sent " + msg2);

		// finish handshake by establishing AES channel
		aes = new AESChannel(decoratedChannel, secretKey, ivParameter);
		String msg3 = new String(aes.readBytes());
		System.out.println("Receiving " + msg3);
		if (!msg3.equals(proxyChallenge)) return false;

		initialized = true;
		System.out.println("AES channel established");
		return true;
	}
}


