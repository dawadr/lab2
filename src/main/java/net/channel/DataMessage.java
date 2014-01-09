package net.channel;

import java.io.Serializable;

/**
 * Ein DTO das serialsierte/verschluesselte/codierte Daten enthaelt.
 * @author Alex
 *
 */
public class DataMessage implements Serializable {

	private static final long serialVersionUID = 6387584378764829956L;
	private byte[] data;

	public DataMessage(byte[] data) {
		this.data = data;
	}

	public byte[] getData() {
		return data;
	}

	public String toString() {
		return new String(data);
	}

}
