package net;

/**
 * Handles incoming objects from an IServerConnection.
 * @author Alex
 *
 */
public interface IServerConnectionHandler {
	
	public Object process(Object inputObject);

	public void close();

	public Object getIllegalRequestResponse();
	
	public boolean breakConnection();
	
	public void setLogAdapter(ILogAdapter log);
	
}
