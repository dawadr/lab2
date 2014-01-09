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

public class VerifiedObjectChannel implements IObjectChannel {
	private Key key;
	private ObjectOutputStream out;
	private ObjectInputStream in;
	private ILogAdapter log;
	private int maxRepeat;
	private ITamperedMessageOutput messageOutput;

	private boolean repeat;
	private boolean hmacUsed = true;
	private VerifiedObjectMessage lastSentMessage;

	/**
	 * 
	 * @param out
	 * @param in
	 * @param key
	 * @param repeat
	 * @throws IOException
	 */
	public VerifiedObjectChannel(OutputStream out, InputStream in, Key key, boolean repeat, int maxRepeat, ITamperedMessageOutput messageOutput) throws IOException {
		this.out = new ObjectOutputStream(out);
		this.in  = new ObjectInputStream((in));
		this.key = key;
		this.repeat = repeat;
		this.maxRepeat = maxRepeat;
		this.messageOutput = messageOutput;
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
			lastSentMessage = new VerifiedObjectMessage(o, hash_encoded);
			out.writeObject(lastSentMessage);
			//			System.out.println("Sended DATAHASHMsg");
		} catch (Exception e) {
			throw new IOException(e);
		} 
	}

	@Override
	public Object readObject() throws IOException, ClassNotFoundException {

		Object returnObject = null;
		boolean tampered = true;
		int i = 0;

		do {
			Object o;
			try {
				o = in.readObject();
			} catch (ClassNotFoundException e) {
				throw new IOException(e);
			}
			if (o == null) return null;
			//		System.out.println("Receiving DATAHASHMsg");

			// Es kommt eine Nachricht mit Hash an
			if (o instanceof VerifiedObjectMessage) {
				hmacUsed = true;
				VerifiedObjectMessage msg = (VerifiedObjectMessage)o;
				log("Receiving IntegrityObjectMessage: " + msg.toString());
				try {
					if(verify(msg)) {
						returnObject = msg.getObject();
						tampered = false;
					} else {
						if (messageOutput != null) messageOutput.write(msg.toString());
						tampered = true;			
						if (repeat) {
							// repeat message to fileserver
							log("Received invalid message - repeating lastSentMessage");
							i++;
							out.writeObject(lastSentMessage);
						} else {
							//inform proxy about the tampering
							log("Received invalid message - sending TamperedMessage");
							out.writeObject(new TamperedMessage());	
						}				
					}
				} catch (Exception e) {
					throw new IOException(e);
				}
			}
			// Es kommt ein TamperedMessage an
			else if (o instanceof TamperedMessage) {
				// repeat message to fileserver
				log("Received TamperedMessage - repeating lastSentMessage");
				i++;
				out.writeObject(lastSentMessage);
				tampered = true;			
			}
			// Es kommt eine Nachricht ohne Hash an (zB zwischen Client und FS) -> einfach so weitergeben
			else {
				hmacUsed = false;
				log("Receiving object without hash");
				returnObject = o;
				tampered = false;
			}
		} while (tampered && i < maxRepeat + 1);

		if (i > maxRepeat) {

			throw new IOException("Maximum number of repeats after tampered messages exceeded.");
		}

		return returnObject;
	}


	@Override
	public void setLogAdapter(ILogAdapter log) {
		this.log = log;
	}

	protected void log(String message) {
		if (log != null) log.log(message);
		//		System.out.println(message);
	}


	private boolean verify(VerifiedObjectMessage msg) throws NoSuchAlgorithmException, InvalidKeyException, IllegalStateException, IOException {
		
		Mac hMac = Mac.getInstance("HmacSHA256");
		hMac.init(this.key);
		hMac.update(Serialization.serialize(msg.getObject()));
		byte[] hash = hMac.doFinal();
		String hash_encoded = new String(Base64.encode(hash));	
		return msg.getHash().equals(hash_encoded);
	}

}
