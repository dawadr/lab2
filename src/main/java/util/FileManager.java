package util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides methods to access and modify files in a specific directory and tracks each file's version.
 * @author Alex
 *
 */
public class FileManager {

	private Map<String, Integer> versions;
	private String directory;

	public FileManager(String directory) {
		this.versions = new HashMap<String, Integer>();
		this.directory = directory;
		if (!this.directory.endsWith("/")) this.directory = this.directory + '/';
	}


	public String getDirectory() {
		return directory;
	}

	public synchronized List<String> getFileList() {
		File folder = new File(directory);
		ArrayList<String> files = new ArrayList<String>();
		for (final File fileEntry : folder.listFiles()) {
			if (fileEntry.isFile()) {
				files.add(fileEntry.getName());
			}
		}
		return files;
	}

	public synchronized long getSize(String filename) throws IOException {
		if (!fileExists(filename)) throw new FileNotFoundException("File not found: " + filename);
		registerVersion(filename, 1);
		return new java.io.File(directory + filename).length();
	}

	public synchronized boolean fileExists(String filename) {
		return new java.io.File(directory + filename).exists();
	}

	public synchronized int getVersion(String filename) throws IOException {
		if (!fileExists(filename)) throw new FileNotFoundException("File not found: " + filename);
		registerVersion(filename, 1);
		return versions.get(filename);
	}

	public synchronized byte[] readFile(String filename) throws IOException {
		if (!fileExists(filename)) throw new FileNotFoundException("File not found: " + filename);
		registerVersion(filename, 1);
		return readBytesFromFile(directory + filename);
	}

	public synchronized void writeFile(String filename, int version, byte[] content) throws IOException {
		if (!isFilenameValid(filename)) throw new IOException("Invalid filename.");

		if (versions.containsKey(filename)) {
			// file gets overwritten
			versions.put(filename, version);
		} else {
			// file is new
			registerVersion(filename, version);
		}
		writeBytesToFile(directory + filename, content);
	}


	private void registerVersion(String filename, int version) {
		if (!versions.containsKey(filename)) {
			// file is new
			versions.put(filename, version);
		}
	}

	private synchronized void writeBytesToFile(String path, byte[] content) throws IOException {
		try  { 
			FileOutputStream fos = new FileOutputStream(path);
			try {
				fos.write(content);
			} catch (Exception e) { 
			} finally {
				fos.close();
			}	
		}
		catch (IOException e)
		{
			throw e;
		}
	}

	private synchronized byte[] readBytesFromFile(String path) throws IOException {
		File file = new File(path);
		ByteArrayOutputStream ous = null;
		InputStream ios = null;
		try {
			byte[] buffer = new byte[4096];
			ous = new ByteArrayOutputStream();
			ios = new FileInputStream(file);
			int read = 0;
			while ( (read = ios.read(buffer)) != -1 ) {
				ous.write(buffer, 0, read);
			}
		} finally { 
			try {
				if ( ous != null ) 
					ous.close();
			} catch (IOException e) {
			}
			try {
				if ( ios != null ) 
					ios.close();
			} catch (IOException e) {
			}
		}
		return ous.toByteArray();		
	}

	private boolean isFilenameValid(String file) {
		java.io.File f = new java.io.File(file);
		try {
			f.getCanonicalPath();
			return true;
		} catch (IOException e) {
			return false;
		}
	}

}
