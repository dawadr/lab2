package proxy;

import java.util.ArrayList;
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
	private List<File> statistics;
	private Map<File, IClientCli> notifications;
	 
	private DownloadStatistics() {
		this.statistics = new ArrayList<File>();
		this.notifications = new HashMap<File, IClientCli>();
	}
 
	public static DownloadStatistics getInstance() {
		return INSTANCE;
	}
	
	public synchronized void reportDownload(String filename) {
		if(this.statistics.contains(new File(filename))) {
			
			int index = this.statistics.indexOf(new File(filename));
			
			File f = this.statistics.get(index);
			f.reportDownload();
			int downloads = f.getDownloads();
			this.statistics.set(index, f);
			
			// Sortierung nach Downloadhaeufigkeit
			if (index > 0) {
				while(this.statistics.get(index - 1).getDownloads() < downloads) {
					File temp = this.statistics.get(index - 1);
					this.statistics.set(index - 1, f);
					this.statistics.set(index, temp);
					index--;
					if(index < 1)  break;
				}
			}
			
		} else {
			this.statistics.add(new File(filename, 1));
		}
		
		//this.checkNotifications(filename);
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
	        	
	        	/*if(pair.getKey().getDownloadsUntilNotification() < 1) {
	        		pair.getValue().notify(new NotificationResponse(pair.getKey()));
	        		this.notifications.remove(pair.getKey());
	        	}*/
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
	
	public synchronized List<File> getTopThree() {
		
		List<File> result = new ArrayList<File>();

		int three = 3;
		
		if(this.statistics.size() < three) 
			three = this.statistics.size();
		
		for(int i = 0; i < three;i++) {
			result.add(this.statistics.get(i));
		}
		
		return result;
	}

}