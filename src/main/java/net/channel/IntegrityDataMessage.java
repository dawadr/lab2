package net.channel;

/**
 * enthaelt die Daten und den generierten Hash
 * @author juliadaurer
 *
 * erweitert datamessage und ist daher abwärtskompatibel -> kann auch von channels empfangen werden die nur ein Datamessage erwarten (Client)
 */
public class IntegrityDataMessage extends DataMessage {
	private static final long serialVersionUID = -2583081741708481647L;
	private String hash;
	
	public IntegrityDataMessage(byte[] data, String hash) {
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
