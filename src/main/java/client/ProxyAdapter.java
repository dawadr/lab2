package client;

import java.io.IOException;

import net.IConnection;
import proxy.IProxy;
import message.Response;
import message.request.*;
import message.response.LoginResponse;
import message.response.MessageResponse;

/**
 * Provides methods processed by a remote proxy.
 * @author Alex
 *
 */
public class ProxyAdapter implements IProxy {

	private IConnection conn;
	public ProxyAdapter(IConnection connection) {
		this.conn = connection;
	}

	@Override
	public LoginResponse login(LoginRequest request) throws IOException {
		Response r = conn.send(request);
		if (!(r instanceof LoginResponse)) throw new IOException("Illegal response by proxy.");
		return (LoginResponse)r;	
	}

	@Override
	public Response credits() throws IOException {
		Response r = conn.send(new CreditsRequest());
		return r;
	}

	@Override
	public Response buy(BuyRequest credits) throws IOException {
		Response r = conn.send(credits);
		return r;
	}

	@Override
	public Response list() throws IOException {
		Response r = conn.send(new ListRequest());
		return r;
	}

	@Override
	public Response download(DownloadTicketRequest request) throws IOException {
		Response r = conn.send(request);
		return r;
	}

	@Override
	public MessageResponse upload(UploadRequest request) throws IOException {
		Response r = conn.send(request);
		if (!(r instanceof MessageResponse)) throw new IOException("Illegal response by proxy.");
		return (MessageResponse)r;	
	}

	@Override
	public MessageResponse logout() throws IOException {
		Response r = conn.send(new LogoutRequest());
		if (!(r instanceof MessageResponse)) throw new IOException("Illegal response by proxy.");
		return (MessageResponse)r;	
	}

}
