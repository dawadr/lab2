package proxy;

import net.IServerConnectionHandler;
import net.IServerConnectionHandlerFactory;

public class ProxyHandlerFactory implements IServerConnectionHandlerFactory {

	private Uac uac;
	private FileServerManager serverManager;

	public ProxyHandlerFactory(Uac uac, FileServerManager serverManager) {
		this.uac = uac;
		this.serverManager = serverManager;
	}

	@Override
	public IServerConnectionHandler create() {
		IServerConnectionHandler h = new ProxyHandler(uac, serverManager);
		return h;
	}

}
