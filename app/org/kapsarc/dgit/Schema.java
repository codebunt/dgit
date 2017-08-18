package org.kapsarc.dgit;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.kapsarc.dgit.ebean.SchemaModel;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.Query;
import com.avaje.ebean.Transaction;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Schema extends VersionedObject {

	private Set<VersionedTable> tables = new HashSet<VersionedTable>();
	private static HashMap<String, Schema> map = new HashMap<String, Schema>();

	private Schema(String name, Branch branch) throws Exception {
		this.name = name;
		this.branch = branch;
		System.out.println(branch);
		load();
	}
	
	public Schema branch(String branchname) throws Exception {
		// TODO : do dirty check. schema has to saved first
		Branch child = branch.branch(branchname);
		return new Schema(name, child);
	}
	
	private String getBranchId() {
		return this.branch.getId();
	}
	
	public Workspace getWorkSpace() throws Exception {
		return Workspace.get(this);
	}

	public static synchronized Schema get(String name, Branch parent , String branch) throws Exception {
		if(branch == null) {
			throw new Exception("branch is null");
		}
		String key = name + "|" + branch;
		if (map.get(key) == null) {
			Schema s = new Schema(name , Branch.ensureBranch(name, parent, branch));
			map.put(key, s);
		}
		return map.get(key);
	}
	
	public static synchronized Schema has(String name, String branch) throws Exception {
		if(branch == null) {
			throw new Exception("branch is null");
		}
		String key = name + "|" + branch;
		if (map.get(key) == null) {
			Schema s = new Schema(name , Branch.getBranch(name, branch));
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
		List<SchemaModel> list = query.where().eq("name", name).eq("branch", branch.getId()).orderBy("ts").setMaxRows(1).findList();
		if(list.size() == 0) {
			return true;
		}
		return false;
	}

	private Set<String> fetchFromVersionTable() throws Exception {
		Set<String> dbtables = new HashSet<>();
		EbeanServer ebeanServer = DGitConnection.get().getEbeanServer();
		Query<SchemaModel> query = ebeanServer.createQuery(SchemaModel.class);
		System.out.println(branch);
		List<SchemaModel> list = query.where().eq("name", name).eq("branch", branch.getId()).orderBy("ts").setMaxRows(1).findList();
		if(list.size() == 0) {
			return dbtables;
		}
		JsonNode content = list.get(0).content;
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
	
	public Schema save() throws Exception {
		EbeanServer ebeanServer = DGitConnection.get().getEbeanServer();
		Transaction transaction = ebeanServer.beginTransaction();
		try {
			SchemaModel sm = getModelById();
			sm.branch = this.branch.getId();
			ObjectMapper mapper = new ObjectMapper();
			JsonNode node = mapper.valueToTree(getTables());
			sm.content = node;
			sm.name = this.name;
			if(sm.id == null)
				sm.save();
			else
				sm.update();
			branch.save();
			transaction.commit();
		} catch (Exception e) {
			transaction.rollback();
			throw e;
		} finally {
			transaction.end();
		}
		return this;
	}

	private SchemaModel getModelById() throws Exception {
		EbeanServer ebeanServer = DGitConnection.get().getEbeanServer();
		Query<SchemaModel> query = ebeanServer.createQuery(SchemaModel.class);
		List<SchemaModel> list = query.where().eq("name", name).eq("branch" , this.branch.getId()).setMaxRows(2).findList();
		if (list.size() == 0) {
			return new SchemaModel();
		}
		if (list.size() > 1) {
			throw new Exception("Corrupt data. Cannot have multiple name with same value " + name);
		}
		return list.get(0);

	}

	Set<String> getTables() {
		Set<String> fromWS = new HashSet<>();
		for (VersionedTable table : tables) {
			fromWS.add(table.getName());
		}
		return fromWS;
	}
}
