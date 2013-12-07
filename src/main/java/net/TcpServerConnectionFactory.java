package net;

import java.net.Socket;

public class TcpServerConnectionFactory implements IServerConnectionFactory {

	private IServerConnectionHandlerFactory handlerFactory;
	
	public TcpServerConnectionFactory(IServerConnectionHandlerFactory handlerFactory) {
		this.handlerFactory = handlerFactory;
	}

	@Override
	public IServerConnection create(Socket socket) {
		return new TcpServerConnection(socket, handlerFactory.create()) {
		};
	}

}
