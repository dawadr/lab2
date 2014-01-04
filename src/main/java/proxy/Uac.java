package proxy;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import model.User;
import model.UserInfo;

/**
 * User Account Control manages the users and provides methods to log them in and out and to modify their credits balance.
 * @author Alex
 *
 */
public class Uac {

	private HashMap<String, UserSessions> users;

	public Uac(List<User> users) {
		this.users = new HashMap<String, UserSessions>();
		for (User u: users) {
			this.users.put(u.getName(), new UserSessions(u));
		}
	}

	/**
	 * Logs in a user
	 * @param username the user's name
	 * @param password the password
	 * @return the successfully logged in user
	 * @throws UacException if login failed (wrong username or password).
	 */
	public synchronized User login(String username, String password, ProxyHandler session) throws UacException {
		if (username == null || password == null || username.isEmpty() || password.isEmpty()) throw new IllegalArgumentException();
		if (users.containsKey(username)) {
			UserSessions u = users.get(username);
			if (password.equals(u.getUser().getPassword())) {
				u.loggedIn(session);
				return u.getUser();
			}  
		}
		throw new UacException("Invalid username or password.");
	}
	
	/**
	 * Logs out a user
	 * @param user
	 */
	public synchronized void logout(User user, ProxyHandler session) {
		if (users.keySet().contains(user.getName())) {
			users.get(user.getName()).loggedOut(session);;
		}
	}

	/**
	 * Increases the user's credits balance.
	 * @param user
	 * @param credits
	 */
	public void increaseCredits(User user, long credits) {
		synchronized (user) {
			user.setCredits(user.getCredits() + credits);
		}
	}

	/**
	 * Charges the credits from the user if the user has enough credits.
	 * @param credits The credits to charge
	 * @return false if the user does not have enough credits; else true
	 */
	public boolean checkAndChargeCredits(User user, long credits) {	
		synchronized (user) {
			if (user.getCredits() < credits) return false;
			user.setCredits(user.getCredits() - credits);
			return true;
		}	
	}

	/**
	 * Lists all users registered by the UAC.
	 * @return a list of UserInfo objects
	 * @throws UacException
	 */
	public synchronized List<UserInfo> getUserList() {
		ArrayList<UserInfo> l = new ArrayList<UserInfo>();
		for (UserSessions u: users.values()) {
			l.add(u.getUser().toUserInfo());
		}
		return l;
	}

	/**
	 * Stores the 'sessions' objects the user is connected to and controls the user's online status.
	 * @author Alex
	 *
	 */
	private class UserSessions {
		private User user;
		private List<ProxyHandler> sessions;

		public UserSessions(User user) {
			this.user = user;
			this.sessions = new ArrayList<ProxyHandler>();
		}

		public User getUser() {
			return user;
		}

		public void loggedIn(ProxyHandler session) {
			if (!sessions.contains(session)) sessions.add(session);	
			evalOnlineStatus();
		}

		public void loggedOut(ProxyHandler session) {
			if (sessions.contains(session)) sessions.remove(session);	
			evalOnlineStatus();
		}

		private void evalOnlineStatus() {
			user.setOnline(sessions.size() > 0);
		}
	}

}
