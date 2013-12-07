package model;

import java.net.InetAddress;
import java.util.Date;

public class FileServer {

	private InetAddress address;
	private int port;
	private long usage;
	private boolean online;
	private Date lastAlive;

	public FileServer(InetAddress address, int port, long usage, boolean online, Date lastAlive) {
		this.address = address;
		this.port = port;
		this.usage = usage;
		this.online = online;
		this.lastAlive = lastAlive;
	}


	public InetAddress getAddress() {
		return address;
	}

	public void setAddress(InetAddress address) {
		this.address = address;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public long getUsage() {
		return usage;
	}

	public void setUsage(long usage) {
		this.usage = usage;
	}

	public boolean isOnline() {
		return online;
	}

	public void setOnline(boolean online) {
		this.online = online;
	}

	public Date getLastAlive() {
		return lastAlive;
	}

	public void setLastAlive(Date lastAlive) {
		this.lastAlive = lastAlive;
	}


	@Override
	public boolean equals(Object o) {
		if (o == null || !(o.getClass() == this.getClass())) return false;
		if (o == this) return true;
		FileServer fs2 = (FileServer)o;
		if (fs2.getAddress().equals(this.getAddress()) && fs2.getPort() == this.getPort()) return true;
		return false;
	}

	@Override
	public String toString() {
		return String.format("%1$-15s %2$-5d %3$-7s %4$13d",
				getAddress().getHostAddress(), getPort(),
				isOnline() ? "online" : "offline", getUsage());
	}

	public FileServerInfo toFileServerInfo() {
		return new FileServerInfo(address, port, usage, online);
	}

}
