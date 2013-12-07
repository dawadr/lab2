package util;

import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Reads the configuration from a {@code .properties} file and provides a method to retrieve the keys.
 */
public class ConfigEx {

	private final ResourceBundle bundle;

	/**
	 * Creates an instance of Config which reads configuration data form
	 * {@code .properties} file with given name found in classpath.
	 *
	 * @param name the name of the .properties file
	 */
	public ConfigEx(final String name) {
		this.bundle = ResourceBundle.getBundle(name);
	}

    public List<String> getKeys() {
    	return Collections.list(this.bundle.getKeys());
    }
    
	/**
	 * Returns the value as String for the given key.
	 *
	 * @param key the property's key
	 * @return String value of the property
	 * @see ResourceBundle#getString(String)
	 */
	public String getString(String key) {
		return this.bundle.getString(key);
	}

	/**
	 * Returns the value as {@code int} for the given key.
	 *
	 * @param key the property's key
	 * @return int value of the property
	 * @throws NumberFormatException if the String cannot be parsed to an Integer
	 */
	public int getInt(String key) {
		return Integer.parseInt(getString(key));
	}
}
