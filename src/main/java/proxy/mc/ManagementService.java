package proxy.mc;

import java.rmi.RemoteException;

import proxy.DownloadStatistics;
import proxy.Uac;
import util.Config;
import util.KeyProvider;
import client.IClientCli;
import message.Response;
import message.response.SubscriptionResponse;
import message.response.TopThreeDownloadsResponse;

public class ManagementService implements IManagementService {
	
	private Uac uac;
	private KeyProvider keyProvider;
	private Config config;
	
	// Implementations must have an explicit constructor
	// in order to declare the RemoteException exception
	public ManagementService(Uac uac, KeyProvider keyProvider, Config config) throws RemoteException {
		super(); 
		
		this.uac = uac;
		this.keyProvider = keyProvider;
		this.config = config;
	}

	@Override
	public int getReadQuorum() throws RemoteException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getWriteQuorum() throws RemoteException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Response getTopThree() throws RemoteException {
		
		return new TopThreeDownloadsResponse(DownloadStatistics.getInstance().getTopThree());
	}

	@Override
	public Response subscribe(String filename, int notificationInterval, INotifyCallback notifyCallback)
			throws RemoteException {
		
		//TODO check login
		
		if (notifyCallback != null) {
			DownloadStatistics.getInstance().addSubscription(filename, notificationInterval, notifyCallback);
			return new SubscriptionResponse(filename, true);
		} else
			return new SubscriptionResponse(filename, false);
	}

	@Override
	public String getProxyPublicKey() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean setUserPublicKey(String username) throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

}
