package proxy;

import java.rmi.RemoteException;

import client.IClientCli;
import message.Response;
import message.response.TopThreeDownloadsResponse;

public class ManagementServiceImpl implements ManagementService {
	
	// Implementations must have an explicit constructor
	// in order to declare the RemoteException exception
	public ManagementServiceImpl() throws RemoteException {
		super(); 
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
	public Response subscribe(String filename, int downloads, IClientCli cli)
			throws RemoteException {
		// TODO Auto-generated method stub
		return null;
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
