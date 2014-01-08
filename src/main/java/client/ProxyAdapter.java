package client;

import java.io.IOException;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;

import net.IConnection;
import net.TcpConnection;
import proxy.IProxy;
import util.KeyProvider;
import message.Response;
import message.request.*;
import message.response.FailedResponse;
import message.response.LoginResponse;
import message.response.MessageResponse;

/**
 * Provides methods processed by a remote proxy.
 * @author Alex
 *
 */
public class ProxyAdapter implements IProxy {

	private IConnection conn;
	private String host;
	private int port;
	private KeyProvider keyProvider;
	private PublicKey publicKey;

	/**
	 * 
	 * @param host
	 * @param port
	 * @param keyProvider Stellt die Private Keys der User zur Verfuegung
	 * @param publicKey Public Key des Proxys
	 */
	public ProxyAdapter(String host, int port, KeyProvider keyProvider, PublicKey publicKey) {
		this.host = host;
		this.port = port;
		this.keyProvider = keyProvider;
		this.publicKey = publicKey;
	}

	@Override
	public Response login(LoginRequest request) throws IOException {
		if (connected()) conn.close();

		try {
			String username = request.getUsername();
			String password = request.getPassword();
			PrivateKey privateKey;
			try {
				// private key mit passwort auslesen
				privateKey = keyProvider.getPrivateKey(username, password);
			} catch (IOException e) {
				throw new IOException("Wrong username or password.", e);
			}

			// channelFactory für die Connection mit den keys initialisieren
			SecureProxyChannelFactory channelFactory = new SecureProxyChannelFactory(username, privateKey, publicKey);
			conn = new TcpConnection(host, port, channelFactory);

			Response r = conn.send(request);
			if (!(r instanceof LoginResponse)) throw new IOException("Illegal response by proxy.");
			return (LoginResponse)r;	
		} catch (IOException e) {
			if (conn != null) conn.close();
			return new FailedResponse("Authentication failed: " + e.getMessage());
		}
	}

	@Override
	public Response credits() throws IOException {
		if (!connected()) return new MessageResponse("Not connected to server. Log in first.");
		Response r = conn.send(new CreditsRequest());
		return r;
	}

	@Override
	public Response buy(BuyRequest credits) throws IOException {
		if (!connected()) return new MessageResponse("Not connected to server. Log in first.");
		Response r = conn.send(credits);
		return r;
	}

	@Override
	public Response list() throws IOException {
		if (!connected()) return new MessageResponse("Not connected to server. Log in first.");
		Response r = conn.send(new ListRequest());
		return r;
	}

	@Override
	public Response download(DownloadTicketRequest request) throws IOException {
		if (!connected()) return new MessageResponse("Not connected to server. Log in first.");
		Response r = conn.send(request);
		return r;
	}

	@Override
	public MessageResponse upload(UploadRequest request) throws IOException {
		if (!connected()) return new MessageResponse("Not connected to server. Log in first.");
		Response r = conn.send(request);
		if (!(r instanceof MessageResponse)) throw new IOException("Illegal response by proxy.");
		return (MessageResponse)r;	
	}

	@Override
	public MessageResponse logout() throws IOException {
		if (!connected()) return new MessageResponse("Not connected to server. Log in first.");
		Response r = conn.send(new LogoutRequest());
		conn.close();
		if (!(r instanceof MessageResponse)) throw new IOException("Illegal response by proxy.");
		return (MessageResponse)r;	
	}

	private boolean connected() {
		return (conn != null && !conn.isClosed());
	}

}
