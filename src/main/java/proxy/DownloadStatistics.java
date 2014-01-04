package proxy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import client.IClientCli;
import message.response.NotificationResponse;
import model.File;

public class DownloadStatistics {
	
	private static final DownloadStatistics INSTANCE = new DownloadStatistics();
	private Map<String, Integer> downloads;
	private Map<File, IClientCli> notifications;
	 
	private DownloadStatistics() {
		this.downloads = new HashMap<String, Integer>();
	}
 
	public static DownloadStatistics getInstance() {
		return INSTANCE;
	}
	
	public synchronized void reportDownload(String filename) {
		if(this.downloads.containsKey(filename)) {
			int i = this.downloads.get(filename);
			this.downloads.put(filename, ++i);
		} else {
			this.downloads.put(filename, 1);
		}
		
		this.checkNotifications(filename);
	}
	
	/*
	 * Invoked after every download. Invocation reflects 1 download.
	 */
	private synchronized void checkNotifications(String filename) {
		Iterator<Entry<File, IClientCli>> it = this.notifications.entrySet().iterator();
		
	    while (it.hasNext()) {
	        Map.Entry<File, IClientCli> pair = (Map.Entry<File, IClientCli>) it.next();
	        
	        if(pair.getKey().getName().equals(filename)) {
	        	pair.getKey().reportDownload();
	        	
	        	if(pair.getKey().getDownloadsUntilNotification() < 1) {
	        		pair.getValue().notify(new NotificationResponse(pair.getKey()));
	        		this.notifications.remove(pair.getKey());
	        	}
	        }
	        
	        it.remove(); // avoids a ConcurrentModificationException
	    }
	}

	public void removeNotification(IClientCli cli) {
		//TODO
	}
	
	public void addNotification(String filename, int downloads, IClientCli cli) {
		File f = new File(filename, downloads);
		this.notifications.put(f, cli);
	}
	
	public synchronized Map<String, Integer> getTopThree() {
		List<Integer> mapValues = new ArrayList<Integer>(downloads.values());
		List<String> mapKeys = new ArrayList<String>(downloads.keySet());
	    Collections.sort(mapValues);
	    Collections.sort(mapKeys);
		
	    HashMap<String, Integer> result = new HashMap<String, Integer>();
		Iterator<Integer> valueIt = mapValues.iterator();
		
		while (valueIt.hasNext()) {
			Integer val = valueIt.next();
			Iterator<String> keyIt = mapKeys.iterator();

	       	while (keyIt.hasNext()) {
	       		String key = keyIt.next();

	       		if (downloads.get(key).equals(val)){
	       			mapKeys.remove(key);
	       			result.put(key, val);
	       			if(result.size() == 3) {
	       				return result;
	       			}
	       			break;
	       		}
       		}
		}
		
		//falls weniger als 3 Elemente
		return result;
	}

}