package net.channel;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.security.Key;
import java.security.MessageDigest;

import javax.crypto.Mac;

import org.bouncycastle.util.encoders.Base64;

import net.ILogAdapter;

/**
 * Ein Channel, welcher die Daten und den HMAC in ein  DTO wrapped und verschickt.
 * @author Julia
 *
 */

public class IntegrityChannel implements IChannel {
	private Key key;

	private ObjectOutputStream out;
	private ObjectInputStream in;
	private ILogAdapter log;

	public IntegrityChannel(OutputStream out, InputStream in, Key key) throws IOException {
		this.out = new ObjectOutputStream(out);
		this.in  = new ObjectInputStream((in));
		this.key = key;
	}

	@Override
	public void writeBytes(byte[] data) throws IOException {
		try {
			Mac hMac = Mac.getInstance("HmacSHA256");
			hMac.init(this.key);
			hMac.update(data);
			byte[] hash = hMac.doFinal();
			String hash_encoded = new String(Base64.encode(hash));
			System.out.println("Sending DATAHASHMsg");
			out.writeObject(new DataHashMessage(data, hash_encoded));
			System.out.println("Sended DATAHASHMsg");

		} catch (Exception e) {
			log(e.getMessage());
		} 
	}

	@Override
	public byte[] readBytes() throws IOException {
		Object o;
		try {
			o = in.readObject();
		} catch (ClassNotFoundException e) {
			throw new IOException(e);
		}
		if (o == null) return null;
		DataHashMessage msg = null;
		System.out.println("Receiving DATAHASHMsg");
		if (o instanceof DataHashMessage) msg = (DataHashMessage)o;
		else throw new IOException();

		if(msg != null && !securityCheck(msg)) {
			log(msg.toString());
			//TODO inform proxy about the tampering
		}
		return msg.getData();

	}

	@Override
	public void setLogAdapter(ILogAdapter log) {
		this.log = log;
	}

	protected void log(String message) {
		if (log != null) log.log(message);
	}

	private boolean securityCheck(DataHashMessage msg) {
		Mac hMac;
		try {
			hMac = Mac.getInstance("HmacSHA256");
			hMac.init(this.key);
			hMac.update(msg.getData());

			byte[] computedHash = hMac.doFinal();
			byte[] receivedDecodedHash = msg.getHash().getBytes();
			
			return MessageDigest.isEqual(computedHash, receivedDecodedHash);

		} catch (Exception e) {
			log(e.getMessage());
		}
		return false;

	}

}
