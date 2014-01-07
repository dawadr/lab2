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

import util.Serialization;
import net.ILogAdapter;

/**
 * Ein Channel, welcher die Daten und den HMAC in ein  DTO wrapped und verschickt.
 * @author Julia
 *
 */

public class IntegrityObjectChannel implements IObjectChannel {
	private Key key;
	private ObjectOutputStream out;
	private ObjectInputStream in;
	private ILogAdapter log;
	
	private boolean hmacUsed = true;

	public IntegrityObjectChannel(OutputStream out, InputStream in, Key key) throws IOException {
		this.out = new ObjectOutputStream(out);
		this.in  = new ObjectInputStream((in));
		this.key = key;
	}

	@Override
	public void writeObject(Object o) throws IOException {
		// wenn nachrichten ohne hash ankamen, soll auch ohne hash versendet werden (zb bei client)
		if (!hmacUsed) {
			out.writeObject(o);
			return;
		}
		try {
			Mac hMac = Mac.getInstance("HmacSHA256");
			hMac.init(this.key);
			hMac.update(Serialization.serialize(o));
			byte[] hash = hMac.doFinal();
			String hash_encoded = new String(Base64.encode(hash));
			//			System.out.println("Sending DATAHASHMsg");
			out.writeObject(new IntegrityObjectMessage(o, hash_encoded));
			//			System.out.println("Sended DATAHASHMsg");

		} catch (Exception e) {
			throw new IOException(e);
		} 
	}

	@Override
	public Object readObject() throws IOException, ClassNotFoundException {
		Object o;
		try {
			o = in.readObject();
		} catch (ClassNotFoundException e) {
			throw new IOException(e);
		}
		if (o == null) return null;
		//		System.out.println("Receiving DATAHASHMsg");

		Object returnObject = null;

		// Es kommt eine Nachricht mit Hash an
		if (o instanceof IntegrityObjectMessage) {
			hmacUsed = true;
			IntegrityObjectMessage msg = (IntegrityObjectMessage)o;
			log("Receiving IntegrityObjectMessage: " + msg.toString());
			try {
				if(integrityCheck(msg)) {
					returnObject = msg.getObject();
				} else {
					log(msg.toString());
					//TODO inform proxy about the tampering
				}
			} catch (Exception e) {
				throw new IOException(e);
			}
		}
		// Es kommt eine Nachricht ohne Hash an (zB zwischen Client und FS) -> einfach so weitergeben
		else {
			hmacUsed = false;
			log("Receiving object without hash");
			returnObject = o;
		}

		return returnObject;
	}


	@Override
	public void setLogAdapter(ILogAdapter log) {
		this.log = log;
	}

	protected void log(String message) {
		// TODO: aufräumen
		if (log != null) log.log(message);
		System.out.println(message);
	}

	private boolean integrityCheck(IntegrityObjectMessage msg) throws NoSuchAlgorithmException, InvalidKeyException, IllegalStateException, IOException {
		Mac hMac = Mac.getInstance("HmacSHA256");
		hMac.init(this.key);
		hMac.update(Serialization.serialize(msg.getObject()));
		byte[] hash = hMac.doFinal();
		String hash_encoded = new String(Base64.encode(hash));	
		return msg.getHash().equals(hash_encoded);
	}


}
