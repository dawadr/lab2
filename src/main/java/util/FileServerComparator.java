package util;

import java.util.Comparator;
import model.FileServer;

public class FileServerComparator implements Comparator<FileServer> { 
	@Override
	public int compare(FileServer arg0, FileServer arg1) {
		if (arg0.getUsage() < arg1.getUsage()) return -1;
		return 1;
	}
};