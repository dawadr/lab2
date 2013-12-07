package proxy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import model.User;
import util.ConfigEx;

/**
 * Reads the configuration from a {@code .properties} file defining users.
 */
public final class UserConfig extends ConfigEx {

	public UserConfig(String name) {
		super(name);
	}

	public List<String> getUserNames() throws IOException {
		ArrayList<String> names = new ArrayList<String>();
		for (String key: this.getKeys()) {
			if (!key.contains(".")) throw new IOException("Invalid user config file");
			String userPrefix = key.substring(0, key.indexOf("."));
			if (userPrefix.length() < 1) throw new IOException("Invalid user config file");
			if (!names.contains(userPrefix)) names.add(userPrefix);
		}
		return names;
	}

	public List<User> getUsers() throws IOException {
		ArrayList<User> users = new ArrayList<User>();
		for (String name: getUserNames()) {
			int credits = this.getInt(name + ".credits");
			String password = this.getString(name + ".password");
			User u = new User(name, password, credits, false, 0);
			users.add(u);
		}
		return users;
	}
}
