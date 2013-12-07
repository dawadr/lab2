package server;

import util.FileManager;
import net.IServerConnectionHandler;
import net.IServerConnectionHandlerFactory;

public class FileServerHandlerFactory implements IServerConnectionHandlerFactory {
	
	private FileManager fileManager;

	public FileServerHandlerFactory(FileManager fileManager) {
		this.fileManager = fileManager;
	}

	@Override
	public IServerConnectionHandler create() {
		IServerConnectionHandler h = new FileServerHandler(fileManager);
		return h;
	}

}
