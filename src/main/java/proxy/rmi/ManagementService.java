package proxy.rmi;

import java.io.IOException;
import java.rmi.RemoteException;
import java.security.PublicKey;

import proxy.DownloadStatistics;
import proxy.FileServerManager;
import proxy.Uac;
import util.Config;
import util.KeyProvider;
import message.Response;
import message.response.QuorumResponse;
import message.response.QuorumResponse.QuorumType;
import message.response.PublicKeyResponse;
import message.response.MessageResponse;
import message.response.SubscriptionResponse;
import message.response.TopThreeDownloadsResponse;

public class ManagementService implements IManagementService {
	
	private Uac uac;
	private KeyProvider keyProvider;
	private Config clientConfig;
	private FileServerManager fileServerManager;
	
	// Implementations must have an explicit constructor
	// in order to declare the RemoteException exception
	public ManagementService(Uac uac, KeyProvider keyProvider, FileServerManager fileServerManager) 
			throws RemoteException {
		super(); 
		
		this.uac = uac;
		this.keyProvider = keyProvider;
		this.clientConfig = new Config("client");
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
		
		PublicKey publicKey = null; 
		
		try {
			publicKey = keyProvider.getPublicKey(clientConfig.getString("proxy.key"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return new PublicKeyResponse(publicKey);
	}

	@Override
	public Response setUserPublicKey(PublicKey key, String username) throws RemoteException {
		if(key != null) {
			try {
				keyProvider.writeKeyTo(key, "keys/upload." + username.toLowerCase() + ".pub.pem");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return new MessageResponse("Successfully transmitted public key of user: " + username);
		}
		
		return new MessageResponse("Transmitting public key of user " + username + " was not successful.");
	}

	@Override
	public void unsubscribe(INotifyCallback notifyCallback)
			throws RemoteException {
		DownloadStatistics.getInstance().removeSubscription(notifyCallback);
	}

}
