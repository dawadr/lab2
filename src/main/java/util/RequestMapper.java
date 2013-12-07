package util;

import java.io.IOException;
import java.util.HashMap;

import message.Request;
import message.Response;
import message.request.*;
import proxy.IProxy;
import server.IFileServer;

/**
 * Invokes a Request's corresponding method of an IProxy or IFileServer.
 * @author Alex
 *
 */
public class RequestMapper {

	private HashMap<Class<? extends Request>, RequestExecutor> map;
	private boolean isProxy;
	private IProxy proxy;
	private IFileServer fileServer;

	public RequestMapper(IProxy proxy) {
		this.proxy = proxy;
		this.isProxy = true;
		initMap();
	}

	public RequestMapper(IFileServer fileServer) {
		this.fileServer = fileServer;
		this.isProxy = false;
		initMap();
	}

	public Response invoke(Request request) throws IOException {
		RequestExecutor executor = map.get(request.getClass());
		if (executor == null) throw new UnsupportedOperationException();
		return executor.invoke(request);
	}

	private void initMap() {
		this.map = new HashMap<Class<? extends Request>, RequestExecutor>();
		map.put(BuyRequest.class, new BuyRequestExecutor());
		map.put(CreditsRequest.class, new CreditsRequestExecutor());
		map.put(DownloadFileRequest.class, new DownloadFileRequestExecutor());
		map.put(DownloadTicketRequest.class, new DownloadTicketRequestExecutor());
		map.put(InfoRequest.class, new InfoRequestExecutor());
		map.put(ListRequest.class, new ListRequestExecutor());
		map.put(LoginRequest.class, new LoginRequestExecutor());
		map.put(LogoutRequest.class, new LogoutRequestExecutor());
		map.put(UploadRequest.class, new UploadRequestExecutor());
		map.put(VersionRequest.class, new VersionRequestExecutor());		
	}


	private abstract class RequestExecutor {
		public abstract Response invoke(Request request) throws IOException;
	}

	private class BuyRequestExecutor extends RequestExecutor {
		public Response invoke(Request request) throws IOException {
			if (isProxy) return proxy.buy((BuyRequest)request);
			else throw new UnsupportedOperationException();
		}
	}

	private class CreditsRequestExecutor extends RequestExecutor {
		public Response invoke(Request request) throws IOException {
			if (isProxy) return proxy.credits();
			else throw new UnsupportedOperationException();
		}
	}

	private class DownloadFileRequestExecutor extends RequestExecutor {
		public Response invoke(Request request) throws IOException {
			if (!isProxy) return fileServer.download((DownloadFileRequest)request);
			else throw new UnsupportedOperationException();
		}
	}

	private class DownloadTicketRequestExecutor extends RequestExecutor {
		public Response invoke(Request request) throws IOException {
			if (isProxy) return proxy.download((DownloadTicketRequest)request);
			else throw new UnsupportedOperationException();
		}
	}

	private class InfoRequestExecutor extends RequestExecutor {
		public Response invoke(Request request) throws IOException {
			if (!isProxy) return fileServer.info((InfoRequest)request);
			else throw new UnsupportedOperationException();
		}
	}

	private class LoginRequestExecutor extends RequestExecutor {
		public Response invoke(Request request) throws IOException {
			if (isProxy) return proxy.login((LoginRequest)request);
			else throw new UnsupportedOperationException();
		}
	}

	private class ListRequestExecutor extends RequestExecutor {
		public Response invoke(Request request) throws IOException {
			if (isProxy) return proxy.list();
			else return fileServer.list();
		}
	}

	private class LogoutRequestExecutor extends RequestExecutor {
		public Response invoke(Request request) throws IOException {
			if (isProxy) return proxy.logout();
			else throw new UnsupportedOperationException();
		}
	}

	private class UploadRequestExecutor extends RequestExecutor {
		public Response invoke(Request request) throws IOException {
			if (isProxy) return proxy.upload((UploadRequest)request);
			else return fileServer.upload((UploadRequest)request);
		}
	}

	private class VersionRequestExecutor extends RequestExecutor {
		public Response invoke(Request request) throws IOException {
			if (!isProxy) return fileServer.version((VersionRequest)request);
			else throw new UnsupportedOperationException();
		}
	}

}
