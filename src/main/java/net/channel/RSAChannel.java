package net.channel;


import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

import message.DataMessage;

import org.bouncycastle.util.encoders.Base64;

import util.Serialization;

public class RSAChannel extends ChannelDecorator {

	private PublicKey publicKey;
	private PrivateKey privateKey;

	public RSAChannel(IChannel decoratedChannel, PublicKey publicKey, PrivateKey privateKey) {
		super(decoratedChannel);
		this.publicKey = publicKey;
		this.privateKey = privateKey;
	}


	@Override
	public void writeBytes(byte[] data) throws IOException {
		try {
			Cipher crypt;
			crypt = Cipher.getInstance("RSA/NONE/OAEPWithSHA256AndMGF1Padding");
			crypt.init(Cipher.ENCRYPT_MODE, publicKey);
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
			crypt = Cipher.getInstance("RSA/NONE/OAEPWithSHA256AndMGF1Padding");
			crypt.init(Cipher.DECRYPT_MODE, privateKey);
			byte[] decrypted = crypt.doFinal(data);
			return decrypted;
		} catch (Exception e) {
			throw new IOException(e);
		} 
	}


}


