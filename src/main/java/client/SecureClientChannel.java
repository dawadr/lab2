package client;


import java.io.IOException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;

import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import net.channel.AESChannel;
import net.channel.ChannelDecorator;
import net.channel.DataMessage;
import net.channel.IChannel;
import net.channel.RSAChannel;

import org.bouncycastle.util.encoders.Base64;

import util.Serialization;

public class SecureClientChannel extends ChannelDecorator {

	private boolean initialized = false;
	private PrivateKey privateKey;
	private PublicKey publicKey;
	private String user;
	private RSAChannel rsa;
	private AESChannel aes;

	public SecureClientChannel(IChannel decoratedChannel, String user, PrivateKey privateKey, PublicKey publicKey) {
		super(decoratedChannel);
		this.privateKey = privateKey;
		this.publicKey = publicKey;
		this.user = user;
	}


	@Override
	public void writeBytes(byte[] data) throws IOException {
		if (!initialized) {
			try {
				if (initialize() == false) {
					throw new IOException("Secure channel authentication failed.");
				}
			} catch (IOException e) {
				throw new IOException("Secure channel authentication failed.", e);
			}
		}

		// Secure channel established - use AES channel
		aes.writeBytes(data);
	}

	@Override
	public byte[] readBytes() throws IOException {
		if (!initialized) {
			throw new IOException("Secure channel not yet established.");
		}

		// Secure channel established - use AES channel
		return aes.readBytes();
	}


	private boolean initialize() throws IOException {
		System.out.println("Initializing handshake");
		
		// rsa channel erstellen
		this.rsa = new RSAChannel(decoratedChannel, publicKey, privateKey);

	
		/**
		 * Message 1 senden
		 */		
		// ClientChallenge erstellen
		SecureRandom secureRandom = new SecureRandom(); 
		final byte[] clientChallenge = new byte[32]; 
		secureRandom.nextBytes(clientChallenge);
		String clientChallenge_encoded = new String(Base64.encode(clientChallenge));

		// Message senden
		String msg1 = "!login " + user + " " + clientChallenge_encoded;
		rsa.writeBytes(msg1.getBytes());
		System.out.println("Sent " + msg1);

		
		/**
		 * Message 2 lesen
		 */
		byte[] b = rsa.readBytes();
		String response = new String(b);
		System.out.println("Received " + response);

		// Response parsen
		String[] parameters = response.split(" ");

		// checken ob "!ok <client-challenge> ..."
		if (parameters.length != 5 && !parameters[0].equals("!ok") && !parameters[1].equals(clientChallenge_encoded)) return false;

		String proxyChallenge_encoded = parameters[2];
		String secretKey_encoded = parameters[3];
		String ivParameter_encoded = parameters[4];
		byte[] secretKey = Base64.decode(secretKey_encoded.getBytes());
		byte[] ivParameter = Base64.decode(ivParameter_encoded.getBytes());


		/**
		 * AES aufbauen und Message 3 senden
		 */
		aes = new AESChannel(decoratedChannel, new SecretKeySpec(secretKey, "AES/CTR/NoPadding"), new IvParameterSpec(ivParameter));
		aes.writeBytes(proxyChallenge_encoded.getBytes());
		System.out.println("Sent " + proxyChallenge_encoded);

		initialized = true;
		System.out.println("AES channel established");
		return true;
	}
}


