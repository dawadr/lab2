package net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DatagramReceiver implements IDatagramReceiver {

	private ExecutorService threadPool;
	private DatagramSocket socket;
	private List<IDatagramPacketListener> listeners;
	private boolean listen = false;
	private ILogAdapter log;

	public DatagramReceiver(int port) throws SocketException {
		this.socket = new DatagramSocket(port);
		this.listeners = new ArrayList<IDatagramPacketListener>();
		threadPool = Executors.newFixedThreadPool(50);
	}


	@Override
	public void run()  {
		if (listen) throw new IllegalStateException();
		listen = true;
		log("DatagramReceiver running");
		while(listen) {  
			// Receive packet
			byte[] buf = new byte[5000];
			DatagramPacket packet = new DatagramPacket(buf, buf.length);	   
			try {
				socket.receive(packet);
				onReceived(packet);
			} catch (IOException e) {}			
		}
		log("DatagramReceiver stopped");
	}

	@Override
	public void close() {
		listen = false;
		socket.close();
		// Shutdown threads
		threadPool.shutdown(); 
		try {			
			if (!threadPool.awaitTermination(5, TimeUnit.SECONDS)) {
				threadPool.shutdownNow(); 
				if (!threadPool.awaitTermination(5, TimeUnit.SECONDS))
					System.err.println("Pool did not terminate");
			}
		} catch (InterruptedException ie) {		
			threadPool.shutdownNow();
			Thread.currentThread().interrupt();
		}
	}

	@Override
	public void addListener(IDatagramPacketListener listener) {
		this.listeners.add(listener);
	}

	@Override
	public void removeListener(IDatagramPacketListener listener) {
		this.listeners.remove(listener);
	}
	
	@Override
	public int getPort() {
		return socket.getLocalPort();
	}

	private void log(String message) {
		if (log != null) log.log("[DatagramReceiver Port " + getPort() + "] " + message);
	}

	@Override
	public void setLogAdapter(ILogAdapter log) {
		this.log = log;
	}

	private void onReceived(DatagramPacket packet) {
		// handle each packet in a separate thread
		PacketHandler handler = new PacketHandler(packet);
		threadPool.execute(handler);
	}


	private class PacketHandler implements Runnable {
		private DatagramPacket packet;

		public PacketHandler(DatagramPacket packet) {
			this.packet = packet;
		}

		@Override
		public void run() {
			synchronized (listeners) { // prevent other threads changing listeners
				for (IDatagramPacketListener listener : listeners) {
					listener.packetReceived(packet);
				}
			}
		}	
	}

}