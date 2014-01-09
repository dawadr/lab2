package net;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import net.channel.IObjectChannel;
import net.channel.IObjectChannelFactory;


public class TcpServerConnection implements IServerConnection, ILogAdapter {

	private Socket client;
	private IObjectChannelFactory channelFactory;
	private IServerConnectionHandler handler;
	private ILogAdapter log;
	private boolean closed;

	/**
	 * 
	 * @param client
	 * @param handler
	 * @param channelFactory Stellt den Channel zur Verfuegung, ueber den die Verbindung laeuft.
	 */
	public TcpServerConnection(Socket client, IServerConnectionHandler handler, IObjectChannelFactory channelFactory) {
		this.handler = handler;
		handler.setLogAdapter(this);
		this.client = client;
		this.channelFactory = channelFactory;
	}


	@Override
	public void run() {
		log("Connection accepted");

		InputStream in;
		OutputStream out;
		IObjectChannel channel;
		try {
			in = client.getInputStream();
			out = client.getOutputStream();
			channel = channelFactory.create(out, in);
			channel.setLogAdapter(this);
		} catch (IOException e1) {
			throw new RuntimeException(e1);
		}

		try {
			Object inputObject;
			Object outputObject;  
			try {
				while ((inputObject = channel.readObject()) != null) {
					// Process input
					outputObject = handler.process(inputObject);
					channel.writeObject(outputObject);
					// check whether to continue or not
					if (isClosed() || client.isClosed() || handler.breakConnection()) break;
				}
				log("Connection closed");
			} catch (ClassNotFoundException e) {
				outputObject = handler.getIllegalRequestResponse();
				channel.writeObject(outputObject);
				log(e.toString());
				log("Connection closed");
			} finally {
				out.close();
				in.close();
			}
		} catch (EOFException e) {
			log("Connection closed by client");
		} catch (IOException e2) {
			log(e2.getMessage());
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