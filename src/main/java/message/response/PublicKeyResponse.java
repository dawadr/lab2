package message.response;

import java.security.PublicKey;

import message.Response;

public class PublicKeyResponse implements Response {
	private static final long serialVersionUID = -7058325034457705550L;

	private final PublicKey key;

	public PublicKeyResponse(PublicKey key) {
		this.key = key;
	}

	public PublicKey getKey() {
		return key;
	}
}
