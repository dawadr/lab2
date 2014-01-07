package net.channel;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;

import org.bouncycastle.util.encoders.Base64;

import net.ILogAdapter;

/**
 * Ein Channel, welcher die Daten und den HMAC in ein  DTO wrapped und verschickt.
 * @author Julia
 *
 */

public class IntegrityDataChannel implements IChannel {
	private Key key;
	private ObjectOutputStream out;
	private ObjectInputStream in;
	private ILogAdapter log;

	public IntegrityDataChannel(OutputStream out, InputStream in, Key key) throws IOException {
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
			//			System.out.println("Sending DATAHASHMsg");
			out.writeObject(new IntegrityDataMessage(data, hash_encoded));
			//			System.out.println("Sended DATAHASHMsg");

		} catch (Exception e) {
			throw new IOException(e);
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
		//		System.out.println("Receiving DATAHASHMsg");

		byte[] data = null;

		// Es kommt eine Nachricht mit Hash an
		if (o instanceof IntegrityDataMessage) {
			log("Receiving IntegrityDataMessage");
			IntegrityDataMessage msg = (IntegrityDataMessage)o;
			try {
				if(integrityCheck(msg)) {
					data = msg.getData();
				} else {
					log(msg.toString());
					//TODO inform proxy about the tampering
				}
			} catch (Exception e) {
				throw new IOException(e);
			}
		}
		// Es kommt eine Nachricht ohne Hash an (zB zwischen Client und FS) -> einfach so weitergeben
		else if (o instanceof DataMessage) {
			log("Receiving DataMessage");
			DataMessage msg = (DataMessage)o;
			data = msg.getData();
		}
		//		else throw new IOException("Receiving data failed");

		return data;
	}


	@Override
	public void setLogAdapter(ILogAdapter log) {
		this.log = log;
	}

	protected void log(String message) {
		// TODO: aufr�umen
		if (log != null) log.log(message);
		System.out.println(message);
	}

	private boolean integrityCheck(IntegrityDataMessage msg) throws NoSuchAlgorithmException, InvalidKeyException {
		Mac hMac = Mac.getInstance("HmacSHA256");
		hMac.init(this.key);
		hMac.update(msg.getData());
		byte[] hash = hMac.doFinal();
		String hash_encoded = new String(Base64.encode(hash));	
		return msg.getHash().equals(hash_encoded);
	}

}
