package client;


import java.io.IOException;
import java.security.PrivateKey;
import java.security.PublicKey;

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

		String clientChallenge = "AbcClientChallengeAbc";

		String msg1 = "!login " + user + " " + clientChallenge;
		//DataMessage r = new DataMessage(s1.getBytes());
		rsa.writeBytes(msg1.getBytes());
		System.out.println("Sent " + msg1);

		byte[] b = rsa.readBytes();
		String response = new String(b);
		System.out.println("Receiving " + response);

		// Response parsen
		String[] parameters = response.split(" ");

		// checken ob "!ok <client-challenge> ..."
		if (parameters.length != 5 && !parameters[0].equals("!ok") && !parameters[1].equals(clientChallenge)) return false;

		String proxyChallenge = parameters[2];
		String secretKey = parameters[3];
		String ivParameter = parameters[4];


		// finish handshake by establishing AES channel
		aes = new AESChannel(decoratedChannel, secretKey, ivParameter);
		String msg3 = proxyChallenge;
		aes.writeBytes(msg3.getBytes());
		System.out.println("Sent " + msg3);

		initialized = true;
		System.out.println("AES channel established");
		return true;
	}
}


