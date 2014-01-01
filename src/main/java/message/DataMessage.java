package message;

import java.io.Serializable;

/**
 * Ein DTO das serialsierte/verschlüsselte/codierte Responses/Requests enthält.
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
	
}
