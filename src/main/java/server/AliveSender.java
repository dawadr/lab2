package server;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import net.IDatagramSender;
import net.ILogAdapter;

/**
 * Sends Alive-messages with a specific message.
 * @author Alex
 *
 */
public class AliveSender {

	private IDatagramSender datagramSender;
	private int interval;
	private Timer t;
	private TimerTask task;
	private boolean active;
	private ILogAdapter log;
	private String msg;

	public AliveSender(final IDatagramSender datagramSender, int intervalInMs, final String message) {
		this.msg = message;
		this.datagramSender = datagramSender;
		this.interval = intervalInMs;
		task = new TimerTask() {
			public void run() {
				try {
					datagramSender.send(message.getBytes());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		};
	}


	public void activate() {
		t = new Timer();
		t.scheduleAtFixedRate(task, 0, interval);
		active = true;
		log("Sending '" + msg  + "' at rate " + interval + "ms");
	}

	public void deactivate() throws IOException {
		t.cancel();
		active = false;
		datagramSender.close();
		log("Deactivated");
	}

	public boolean isActive() {
		return active;
	}

	public void setLogAdapter(ILogAdapter log) {
		this.log = log;
	}
	
	private void log(String message) {
		if (log != null) log.log("[AliveSender Port " + datagramSender.getPort() + "] " + message);
	}
	
}
