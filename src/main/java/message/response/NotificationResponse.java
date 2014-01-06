package message.response;

import message.Response;

public class NotificationResponse implements Response {
	private static final long serialVersionUID = -2527380842129589182L;

	private String filename;
	private int notificationInterval;

	public NotificationResponse(String filename, int notificationInterval) {
		this.notificationInterval = notificationInterval;
		this.filename = filename;
	}

	@Override
	public String toString() {
		return "Notification: " + this.filename + " got downloaded " + this.notificationInterval +" times!\n";
	}
}
