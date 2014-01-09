package net;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TcpServer implements IServer, ILogAdapter {

	private ExecutorService threadPool;
	private List<IServerConnection> connections;
	private ServerSocket serverSocket;
	private IServerConnectionFactory connectionFactory;
	private boolean listen;
	private ILogAdapter log;

	public TcpServer(int port, IServerConnectionFactory connectionFactory) throws IOException {
		threadPool = Executors.newCachedThreadPool();
		serverSocket = new ServerSocket(port);
		connections = new ArrayList<IServerConnection>();
		this.connectionFactory = connectionFactory;
	}


	@Override
	public void run() {
		if (listen) throw new IllegalStateException();
		listen = true;
		log("TcpServer running");
		while (listen) {            
			Socket clientSocket;
			try {
				clientSocket = serverSocket.accept(); 
			} catch (SocketException e) {
				if (listen) e.printStackTrace();
				break;
			} catch (IOException e) {
				e.printStackTrace();
				break;
			}

			IServerConnection c = connectionFactory.create(clientSocket);
			c.setLogAdapter(this);
			if (listen) threadPool.execute(c);
			synchronized (connections) {
				connections.add(c);	
			}
		};
		log("TcpServer stopped");
	}

	public void stop() {
		this.listen = false;

		//Close all connections
		synchronized (connections) {
			for (IServerConnection c : connections) {
				c.close();
			}	
		}

		//Shutdown threads
		threadPool.shutdown(); // Disable new tasks from being submitted
		try {
			// Wait a while for existing tasks to terminate
			if (!threadPool.awaitTermination(5, TimeUnit.SECONDS)) {
				threadPool.shutdownNow(); // Cancel currently executing tasks
				// Wait a while for tasks to respond to being cancelled
				if (!threadPool.awaitTermination(5, TimeUnit.SECONDS))
					System.err.println("Pool did not terminate");
			}
		} catch (InterruptedException ie) {
			// (Re-)Cancel if current thread also interrupted
			threadPool.shutdownNow();
			// Preserve interrupt status
			Thread.currentThread().interrupt();
		}

		// Close socket
		try {
			serverSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public int getPort() {
		return serverSocket.getLocalPort();
	}

	@Override
	public List<IServerConnection> getConnections() {
		return new ArrayList<IServerConnection>(connections);
	}

	@Override
	public void setLogAdapter(ILogAdapter log) {
		this.log = log;
	}

	@Override
	public void log(String message) {
		if (log != null) log.log("[TcpServer Port " + getPort() + "] " + message);
	}

}
