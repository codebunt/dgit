package org.kapsarc.dgit;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.Transaction;
import com.avaje.ebean.annotation.Transactional;
import com.fasterxml.jackson.databind.JsonNode;

public class Table {
	private HashMap<String, Column> columns = new HashMap<String, Column>();
	private String name;
	private Workspace ws;
	private boolean dirty;
	private static final List<String> RESERVED = new ArrayList<String>();

	static {
		RESERVED.add("from");
		RESERVED.add("to");
		RESERVED.add("user");
		RESERVED.add("group");
	}

	public ArrayList<Column> getColumns() {
		ArrayList<Column> list = new ArrayList<>(columns.values());
		return list;
	}

	HashMap<String, Column> columns() {
		return columns;
	}

	private static HashMap<String, Table> map = new HashMap<String, Table>();

	private Table(String name, Workspace ws) throws Exception {
		this.name = name;
		this.ws = ws;
		load();
	}

	private void load() throws Exception {
		columns = fetchFromDb();
	}

	@Transactional
	private HashMap<String, Column> fetchFromDb() throws Exception {
		Transaction transaction = null;
		try {
			EbeanServer ebeanServer = DGitConnection.get().getEbeanServer();
			transaction = ebeanServer.createTransaction();
			System.out.println("transaction - " + transaction);
			Connection connection = transaction.getConnection();
			DatabaseMetaData metaData = connection.getMetaData();
			ResultSet columns = metaData.getColumns(null, null, this.name, null);
			HashMap<String, Column> dbtables = new HashMap<String, Column>();
			while (columns.next()) {
				String name = columns.getString(4);
				String type = columns.getString(6);
				dbtables.put(name, new Column(name, type));
			}
			transaction.commit();
			return dbtables;
		} catch (Exception e) {
			transaction.rollback();
			throw e;
		} finally {
			transaction.end();
		}
	}

	public void addColumn(Column column) {
		columns.put(column.getName(), column);
		dirty = true;
	}

	public static synchronized Table get(String name, Workspace ws) throws Exception {
		String key = name + "|" + ws.getBranch();
		if (map.get(key) == null) {
			Table s = new Table(name, ws);
			map.put(key, s);
		}
		return map.get(key);
	}

	@Override
	public int hashCode() {
		return (name + "|" + this.ws.getBranch()).hashCode();
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Table) {
			return (name.equals(((Table) other).name) && this.ws.getBranch().equals(((Table) other).ws.getBranch()));
		}
		return false;
	}

	public void save() throws Exception {
		String sql = toSQL();
		if (sql == null || sql.equals("")) {
			return;
		}
		Transaction transaction = Ebean.currentTransaction();
		System.out.println(transaction.isActive());
		Connection connection = transaction.getConnection();
		try {
			Statement stmt = connection.createStatement();
			System.out.println(sql);
			stmt.executeUpdate(sql);
			stmt.close();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	private String toSQL() throws Exception {
		HashMap<String, Column> fromDb = fetchFromDb();
		ArrayList<Column> toadd = getColumns();
		ArrayList<Column> todel = new ArrayList<>();
		Iterator<String> iterator = fromDb.keySet().iterator();
		while (iterator.hasNext()) {
			String colname = iterator.next();
			Column column = fromDb.get(colname);
			if (toadd.contains(column)) {
				toadd.remove(column);
			} else {
				todel.add(column);
			}
		}
		if (todel.size() == 0 && toadd.size() == 0) {
			return null;
		}
		String sql = "ALTER TABLE " + this.name + " ";
		boolean first = true;
		for (Column column : toadd) {
			if (!first) {
				sql += ",";
			}
			sql += "ADD " + column.getName() + " " + column.getType() + " ";
			first = false;
		}
		for (Column column : todel) {
			if (!first) {
				sql += ",";
			}
			sql += "DROP " + column.getName();
			first = false;
		}
		return sql;
	}

	public void save(JsonNode node) throws Exception {
		Connection connection = this.ws.getConnection();
		try {
			ArrayList<Row> rows = new ArrayList<>();
			if (node.isArray()) {
				for (int i = 0; i < node.size(); i++) {
					rows.add(new Row(node.get(i), this));
					if (rows.size() > 1000) {
						saveRecords(rows, connection);
						rows = new ArrayList<>();
					}
				}
			} else {
				rows.add(new Row(node, this));
			}
			saveRecords(rows , connection);
			connection.commit();
		} catch (Exception e) {
			e.printStackTrace();
			connection.rollback();
			throw e;
		} finally {
			connection.close();
		}
	}

	private void saveRecords(ArrayList<Row> rows, Connection connection) throws Exception {
		Statement statement = connection.createStatement();
		for (int i = 0; i < rows.size(); i++) {
			statement.addBatch(rows.get(i).toSql());
		}
		int[] count = statement.executeBatch();
	}

	public static String unReserve(String col) {
		if (RESERVED.contains(col)) {
			return col = "_" + col;
		}
		return col;
	}

	public String getName() {
		return this.name;
	}

	public void removeColumn(String colname) {
		columns.remove(colname);
	}
}
