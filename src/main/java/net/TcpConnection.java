package net;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;

import net.channel.Base64Channel;
import net.channel.ByteChannel;
import net.channel.IChannel;
import net.channel.IObjectChannel;
import net.channel.IObjectChannelFactory;
import net.channel.ObjectChannel;

import message.Request;
import message.Response;
import message.response.RefuseResponse;

public class TcpConnection implements IConnection {

	private String host;
	private int port;

	private Socket sock;
	private OutputStream out;
	private InputStream in;

	private IObjectChannelFactory channelFactory;
	private IObjectChannel channel;

	/**
	 * 
	 * @param host
	 * @param port
	 * @param channelFactory Stellt den Channel zur Verfuegung, ueber den die Verbindung laeuft.
	 */
	public TcpConnection(String host, int port, IObjectChannelFactory channelFactory) {
		this.host = host;
		this.port = port;
		this.channelFactory = channelFactory;
	}


	private synchronized void connect() throws IOException {
		if (sock != null && sock.isConnected() && !sock.isClosed() && !sock.isOutputShutdown()) return;
		try {
			sock = new Socket(host, port);
		} catch (ConnectException e) {
			throw new ConnectException("Connection refused: " + host + " on port " + port);
		}	
		out = sock.getOutputStream();
		in = sock.getInputStream();
		channel = channelFactory.create(out, in);
	}

	public synchronized Response send(Request req) throws IOException {
		connect();
		try {
			channel.writeObject(req);
		} catch (IOException ex) {	
			close();
			connect();
			channel.writeObject(req);
		}

		Object response;
		try {
			response = channel.readObject();
		} catch (ClassNotFoundException e) {
			throw new IOException("Illegal response by server.", e);
		} catch (EOFException e2) {
			return new RefuseResponse("Server disconnected.");
			//			throw new IOException("Server closed connection.", e2);
		}
		if (response instanceof Response) {
			return (Response) response;
		} else {
			throw new IOException("Illegal response by server.");
		}
	}


	@Override
	public synchronized void close() throws IOException {
		if (in != null) in.close();
		if (out != null && !sock.isClosed()) out.close();
		if (sock != null) sock.close();
	}


	@Override
	public InetAddress getHost() {
		return sock.getInetAddress();
	}


	@Override
	public int getPort() {
		return port;
	}


	@Override
	public synchronized boolean isClosed() {
		return sock == null || sock.isClosed();
	}

}
