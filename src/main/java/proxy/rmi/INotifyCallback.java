package proxy.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

import message.Response;

public interface INotifyCallback extends Remote {

	public void notify(Response r) throws RemoteException;
	
}
