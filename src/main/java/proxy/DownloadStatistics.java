package proxy;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import proxy.rmi.INotifyCallback;
import message.response.NotificationResponse;
import model.DownloadInfo;
import model.DownloadSubscription;

public class DownloadStatistics {
	
	private static final DownloadStatistics INSTANCE = new DownloadStatistics();
	private List<DownloadInfo> statistics;
	private List<DownloadSubscription> subscriptions;
	 
	private DownloadStatistics() {
		this.statistics = new ArrayList<DownloadInfo>();
		this.subscriptions = new ArrayList<DownloadSubscription>();
	}
 
	public static DownloadStatistics getInstance() {
		return INSTANCE;
	}
	
	public void reportDownload(String filename) {
		synchronized(this.statistics) {
			if(this.statistics.contains(new DownloadInfo(filename))) {
				
				int index = this.statistics.indexOf(new DownloadInfo(filename));
				
				DownloadInfo f = this.statistics.get(index);
				f.reportDownload();
				int downloads = f.getDownloads();
				this.statistics.set(index, f);
				
				// Sortierung nach Downloadhaeufigkeit
				if (index > 0) {
					while(this.statistics.get(index - 1).getDownloads() < downloads) {
						DownloadInfo temp = this.statistics.get(index - 1);
						this.statistics.set(index - 1, f);
						this.statistics.set(index, temp);
						index--;
						if(index < 1)  break;
					}
				}
				
			} else {
				this.statistics.add(new DownloadInfo(filename, 1));
			}
		}
		
		this.checkNotifications(filename);
	}
	
	/*
	 * Invoked after every download. Invocation reflects 1 download.
	 */
	private void checkNotifications(String filename) {
		
		synchronized(this.subscriptions) {
			for(int i = 0; i < this.subscriptions.size(); i++) {
				if(this.subscriptions.get(i).getFilename().equals(filename)) {
					DownloadSubscription ds = this.subscriptions.get(i);
					ds.reportDownload();
					
					if(ds.getDownloadsUntilNotification() == 0) {
						try {
							ds.getNotifyCallback().notify(new NotificationResponse(ds.getFilename(), ds.getNotificationInterval()));
						} catch (RemoteException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						ds.resetDownloadsUntilNotification();
					}
					
					System.out.println(ds.toString());
					
					this.subscriptions.set(i, ds);
				}
			}
		}
	}

	public void removeSubscription(INotifyCallback notifyCallback) {
		
		synchronized(this.subscriptions) {
			for(int i = 0; i < this.subscriptions.size(); i++) {
				if(this.subscriptions.get(i).getNotifyCallback().equals(notifyCallback)) {
					this.subscriptions.remove(i);
					i--;
				}
			}
		}
	}
	
	public void addSubscription(String filename, int notificationInterval, INotifyCallback notifyCallback) {
		DownloadSubscription ds = new DownloadSubscription(filename, notificationInterval, notifyCallback); 
		synchronized(this.subscriptions) {
			this.subscriptions.add(ds);
		}
	}
	
	public List<DownloadInfo> getTopThree() {
		
		List<DownloadInfo> result = new ArrayList<DownloadInfo>();

		int three = 3;
		
		synchronized(this.statistics) {
			if(this.statistics.size() < three) 
				three = this.statistics.size();
			
			for(int i = 0; i < three;i++) {
				result.add(this.statistics.get(i));
			}
		}
		
		return result;
	}

}