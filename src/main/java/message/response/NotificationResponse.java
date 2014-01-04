package message.response;

import message.Response;
import model.File;
import model.FileServerInfo;

import java.util.List;

public class NotificationResponse implements Response {
	private static final long serialVersionUID = -2527380842129589182L;

	private File file;

	public NotificationResponse(File f) {
		this.file = f;
	}

	@Override
	public String toString() {
		return "Notification: " + file.getName() + " got downloaded " + file.getDownloads() +" times!\n";
	}
}
