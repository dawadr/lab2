package net.channel;

import java.io.Serializable;

/**
 * enthaelt die Daten und den generierten Hash
 * @author juliadaurer
 *
 */
public class VerifiedObjectMessage implements Serializable {
	private static final long serialVersionUID = -2583081741708481647L;
	private String hash;
	private Object object;


	public VerifiedObjectMessage(Object object, String hash) {
		this.object = object;
		this.hash = hash; 
	}

	public String getHash() {
			return hash;
	}

	public Object getObject() {
		return object;
	}

	public String toString() {
		return getHash() + " " + object.toString();
	}
	
}
