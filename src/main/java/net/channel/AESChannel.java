package net.channel;

import java.io.IOException;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

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


