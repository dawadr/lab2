package model;

import java.io.Serializable;

import proxy.rmi.INotifyCallback;

public class DownloadSubscription implements Serializable {

	//must be serializable for transmitting
	private static final long serialVersionUID = 1L;
	private final String filename;
	private final int notificationInterval;
	private int downloadsUntilNotification;
	private final INotifyCallback notifyCallback;

	public DownloadSubscription(String filename, int notificationInterval, INotifyCallback notifyCallback) {
		this.filename = filename;
		this.notificationInterval = notificationInterval;
		this.downloadsUntilNotification = notificationInterval;
		this.notifyCallback = notifyCallback;
	}

	public String getFilename() {
		return filename;
	}
	
	public int getNotificationInterval() {
		return notificationInterval;
	}
	
	public void resetDownloadsUntilNotification() {
		this.downloadsUntilNotification = this.notificationInterval;
	}
	
	public int getDownloadsUntilNotification() {
		return downloadsUntilNotification;
	}

	public INotifyCallback getNotifyCallback() {
		return notifyCallback;
	}

	public void reportDownload() {
		this.downloadsUntilNotification--;
	}
	
	@Override
	public String toString() {
		return "DownloadSubscription [filename=" + filename
				+ ", notificationInterval=" + notificationInterval
				+ ", downloadsUntilNotification=" + downloadsUntilNotification
				+ ", notifyCallback=" + notifyCallback + "]";
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof DownloadSubscription)
            return filename.equals(((DownloadSubscription) o).getFilename()) 
            		&& notifyCallback.equals(((DownloadSubscription) o).getNotifyCallback()) ; 
        else
            return false;	
	}

}
