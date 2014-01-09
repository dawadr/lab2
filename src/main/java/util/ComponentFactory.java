package util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cli.Shell;
import client.Client;
import client.IClientCli;
import proxy.IProxyCli;
import proxy.Proxy;
import proxy.UserConfig;
import server.FileServer;
import server.IFileServerCli;

/**
 * Provides methods for starting an arbitrary amount of various components.
 */
public class ComponentFactory {
	
	
	private ExecutorService threadPool = Executors.newCachedThreadPool();
	
	/**
	 * Creates and starts a new client instance using the provided {@link Config} and {@link Shell}.
	 *
	 * @param config the configuration containing parameters such as connection info
	 * @param shell  the {@code Shell} used for processing commands
	 * @return the created component after starting it successfully
	 * @throws Exception if an exception occurs
	 */
	public IClientCli startClient(Config config, Shell shell) throws Exception {
		Client client = new Client(config, new Config("mc"), shell);
		threadPool.execute(client);
		return client.getCli();
	}

	/**
	 * Creates and starts a new proxy instance using the provided {@link Config} and {@link Shell}.
	 *
	 * @param config the configuration containing parameters such as connection info
	 * @param shell  the {@code Shell} used for processing commands
	 * @return the created component after starting it successfully
	 * @throws Exception if an exception occurs
	 */
	public IProxyCli startProxy(Config config, Shell shell) throws Exception {
		Proxy proxy = new Proxy(config, new UserConfig("user"), new Config("mc"), shell, "12345");
		threadPool.execute(proxy);
		return proxy.getCli();
	}

	/**
	 * Creates and starts a new file server instance using the provided {@link Config} and {@link Shell}.
	 *
	 * @param config the configuration containing parameters such as connection info
	 * @param shell  the {@code Shell} used for processing commands
	 * @return the created component after starting it successfully
	 * @throws Exception if an exception occurs
	 */
	public IFileServerCli startFileServer(Config config, Shell shell) throws Exception {
		FileServer fileServer = new FileServer(config, shell);
		threadPool.execute(fileServer);
		return fileServer.getCli();
	}
}
