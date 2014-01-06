package proxy.mc;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import proxy.Uac;
import net.ILogAdapter;
import util.Config;
import util.KeyProvider;

public class ManagementServiceServer {
	
	private int port;
	private String name;
	private ILogAdapter log;
	private IManagementService ms;
	
	public ManagementServiceServer(Uac uac, KeyProvider keyProvider, Config config) {	
		Config mc = new Config("mc");
		this.port = mc.getInt("proxy.rmi.port");
		this.name = mc.getString("binding.name");
		
		try {
			this.ms = new ManagementService(uac, keyProvider, config);
		} catch (RemoteException e) {
			log("ManagementServiceImpl could not be initialized: " + e);
		}
		
	}
	
	public void start() {	
		try {			
			IManagementService obj = (IManagementService) UnicastRemoteObject.exportObject(ms, 0);
			Registry registry = LocateRegistry.createRegistry(port);
			registry.bind(this.name, obj);
			log("ManagementServiceServer running");
			
		} catch (Exception e) {
			log("start failed: " + e); 
		}
		
	}
	
	private void log(String message) {
		if (log != null) log.log("[ManagementServiceServer] " + message);
	}
	
	public void setLogAdapter(ILogAdapter log) {
		this.log = log;
	}
	
	public void stop() {	
		try {		
			UnicastRemoteObject.unexportObject(ms, true);
			log("ManagementServiceServer stopped");	
		} catch (Exception e) {
			log("stop failed: " + e); 
		}	
	}
	
}
