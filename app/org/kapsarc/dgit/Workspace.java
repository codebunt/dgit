package org.kapsarc.dgit;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.Transaction;

public class Workspace {
	private Set<Table> tables = new HashSet<Table>();

	private Schema schema;

	private boolean dirty = false;

	private static HashMap<String, Workspace> map = new HashMap<String, Workspace>();

	public Workspace(Schema schema) throws Exception {
		this.schema = schema;
		Set<String> wstables = fetchFromDb();
		for (String tab : wstables) {
			addTable(tab);
		}
	}

	public static synchronized Workspace get(Schema schema) throws Exception {
		String key = schema.name + "|" + schema.branch;
		if (map.get(key) == null) {
			Workspace s = new Workspace(schema);
			map.put(key, s);
		}
		return map.get(key);
	}
	
	private String getTableNamePrefix() {
		return "u_"+this.schema.name+"_"+this.schema.branch+"_";
	}

	public Workspace addTable(String tabname) throws Exception {
		tabname = tabname.toLowerCase();
		if(!tabname.startsWith(getTableNamePrefix())) {
			tabname = getTableNamePrefix() + tabname;
		}
		tables.add(Table.get(tabname, this));
		this.dirty = true;
		return this;
	}

	public String getBranch() {
		return schema.branch;
	}

	private Set<String> fetchFromDb() throws Exception {
		try {
			EbeanServer ebeanServer = DGitConnection.get().getEbeanServer();
			Transaction transaction = ebeanServer.beginTransaction();
			Connection connection = transaction.getConnection();
			DatabaseMetaData metaData = connection.getMetaData();
			String[] types = { "TABLE" };
			ResultSet columns = metaData.getTables(null, null, getTableNamePrefix() + "%", types);
			Set<String> dbtables = new HashSet<>();
			while (columns.next()) {
				String table = columns.getString(3);
				if (!dbtables.contains(table)) {
					dbtables.add(table);
				}
			}
			transaction.commit();
			return dbtables;
		} catch (Exception e) {
			DGitConnection.get().getEbeanServer().rollbackTransaction();
			throw e;
		} finally {
			DGitConnection.get().getEbeanServer().endTransaction();
		}
	}

	public void save() throws Exception {
		Set<String> todel = new HashSet<>();
		Set<String> fromDb = fetchFromDb();
		Set<String> toadd = getTables();
		for (String tab : fromDb) {
			if (toadd.contains(tab)) {
				toadd.remove(tab);
			} else {
				todel.add(tab);
			}
		}
		modifyTables(toadd, todel);
	}

	private void modifyTables(Set<String> toadd, Set<String> todel) throws Exception {
		EbeanServer ebeanServer = DGitConnection.get().getEbeanServer();
		Transaction transaction = ebeanServer.beginTransaction();
		Connection connection = transaction.getConnection();
		try {
			for (String table : todel) {
				Statement stmt = connection.createStatement();
				String sql = "DROP TABLE " + table;
				stmt.executeUpdate(sql);
				stmt.close();
			}

			for (String table : toadd) {
				Statement stmt = connection.createStatement();
				stmt.executeUpdate("CREATE TABLE " + table + " (_ID TEXT PRIMARY KEY NOT NULL)");
				stmt.close();
			}
			transaction.close();
		} catch (Exception e) {
			e.printStackTrace();
			transaction.rollback(e);
			throw e;
		} finally {
			transaction.end();
		}
	}

	private Set<String> getTables() {
		Set<String> fromWS = new HashSet<>();
		for (Table table : tables) {
			fromWS.add(table.getName());
		}
		return fromWS;
	}
}
