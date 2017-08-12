package org.kapsarc.dgit;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.kapsarc.dgit.ebean.SchemaModel;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.Query;
import com.fasterxml.jackson.databind.JsonNode;

public class Schema extends VersionedObject {

	private Set<VersionedTable> tables = new HashSet<VersionedTable>();

	private static HashMap<String, Schema> map = new HashMap<String, Schema>();

	private Schema(String name, String branch) throws Exception {
		this.name = name;
		this.branch = branch;
		verifyBranch();
		load();
	}
	
	private void verifyBranch() {
		
	}

	public static synchronized Schema get(String name, String branch) throws Exception {
		String key = name + "|" + branch;
		if (map.get(key) == null) {
			Schema s = new Schema(name, branch);
			map.put(key, s);
		}
		return map.get(key);
	}

	public Schema addTable(String tabname) throws Exception {
		tables.add(VersionedTable.get(tabname, branch));
		this.dirty = true;
		return this;
	}

	private void load() throws Exception {
		Set<String> fromvtable = fetchFromVersionTable();
		for (String tabname : fromvtable) {
			addTable(tabname);
		}
	}
	
	private boolean isNew() {
		EbeanServer ebeanServer = DGitConnection.get().getEbeanServer();
		Query<SchemaModel> query = ebeanServer.createQuery(SchemaModel.class);
		List<SchemaModel> list = query.where().eq("name", name).eq("branch", branch).orderBy("ts").setMaxRows(1).findList();
		if(list.size() == 0) {
			return true;
		}
		return false;
	}

	private Set<String> fetchFromVersionTable() throws Exception {
		EbeanServer ebeanServer = DGitConnection.get().getEbeanServer();
		Query<SchemaModel> query = ebeanServer.createQuery(SchemaModel.class);
		List<SchemaModel> list = query.where().eq("name", name).eq("branch", branch).orderBy("ts").setMaxRows(1).findList();
		if(list.size() == 0) {
			throw new Exception("Invalid branch or schema name");
		}
		JsonNode content = list.get(0).content;
		Set<String> dbtables = new HashSet<>();
		if (content.isArray()) {
		    for (final JsonNode objNode : content) {
		        System.out.println(objNode.asText());
		        dbtables.add(objNode.asText());
		    }
		} else {
			throw new Exception("Content should be an array of string (table names)");
		}
		return dbtables;
	}

	Set<String> getTables() {
		Set<String> fromWS = new HashSet<>();
		for (VersionedTable table : tables) {
			fromWS.add(table.getName());
		}
		return fromWS;
	}
}
