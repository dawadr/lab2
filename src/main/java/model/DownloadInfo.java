package model;

import java.io.Serializable;

/**
 * Contains information about a file.
 */
public class DownloadInfo implements Serializable {

	//must be serializable for transmitting
	private static final long serialVersionUID = 1L;
	private String filename;
	private int downloads;

	public DownloadInfo(String filename) {
		this.filename = filename;
	}

	public DownloadInfo(String filename, int downloads) {
		this.filename = filename;
		this.downloads = downloads;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public int getDownloads() {
		return downloads;
	}

	public void setDownloads(int downloads) {
		this.downloads = downloads;
	}
	
	public void reportDownload() {
		this.downloads++;
	}

	@Override
	public String toString() {
		return String.format("%1$-12s %2$5d", filename, downloads);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof DownloadInfo)
            return filename.equals(((DownloadInfo) o).getFilename()); 
        else
            return false;	
	}

}
