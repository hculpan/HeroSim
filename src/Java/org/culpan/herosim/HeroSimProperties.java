package org.culpan.herosim;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

public class HeroSimProperties {
	public final static String DATA_DIR = "data-dir";
	
	private final static Logger logger = Logger.getLogger(HeroSimProperties.class);
	
	protected final static File DEFAULT_PROP_FILE = new File(System.getProperty("user.home"), ".herosim");
	
	protected static Properties props = new Properties();
	
	static {
		try {
			props.load(new FileInputStream(DEFAULT_PROP_FILE));
		} catch (Throwable e) {
			logger.error(e);
		}
	}
	
	protected HeroSimProperties() {
	}
	
	public static void save(File file) {
		logger.debug("Saving properties to '" + file.getPath() + "'");
		try {
			props.store(new FileOutputStream(file), "HeroSim");
		} catch (Throwable e) {
			logger.error(e);
		}
	}
	
	public static void save() {
		save(DEFAULT_PROP_FILE);
	}
	
	public static String getProperty(String key) {
		return props.getProperty(key);
	}
	
	public static void setProperty(String key, String value) {
		props.setProperty(key, value);
	}
	
	public static int getIntProperty(String key) {
		return getIntProperty(key, 0);
	}
	
	public static int getIntProperty(String key, int defaultValue) {
		if (props.containsKey(key)) {
			return Integer.parseInt(getProperty(key));
		} else {
			return defaultValue;
		}
	}
	
	public static void setIntProperty(String key, int value) {
		setProperty(key, Integer.toString(value));
	}
	
	public static boolean getBooleanProperty(String key) {
		return getBooleanProperty(key, false);
	}
	
	public static boolean getBooleanProperty(String key, boolean defaultValue) {
		if (props.containsKey(key)) {
			return Boolean.valueOf(getProperty(key)).booleanValue();
		} else {
			return defaultValue;
		}
	}
	
	public static void setBooleanProperty(String key, boolean value) {
		setProperty(key, Boolean.toString(value));
	}
	
	public static boolean hasProperty(String key) {
		return props.containsKey(key);
	}
}
