package client;

import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedHashSet;

import proxy.mc.IManagementService;
import proxy.mc.INotifyCallback;



/**
 * Stellt das ManagementService der Management-Komponente zur Veruegung & methoden zum exporten/unexporten von Callbacks.
 * @author Alex
 *
 */
public class RemoteService {

	private String host;
	private String name;
	private int port;
	private IManagementService managementService;
	private LinkedHashSet<INotifyCallback> callbacks;

	public RemoteService(String host, String name, int port) {
		this.host = host;
		this.name = name;
		this.port = port;
		this.callbacks = new LinkedHashSet<INotifyCallback>();
	}

	/**
	 * liefert ein IManagementService und stellt dazu wenn noetig die Verbindung her.
	 * @return
	 * @throws NotBoundException
	 */
	public IManagementService getManagementService() throws NotBoundException {
		if (managementService != null) return managementService;
		try {
			Registry registry = LocateRegistry.getRegistry(host, port);
			this.managementService = (IManagementService) registry.lookup(name);
		} catch (Exception e) {
			this.managementService = null;
			throw new NotBoundException(e.getMessage());
		}
		return managementService;
	}

	/**
	 * exportiert ein Callback-Objekt
	 * @param callback
	 * @throws RemoteException
	 */
	public void exportCallback(INotifyCallback callback) throws RemoteException {
		//UnicastRemoteObject.exportObject(callback);
		callbacks.add(callback);
	}

	/**
	 * unexportet alle Callback-Objekte
	 */
	public void close() {
		for (INotifyCallback callback: callbacks) {
			try {
				getManagementService().unsubscribe(callback);
			} catch (NotBoundException e) {
			} catch (RemoteException e) {
			}
			try {
				UnicastRemoteObject.unexportObject(callback, true);
			} catch (NoSuchObjectException e) {
			}
		}
	}

}
