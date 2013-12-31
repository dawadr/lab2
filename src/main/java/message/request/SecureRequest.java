package message.request;

import message.Request;

public class SecureRequest implements Request {

	private static final long serialVersionUID = 6387584378764829956L;
	private byte[] encryptedRequest;
		
	public SecureRequest(byte[] encryptedRequest) {
		this.encryptedRequest = encryptedRequest;
	}
	
	public byte[] getEncryptedRequest() {
		return encryptedRequest;
	}
	
}
