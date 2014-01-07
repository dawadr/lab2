package message.response;

import java.security.Key;

import message.Response;

public class KeyResponse implements Response {
	private static final long serialVersionUID = -7058325034457705550L;

	private final Key key;

	public KeyResponse(Key key) {
		this.key = key;
	}

	public Key getKey() {
		return key;
	}
}
