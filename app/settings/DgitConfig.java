package settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class DgitConfig {

	private static final Properties props = new Properties();

	public static void load(File f) throws Exception {
		props.load(new FileInputStream(f));
	}

	public static void load(InputStream is) throws Exception {
		props.load(is);
	}

	public static String getKey() {
		return props.getProperty("key");
	}
	
	public static String getSalt() {
		return props.getProperty("salt");
	}
	
	public static String getAlgorithm() {
		return props.getProperty("algo");
	}
	
	public static String getDelimiter() {
		return props.getProperty("delim");
	}

	public static String get(String key) {
		return props.getProperty(key);
	}


}
