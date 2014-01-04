package model;

import java.io.Serializable;

/**
 * Contains information about a file.
 */
public class File implements Serializable {

	//must be serializable for transmitting
	private static final long serialVersionUID = 1L;
	private String name;
	private int downloads;
	private int downloadsUntilNotification;

	public File(String name) {
		this.name = name;
	}

	public File(String name, int downloads) {
		this.name = name;
		this.downloads = downloads;
		this.downloadsUntilNotification = downloads;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getDownloads() {
		return downloads;
	}

	public void setDownloads(int downloads) {
		this.downloads = downloads;
	}
	
	public void reportDownload() {
		this.downloadsUntilNotification--;
	}

	public int getDownloadsUntilNotification() {
		return downloadsUntilNotification;
	}

	@Override
	public String toString() {
		return "File [name=" + name + ", downloads=" + downloads + "]";
	}

}
