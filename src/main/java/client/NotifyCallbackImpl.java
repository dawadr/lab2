package client;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import cli.Shell;
import proxy.mc.INotifyCallback;
import message.Response;

/**
 * implementiert das INotifyCallback fuer die management component
 * @author Alex
 *
 */
public class NotifyCallbackImpl extends UnicastRemoteObject implements INotifyCallback {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5144760321797630531L;
	private Shell shell;

	protected NotifyCallbackImpl(Shell shell) throws RemoteException {
		super();
		this.shell = shell;
	}


	@Override
	public void notify(Response r) throws RemoteException {
		try {
			shell.writeLine(r.toString());
		} catch (IOException e) {
			System.out.println("Notification callback failed: " + e.toString());
		}
	}

}
