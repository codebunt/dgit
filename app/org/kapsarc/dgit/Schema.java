package org.kapsarc.dgit;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.Transaction;

public class Schema {
	private String name;

	private String owner;

	private String branch;

	private Set<String> tables = new HashSet<String>();

	private boolean dirty = false;

	private static HashMap<String, Schema> map = new HashMap<String, Schema>();

	private Schema(String name, String owner) {
		this.name = name;
		this.owner = owner;
		load();
	}

	public static synchronized Schema get(String name, String owner) {
		String key = name + "|" + owner;
		if (map.get(key) == null) {
			Schema s = new Schema(name, owner);
			map.put(name, s);
		}
		return map.get(key);
	}

	public Schema addTable(String tabname) {
		tables.add(tabname);
		this.dirty = true;
		return this;
	}

	private void load() {
		fetchFromDb();
	}

	private void fetchFromDb() {
		try {
			EbeanServer ebeanServer = DGitConnection.get().getEbeanServer();
			Transaction transaction = ebeanServer.beginTransaction();
			Connection connection = transaction.getConnection();
			DatabaseMetaData metaData = connection.getMetaData();
			String[] types = { "TABLE" };
			ResultSet columns = metaData.getTables(null, null, "%", types);
			while (columns.next()) {
				String table = columns.getString(3);
				if(!tables.contains(table)) {
					tables.add(table);
				}
			}
		} catch (Exception e) {
			DGitConnection.get().getEbeanServer().rollbackTransaction();
			e.printStackTrace();
		} finally {
			DGitConnection.get().getEbeanServer().endTransaction();
		}
	}

}
