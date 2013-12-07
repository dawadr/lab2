package model;

/**
 * Contains information about a user account.
 */
public class User implements Comparable<User> {

	private String name;
	private long credits;
	private boolean online;
	private int sessions;
	private String password; 

	public User(String name, String password, long credits, boolean online, int sessions) {
		this.name = name;
		this.password = password;
		this.credits = credits;
		this.online = online;
		this.sessions = sessions;
	}


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getCredits() {
		return credits;
	}

	public void setCredits(long credits) {
		this.credits = credits;
	}

	public boolean isOnline() {
		return online;
	}

	public void setOnline(boolean online) {
		this.online = online;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int getSessions() {
		return sessions;
	}

	public void setSessions(int sessions) {
		this.sessions = sessions;
	}

	@Override
	public String toString() {
		return String.format("%1$-15s %2$-7s %3$13d", name, isOnline() ? "online" : "offline", credits);
	}

	@Override
	public int compareTo(User o) {
		return getName().compareTo(o.getName());
	}

	public UserInfo toUserInfo() {
		return new UserInfo(getName(), getCredits(), isOnline()); 
	}

}
