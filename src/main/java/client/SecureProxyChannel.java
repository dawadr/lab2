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

/**
 * Ein Channel, der eine sichere Verbindung zur Verfügung stellt.
 * Die Verbindung mit dem Proxy wird gemaess spezifiziertem Handshake eigenstaendig aufgebaut.
 * Hierzu verwendet der Channel intern RSA- und AESChannel.
 * @author Alex
 *
 */
public class SecureProxyChannel extends ChannelDecorator {

	private boolean initialized = false;
	private PrivateKey privateKey;
	private PublicKey publicKey;
	private String username;
	private RSAChannel rsa;
	private AESChannel aes;

	/**
	 * Initialisiert den Channel
	 * @param decoratedChannel Der zu dekorierende unterliegende Channel
	 * @param user Der User fuer den die Verbindung hergestellt wrden soll
	 * @param privateKey Der RSA-PrivateKey des Users
	 * @param publicKey Der RSA-PublicKey der Gegenstelle
	 */
	public SecureProxyChannel(IChannel decoratedChannel, String username, PrivateKey privateKey, PublicKey publicKey) {
		super(decoratedChannel);
		this.privateKey = privateKey;
		this.publicKey = publicKey;
		this.username = username;
	}


	@Override
	public void writeBytes(byte[] data) throws IOException {
		if (!initialized) {
			try {
				if (initialize() == false) {
					log("Handshake failed");
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
		log("Initializing handshake");
		
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
		String msg1 = "!login " + username + " " + clientChallenge_encoded;
		rsa.writeBytes(msg1.getBytes());
		log("Sent " + msg1);

		
		/**
		 * Message 2 lesen
		 */
		byte[] b = rsa.readBytes();
		String response = new String(b);
		log("Received " + response);

		// Response parsen
		String[] parameters = response.split(" ");

		// checken ob "!ok <client-challenge> ..."
		if (parameters.length != 5 && !parameters[0].equals("!ok") && !parameters[1].equals(clientChallenge_encoded)) {
			log("Invalid response");
			return false;
		}

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
		log("Sent " + proxyChallenge_encoded);

		initialized = true;
		log("AES channel established");
		return true;
	}
}


