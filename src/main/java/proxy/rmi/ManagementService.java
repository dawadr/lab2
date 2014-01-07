package proxy.rmi;

import java.rmi.RemoteException;

import proxy.DownloadStatistics;
import proxy.FileServerManager;
import proxy.Uac;
import util.Config;
import util.KeyProvider;
import client.IClientCli;
import message.Response;
import message.response.QuorumResponse;
import message.response.QuorumResponse.QuorumType;
import message.response.SubscriptionResponse;
import message.response.TopThreeDownloadsResponse;

public class ManagementService implements IManagementService {
	
	private Uac uac;
	private KeyProvider keyProvider;
	private Config config;
	private FileServerManager fileServerManager;
	
	// Implementations must have an explicit constructor
	// in order to declare the RemoteException exception
	public ManagementService(Uac uac, KeyProvider keyProvider, Config config, 
			FileServerManager fileServerManager) throws RemoteException {
		super(); 
		
		this.uac = uac;
		this.keyProvider = keyProvider;
		this.config = config;
		this.fileServerManager = fileServerManager;
	}

	@Override
	public Response getReadQuorum() throws RemoteException {
		
		int quorum = this.fileServerManager.getServerProvider().getReadQuorum().size();
		return new QuorumResponse(QuorumType.READ, quorum);
	}

	@Override
	public Response getWriteQuorum() throws RemoteException {
		
		int quorum = this.fileServerManager.getServerProvider().getWriteQuorum().size();
		return new QuorumResponse(QuorumType.WRITE, quorum);
	}

	@Override
	public Response getTopThree() throws RemoteException {
		
		return new TopThreeDownloadsResponse(DownloadStatistics.getInstance().getTopThree());
	}

	@Override
	public Response subscribe(String filename, int notificationInterval, INotifyCallback notifyCallback, 
			String username) throws RemoteException {
		
		if(username == null) return new SubscriptionResponse(filename, false);
		
		if (notifyCallback != null && uac.isLoggedIn(username)) {
			DownloadStatistics.getInstance().addSubscription(filename, notificationInterval, notifyCallback);
			return new SubscriptionResponse(filename, true);
		} else
			return new SubscriptionResponse(filename, false);
	}

	@Override
	public Response getProxyPublicKey() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response setUserPublicKey(String username) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

}
