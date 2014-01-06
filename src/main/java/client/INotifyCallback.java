package client;

import java.io.Serializable;

import message.Response;

public interface INotifyCallback extends Serializable {

	public void notify(Response r);
	
}
