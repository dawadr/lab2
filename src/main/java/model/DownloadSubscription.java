package model;

import java.io.Serializable;

import client.IClientCli;

public class DownloadSubscription implements Serializable {

	//must be serializable for transmitting
	private static final long serialVersionUID = 1L;
	private final String filename;
	private final int notificationInterval;
	private int downloadsUntilNotification;
	private final IClientCli cli;

	public DownloadSubscription(String filename, int notificationInterval, IClientCli cli) {
		this.filename = filename;
		this.notificationInterval = notificationInterval;
		this.downloadsUntilNotification = notificationInterval;
		this.cli = cli;
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

	public IClientCli getCli() {
		return cli;
	}

	public void reportDownload() {
		this.downloadsUntilNotification--;
	}

	//TODO
	/*@Override
	public String toString() {
		return String.format("%1$-12s %2$5d", filename, downloads);
	}*/
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof DownloadSubscription)
            return filename.equals(((DownloadSubscription) o).getFilename()) 
            		&& cli.equals(((DownloadSubscription) o).getCli()) ; 
        else
            return false;	
	}

}
