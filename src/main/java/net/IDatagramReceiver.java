package net;

/**
 * Listens for incoming Datagram (UDP) packages.
 * @author Alex
 *
 */
public interface IDatagramReceiver extends Runnable {

	@Override
	public void run();
	
	public void close();
	
	public void addListener(IDatagramPacketListener listener);

	public void removeListener(IDatagramPacketListener listener);

	public void setLogAdapter(ILogAdapter log);
	
	public int getPort();

}
