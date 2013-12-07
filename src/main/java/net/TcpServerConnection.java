package net;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public abstract class TcpServerConnection implements IServerConnection, ILogAdapter {

	private Socket client;
	private IServerConnectionHandler handler;
	private ILogAdapter log;
	private boolean closed;

	public TcpServerConnection(Socket client, IServerConnectionHandler handler) {
		this.handler = handler;
		handler.setLogAdapter(this);
		this.client = client;
	}


	@Override
	public void run() {
		log("Connection accepted");
		try {
			ObjectInputStream in = new ObjectInputStream(client.getInputStream());
			ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());

			Object inputObject;
			Object outputObject;  
			try {
				while ((inputObject = in.readObject()) != null) {
					// Process input
					outputObject = handler.process(inputObject);
					out.writeObject(outputObject);
					// check whether to continue or not
					if (isClosed() || client.isClosed() || handler.breakConnection()) break;
				}
				log("Connection closed");
			} catch (ClassNotFoundException e) {
				outputObject = handler.getIllegalRequestResponse();
				out.writeObject(outputObject);
				log(e.toString());
				log("Connection closed");
			} finally {
				out.close();
				in.close();
			}
		} catch (EOFException e) {
			log("Connection closed by client");
		} catch (IOException e2) {
			log("Connection closed");
		} finally {
			close();
		}
	}

	@Override
	public void close()  {
		this.closed = true;	
		try {
			handler.close();
			if (!client.isClosed()) client.shutdownOutput();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {		
			try {	
				client.close();	
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void setLogAdapter(ILogAdapter log) {
		this.log = log;
	}

	public Socket getSocket() {
		return client;
	}

	public boolean isClosed() {
		return this.closed;
	}

	public void log(String message) {
		if (log != null) log.log("[" + client.toString() + "] " + message);
	}

}