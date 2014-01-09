package proxy.mc;

import java.io.IOException;
import java.rmi.RemoteException;
import java.security.PublicKey;

import proxy.DownloadStatistics;
import proxy.FileServerManager;
import proxy.Uac;
import util.KeyProvider;
import message.Response;
import message.response.FailedResponse;
import message.response.MessageResponse;
import message.response.PublicKeyResponse;
import message.response.QuorumResponse.QuorumType;
import message.response.SubscriptionResponse;
import message.response.QuorumResponse;
import message.response.TopThreeDownloadsResponse;

public class ManagementService implements IManagementService {

	private Uac uac;
	private KeyProvider keyProvider;
	private FileServerManager fileServerManager;
	private PublicKey proxyPublicKey;

	// Implementations must have an explicit constructor
	// in order to declare the RemoteException exception
	public ManagementService(Uac uac, KeyProvider keyProvider, FileServerManager fileServerManager, PublicKey proxyPublicKey) throws RemoteException {
		super(); 
		this.uac = uac;
		this.keyProvider = keyProvider;
		this.fileServerManager = fileServerManager;
		this.proxyPublicKey = proxyPublicKey;
	}

	
	@Override
	public Response getReadQuorum() throws RemoteException {	
		Integer quorum = this.fileServerManager.getReadQuorum();
		if (quorum == null) {
			return new QuorumResponse(QuorumType.READ);
		} else {
			return new QuorumResponse(QuorumType.READ, quorum);
		}
	}

	@Override
	public Response getWriteQuorum() throws RemoteException {
		Integer quorum = this.fileServerManager.getWriteQuorum();
		if (quorum == null) {
			return new QuorumResponse(QuorumType.WRITE);
		} else {
			return new QuorumResponse(QuorumType.WRITE, quorum);
		}
	}

	@Override
	public Response getTopThree() throws RemoteException {

		return new TopThreeDownloadsResponse(DownloadStatistics.getInstance().getTopThree());
	}

	@Override
	public Response subscribe(String filename, int notificationInterval, INotifyCallback notifyCallback, String username, String password)
			throws RemoteException {

		if (!uac.isValid(username, password)) return new FailedResponse("You need to be logged in to execute this command.");

		if (notifyCallback != null && uac.isLoggedIn(username)) {
			DownloadStatistics.getInstance().addSubscription(filename, notificationInterval, notifyCallback);
			return new SubscriptionResponse(filename, true);
		} else
			return new SubscriptionResponse(filename, false);
	}

	@Override
	public void unsubscribe(INotifyCallback notifyCallback)	throws RemoteException {
		DownloadStatistics.getInstance().removeSubscription(notifyCallback);
	}

	@Override
	public Response getProxyPublicKey() throws RemoteException {
		return new PublicKeyResponse(proxyPublicKey);
	}

	@Override
	public Response setUserPublicKey(PublicKey key, String username) throws RemoteException {
		if(key != null) {
			try {
				keyProvider.savePublicKey(key, "upload." + username.toLowerCase());
			} catch (IOException e) {
				return new FailedResponse(e);
			}

			return new MessageResponse("Successfully transmitted public key of user: " + username);
		}

		return new MessageResponse("Transmitting public key of user " + username + " was not successful.");
	}

}