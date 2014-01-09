package proxy.mc;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.PublicKey;

import message.Response;

public interface IManagementService extends Remote {
	
	public Response getReadQuorum() throws RemoteException;
	
	public Response getWriteQuorum() throws RemoteException;
	
	public Response getTopThree() throws RemoteException;
	
	public Response subscribe(String filename, int downloads, INotifyCallback notifyCallback, String username, String password) throws RemoteException;
	
	public Response getProxyPublicKey() throws RemoteException;
	
	public Response setUserPublicKey(PublicKey key, String username) throws RemoteException;

	void unsubscribe(INotifyCallback notifyCallback) throws RemoteException;

	
	
}
