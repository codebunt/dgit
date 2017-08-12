package org.kapsarc.dgit;

import java.util.HashMap;
import java.util.List;

import org.kapsarc.dgit.ebean.TableModel;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.Query;
import com.fasterxml.jackson.databind.JsonNode;

public class VersionedTable extends VersionedObject {
	private HashMap<String, Column> columns = new HashMap<String, Column>();

	public HashMap<String, Column>  getColumns() {
		return columns;
	}

	private static HashMap<String, VersionedTable> map = new HashMap<String, VersionedTable>();
	
	private VersionedTable(String name, String branch) throws Exception {
		this.name = name;
		this.branch = branch;
		load();
	}

	private void load() throws Exception {
		fetchFromHistory();
	}

	private void fetchFromHistory() throws Exception {
		try {
			EbeanServer ebeanServer = DGitConnection.get().getEbeanServer();
			Query<TableModel> query = ebeanServer.createQuery(TableModel.class);
			List<TableModel> list = query.where().eq("name", name).eq("branch", branch).orderBy("ts").setMaxRows(1).findList();
			if(list.size() == 0) {
				throw new Exception("Invalid branch or schema name");
			}
			JsonNode content = list.get(0).content;
			if (content.isArray()) {
			    for (final JsonNode objNode : content) {
			        System.out.println(objNode.asText());
			        addColumn(new Column(objNode.get("name").asText(), objNode.get("type").asText()));
			    }
			} else {
				throw new Exception("Content should be an array of string (table names)");
			}
		} catch (Exception e) {
			DGitConnection.get().getEbeanServer().rollbackTransaction();
			throw e;
		} finally {
			DGitConnection.get().getEbeanServer().endTransaction();
		}
	}
	private void addColumn(Column column) {
		columns.put(column.getName(), column);
		dirty = true;
	}

	public static synchronized VersionedTable get(String name, String branch) throws Exception {
		String key = name + "|" + branch;
		if (map.get(key) == null) {
			VersionedTable s = new VersionedTable(name, branch);
			map.put(name, s);
		}
		return map.get(key);
	}

	@Override
	public int hashCode() {
		return (name+"|"+branch).hashCode();
	}
	
	@Override
	public boolean equals(Object other) {
		if(other instanceof VersionedTable) {
			return (name.equals(((VersionedTable) other).name) && branch.equals(((VersionedTable) other).branch));
		}
		return false;
	}
}
