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

import net.channel.Base64ChannelDecorator;
import net.channel.IChannel;
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

	private IChannel channel;

	public TcpConnection(String host, int port) {
		this.host = host;
		this.port = port;
		channel = new Base64ChannelDecorator(new ObjectChannel());
	}


	private synchronized void connect() throws IOException {
		if (sock != null && sock.isConnected() && !sock.isClosed()) return;
		try {
			sock = new Socket(host, port);
		} catch (ConnectException e) {
			throw new ConnectException("Connection refused: " + host + " on port " + port);
		}	
				out = sock.getOutputStream();
				in = sock.getInputStream();
		channel.initialize(out, in);
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
			throw new IOException("Illegal response by proxy.", e);
		} catch (EOFException e2) {
			return new RefuseResponse("Server disconnected.");
			//			throw new IOException("Server closed connection.", e2);
		}
		if (response instanceof Response) {
			return (Response) response;
		} else {
			throw new IOException("Illegal response by proxy.");
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
		return sock.isClosed();
	}

}
