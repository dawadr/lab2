package proxy;

import java.rmi.Remote;
import java.rmi.RemoteException;

import client.INotifyCallback;
import message.Response;

public interface ManagementService extends Remote {
	
	public int getReadQuorum() throws RemoteException;
	
	public int getWriteQuorum() throws RemoteException;
	
	public Response getTopThree() throws RemoteException;
	
	public Response subscribe(String filename, int downloads, INotifyCallback notifyCallback) throws RemoteException;
	
	public String getProxyPublicKey() throws RemoteException;
	
	public boolean setUserPublicKey(String username) throws RemoteException;
	
}
