package org.kapsarc.dgit;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.kapsarc.dgit.conf.DgitConfig;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.Transaction;

import play.db.Database;
import play.db.Databases;

public class Workspace {
	private Set<Table> tables = new HashSet<Table>();

	private Schema schema;

	private boolean dirty = false;

	private Database database;

	private static HashMap<String, Workspace> map = new HashMap<String, Workspace>();

	private Workspace(Schema schema) throws Exception {
		initializePool();
		this.schema = schema;
		Set<String> wstables = fetchFromDb();
		for (String tab : wstables) {
			addTable(tab);
		}
	}

	private void initializePool() {
		Map<String, String> pm = new HashMap<>();
		pm.put("user",DgitConfig.get("pg.uname"));
		pm.put("password",DgitConfig.get("pg.pass"));
		pm.put("connectionTestQuery", "SELECT 1");
		this.database = Databases.createFrom(getDBName(), DgitConfig.get("pg.driver") , DgitConfig.get("pg.url"), pm);
	}

	private void createdb() throws Exception {
		Connection conn = null;
		Statement stmt = null;
		final String DB_URL = DgitConfig.get("pg.url");
		try {
			Class.forName(DgitConfig.get("pg.driver"));
			System.out.println("Connecting to database...");
			conn = DriverManager.getConnection(DB_URL, DgitConfig.get("pg.uname"), DgitConfig.get("pg.pass"));
			System.out.println("Creating database...");
			stmt = conn.createStatement();
			String sql = "CREATE DATABASE " + getDBName();
			stmt.executeUpdate(sql);
			System.out.println("Database created successfully...");
		} catch (Exception se) {
			se.printStackTrace();
			throw se;
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException se2) {
			} // nothing we can do
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			} // end finally try
		} // end try

	}

	private String getDBName() {
		return schema.name + "_" + schema.branch.getName();
	}

	public static synchronized Workspace get(Schema schema) throws Exception {
		String key = schema.name + "|" + schema.branch.getId();
		if (map.get(key) == null) {
			Workspace s = new Workspace(schema);
			map.put(key, s);
		}
		return map.get(key);
	}

	private String getTableNamePrefix() {
		return "u_" + this.schema.name + "_" + this.schema.branch.getName() + "_";
	}

	public Table addTable(String tabname) throws Exception {
		tabname = tabname.toLowerCase();
		if (!tabname.startsWith(getTableNamePrefix())) {
			tabname = getTableNamePrefix() + tabname;
		}
		Table table = Table.get(tabname, this);
		tables.add(table);
		this.dirty = true;
		return table;
	}

	public String getBranch() {
		return schema.branch.getId();
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
			System.out.println(transaction.isActive());
			for (Table tab : this.tables) {
				tab.save();
				System.out.println(transaction.isActive());
			}
			transaction.commit();
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

	public Connection getConnection() {
		return this.database.getConnection();
	}

	public Database getDatabase() {
		return database;
	}
}
