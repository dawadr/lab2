package message.response;

import message.Response;

public class SubscriptionResponse implements Response {
	private static final long serialVersionUID = -4994758755943921733L;

	private final String filename;
	private final boolean success;

	public SubscriptionResponse(String filename, boolean success) {
		this.filename = filename;
		this.success = success;
	}

	public String getFilename() {
		return filename;
	}

	@Override
	public String toString() {
		if (success)
			return "Successfully subscribed for file: " + filename;
		else 
			return "Could not subscribe for file: " + filename;
	}
}
