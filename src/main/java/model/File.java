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

	public File(String name) {
		this.name = name;
	}

	public File(String name, int downloads) {
		this.name = name;
		this.downloads = downloads;
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
		this.downloads++;
	}

	@Override
	public String toString() {
		return String.format("%1$-12s %2$5d", name, downloads);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof File)
            return name.equals(((File) o).getName()); 
        else
            return false;	
	}

}
