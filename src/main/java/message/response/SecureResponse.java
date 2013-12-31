package message.response;

import message.Response;

public class SecureResponse implements Response {

	private static final long serialVersionUID = -2005000174151668819L;
	private byte[] encryptedResponse;
		
	public SecureResponse(byte[] encryptedResponse) {
		this.encryptedResponse = encryptedResponse;
	}
	
	public byte[] getEncryptedResponse() {
		return encryptedResponse;
	}
	
}
