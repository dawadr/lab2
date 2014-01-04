package net.channel;

import java.io.IOException;
import java.security.AlgorithmParameters;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;

import util.Serialization;

/**
 * Ein Channel, der die Daten beim Senden mit den uebergebenen Keys AES-verschluesselt und beim Lesen wieder entschluesselt.
 * @author Alex
 *
 */
public class AESChannel extends ChannelDecorator {

	private SecretKey secretKey;
	private IvParameterSpec ivParameter;
	
	
	public AESChannel(IChannel decoratedChannel, SecretKey secretKey, IvParameterSpec ivParameter) {
		super(decoratedChannel);
		this.secretKey = secretKey;
		this.ivParameter = ivParameter;
	}


	@Override
	public void writeBytes(byte[] data) throws IOException {
		try {
			Cipher crypt;
			crypt = Cipher.getInstance("AES/CTR/NoPadding");
			crypt.init(Cipher.ENCRYPT_MODE, secretKey, ivParameter);
			byte[] encrypted = crypt.doFinal(data);
			super.writeBytes(encrypted);
		} catch (Exception e) {
			throw new IOException(e);
		} 
	}

	@Override
	public byte[] readBytes() throws IOException {
		byte[] data = super.readBytes();
		if (data == null) return null;
		try {
			Cipher crypt;
			crypt = Cipher.getInstance("AES/CTR/NoPadding");
			crypt.init(Cipher.DECRYPT_MODE, secretKey, ivParameter);
			byte[] decrypted = crypt.doFinal(data);
			return decrypted;
		} catch (Exception e) {
			throw new IOException(e);
		} 
	}

}


