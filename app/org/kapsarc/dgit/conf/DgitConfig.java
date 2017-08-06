package org.kapsarc.dgit.conf;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class DgitConfig {
	private static final Properties conf = new Properties();

	static {
		try {
			conf.load(new FileInputStream(new File("./conf/dgit.conf")));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static String get(String key) {
		return conf.getProperty(key) != null ? conf.getProperty(key) : null;
	}

	public static Properties getConf() {
		return conf;
	}
}