package net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class DatagramSender implements IDatagramSender {

	DatagramSocket socket;
	InetAddress client;
	int port;

	public DatagramSender(String client, int port) throws IOException {
		this.port = port;
		init();
		this.client = InetAddress.getByName(client);
	} 

	private void init() throws SocketException {
		this.socket = new DatagramSocket();
	}

	@Override
	public void send(byte[] data) throws IOException {
		if (socket.isClosed()) init();
		DatagramPacket packet = new DatagramPacket(data, data.length, client, port);
		socket.send(packet);
	}

	@Override
	public void close() throws IOException {
		socket.close();
	}

	@Override
	public InetAddress getHost() {
		return client;
	}

	@Override
	public int getPort() {
		return this.port;
	}

	@Override
	public boolean isClosed() {
		return (socket.isClosed());
	}

}
