package proxy;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.security.Key;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

import util.FileServerComparator;
import model.FileServer;
import model.FileServerInfo;
import net.IConnection;
import net.IDatagramPacketListener;
import net.IDatagramReceiver;
import net.ILogAdapter;
import net.TcpConnection;
import net.channel.VerifiedObjectChannelFactory;

/**
 * Manages fileservers, monitors their status and provides methods to access and modify them.
 * @author Alex
 *
 */
public class FileServerManager {

	private List<FileServer> servers;
	private Map<FileServer, FileServerAdapter> adapters;
	private IDatagramReceiver datagramReceiver;
	private boolean stopped = true;
	private Timer t;
	private TimerTask task;
	private int checkPeriod;
	private int timeout;
	private ILogAdapter log;
	private Key key;
	
	private int nr;
	private int nw;
	private boolean quorumsSet;

	public FileServerManager(IDatagramReceiver datagramReceiver, Key key, int checkPeriod, int timeout, ILogAdapter log) {
		this.key = key;
		this.log = log;
		this.checkPeriod = checkPeriod;
		this.timeout = timeout;
		this.datagramReceiver = datagramReceiver;
		this.servers = new ArrayList<FileServer>();
		this.adapters = new HashMap<FileServer, FileServerAdapter>();
		task = new CheckTask();
	}

	/**
	 * Retrieves a list of all monitored fileservers
	 * @return
	 */
	public List<FileServerInfo> getServerList() {
		synchronized (servers) {
			// sort fileservers
			Collections.sort(servers, new FileServerComparator());
			ArrayList<FileServerInfo> list = new ArrayList<FileServerInfo>();
			for (FileServer i : servers) {
				list.add(i.toFileServerInfo());
			}
			return list;
		}
	}

	public IDatagramReceiver getDatagramReceiver() {
		return this.datagramReceiver;
	}

	/**
	 * Starts monitoring fileservers
	 */
	public void start() {
		IDatagramPacketListener listener = new IDatagramPacketListener() {
			@Override
			public void packetReceived(DatagramPacket packet) {
				processPacket(packet);
			}
		};
		datagramReceiver.addListener(listener);
		stopped = false;
		t = new Timer();
		t.scheduleAtFixedRate(task, checkPeriod, checkPeriod);
	}

	/**
	 * Stops monitoring of fileservers
	 */
	public void stop() {
		stopped = true;
		t.cancel();
	}

	/**
	 * Returns a {@link FileServerProvider} which provides methods to determine the least used fileserver and to send requests to all fileservers.
	 * @return
	 */
	public FileServerProvider getServerProvider() {		
		// statisch setzen beim ersten upload (https://tuwel.tuwien.ac.at/mod/forum/discuss.php?d=46934)
		if (!quorumsSet) {		
			// Gifford's scheme
			int n = servers.size();
			nw = (n / 2) + 1;	
			nr = (n / 2);
			if (nw < 1) nw = 1;
			if (nr < 1) nr = 1;
			quorumsSet = true;
			log("Quorums set: Nr = " + nr + ", nw = " + nw);
		}
		
		synchronized (servers) {
			// sort fileservers
			Collections.sort(servers, new FileServerComparator());
		}
		// get adapters
		List<FileServerAdapter> a = getOnlineAdapters();
		// get queue of adapters
		ConcurrentLinkedQueue<FileServerAdapter> q = new ConcurrentLinkedQueue<FileServerAdapter>(a);
		return new FileServerProvider(q, nr, nw);
	}

	/**
	 * Returns the {@link FileServerAdapter}s of all online fileservers
	 * @return
	 */
	public List<FileServerAdapter> getOnlineAdapters() {
		synchronized (servers) {
			ArrayList<FileServerAdapter> a = new ArrayList<FileServerAdapter>();
			for (FileServer server : servers) {
				// if online, get the adapter
				if (server.isOnline()) {
					a.add(getAdapter(server));
				}
			}
			return a;
		}	
	}

	/**
	 * Increases the specified fileserver's usage stats.
	 * @param server
	 * @param usage
	 */
	public void increaseUsage(FileServer server, long usage) {
		synchronized (servers) {
			if (!servers.contains(server)) throw new IllegalArgumentException("Server not listed");
		}	
		synchronized (server) {
			server.setUsage(server.getUsage() + usage);
		}	
	}


	private FileServerAdapter getAdapter(FileServer server) {
		synchronized (adapters) {
			if (adapters.containsKey(server)) return adapters.get(server);
			// TODO AUFPASSEN  new FileserverChannelFactory() AUF integritychannelfactory!!!! 
			IConnection c = new TcpConnection(server.getAddress().getHostAddress(), server.getPort(), new VerifiedObjectChannelFactory(this.key, true));
			c.setLogAdapter(log);
			
			FileServerAdapter a = new FileServerAdapter(c, server, log);
			adapters.put(server, a);
			return a;	
		}	
	}

	private void processPacket(DatagramPacket packet) {
		if (stopped) return;
		String received = new String(packet.getData(), 0, packet.getLength());
		if (!received.startsWith("!alive ")) return;
		received = received.substring(7);
		int tcpPort;
		try {
			tcpPort = Integer.parseInt(received);
		} catch (Exception e) {
			return;
		}
		if (tcpPort < 0 || tcpPort > 65535) return; // must be in tcp range
		InetAddress address = packet.getAddress();
		register(address, tcpPort);
	}

	private void register(InetAddress address, int port) {		
		synchronized (servers) {
			FileServer server = new FileServer(address, port, 0, true, new Date());
			// Check if server already registered
			if (servers.contains(server)) {
				int i = servers.indexOf(server);
				server = servers.get(i);
				// Update online and lastAlive
				synchronized (server) {
					server.setLastAlive(new Date());
					if (!server.isOnline()) {
						server.setOnline(true);
						log("Fileserver online: " + server);
					}
				}
			} else {
				// Add server
				servers.add(server);
				log("Fileserver online: " + server);
			}
		}
	}

	private void check() {
		synchronized (servers) {
			Date now = new Date();
			for (FileServer server : servers) {
				Date lastAlive = server.getLastAlive();
				long diffMs = now.getTime() - lastAlive.getTime(); 
				if (server.isOnline() && diffMs > timeout) {
					synchronized (server) {
						server.setOnline(false);
						log("Fileserver offline: " + server);
					}
				}
			}
		}
	}

	
	private void log(String message) {
		log.log("[FileServerManager] " + message);
	}
	
	private class CheckTask extends TimerTask {
		public void run() {
			check();
		}
	}

}
