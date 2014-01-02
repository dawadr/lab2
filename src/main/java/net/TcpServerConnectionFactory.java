package net;

import java.net.Socket;

import net.channel.IObjectChannelFactory;

public class TcpServerConnectionFactory implements IServerConnectionFactory {

	private IServerConnectionHandlerFactory handlerFactory;
	private IObjectChannelFactory channelFactory;
	
	public TcpServerConnectionFactory(IServerConnectionHandlerFactory handlerFactory, IObjectChannelFactory channelFactory) {
		this.handlerFactory = handlerFactory;
		this.channelFactory = channelFactory;
	}

	@Override
	public IServerConnection create(Socket socket) {
		return new TcpServerConnection(socket, handlerFactory.create(), channelFactory);
	}

}
