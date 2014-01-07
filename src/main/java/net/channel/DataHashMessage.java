package net.channel;

/**
 * enthaelt die Daten und den generierten Hash
 * @author juliadaurer
 *
 */
public class DataHashMessage extends DataMessage {
	private static final long serialVersionUID = -2583081741708481647L;
	private String hash;
	
	public DataHashMessage(byte[] data, String hash) {
		super(data);
		this.hash = hash; 
	}
	public String getHash() {
		return hash;
	}
	public String toString() {
		return new String(this.hash) + " " + new String(super.getData());
	}
}
