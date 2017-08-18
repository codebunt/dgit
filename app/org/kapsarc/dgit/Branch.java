package org.kapsarc.dgit;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.kapsarc.dgit.ebean.BranchModel;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.Query;

public class Branch {
	private String id;

	private String name;

	private String schema;

	private String commitId;

	public Branch(String id) throws Exception {
		BranchModel model = fetchById(id);
		id = model.id;
		name = model.branchName;
		schema = model.schema;
		commitId = model.commitId;
	}

	public static Branch ensureBranch(String schema, Branch parent, String bname) throws Exception {
		try {
			BranchModel bm = fetchByName(schema, bname);
			if (bm != null)
				return new Branch(bm);
		} catch (Exception e) {
		}
		BranchModel bm = new BranchModel();
		bm.branchName = bname;
		if (parent != null)
			bm.id = parent.nextAvailableId();
		else if (bname.equalsIgnoreCase("main"))
			bm.id = schema + "_1";
		else
			throw new Exception("Parent branch cannot be null unless it is main");
		bm.schema = schema;
		Branch branch = new Branch(bm);
		// branch.save();
		return branch;
	}

	public static Branch getBranch(String schema, String bname) throws Exception {
		try {
			BranchModel bm = fetchByName(schema, bname);
			if (bm != null)
				return new Branch(bm);
		} catch (Exception e) {
		}
		return null;
	}

	private Branch() {

	}

	private Branch(BranchModel bm) {
		this.commitId = bm.commitId;
		this.id = bm.id;
		this.name = bm.branchName;
		this.schema = bm.schema;
	}

	public List<Branch> getChildren() {
		EbeanServer ebeanServer = DGitConnection.get().getEbeanServer();
		Query<BranchModel> query = ebeanServer.createQuery(BranchModel.class);
		List<BranchModel> list = query.where().startsWith("id", id + "_").findList();
		List<Branch> ret = new ArrayList<Branch>();
		for (BranchModel bm : list) {
			Branch b = new Branch(bm);
			ret.add(b);
		}
		ret.sort(new Comparator<Branch>() {
			@Override
			public int compare(Branch o1, Branch o2) {
				return o1.id.compareTo(o2.id);
			}
		});
		return ret;
	}

	void save() throws Exception {
		BranchModel bm = ensureModel(id);
		if (bm == null) {
			bm = new BranchModel();
			populateModel(bm);
			bm.save();
		} else {
			populateModel(bm);
			bm.update();
		}
	}

	private void populateModel(BranchModel bm) {
		bm.branchName = this.name;
		bm.id = this.id;
		bm.commitId = this.commitId;
		bm.schema = this.schema;
	}

	public Branch branch(String branchname) throws Exception {
		BranchModel bm = fetchByName(schema, branchname);
		System.out.println("branchibg"+bm);
		if (bm == null) {
			bm = new BranchModel();
			bm.branchName = branchname;
			bm.id = nextAvailableId();
			bm.commitId = this.commitId;
			bm.schema = this.schema;
			bm.save();
			System.out.println("savin");
		} 
		return new Branch(bm);
	}

	public Branch getParent() throws Exception {
		String pid = getParentId();
		if (pid == null) {
			return null;
		}
		BranchModel parent = fetchById(pid);
		if (parent == null)
			throw new Exception("no parent");
		return new Branch(parent);
	}

	private String getParentId() {
		if (id.equals(schema + "_1"))
			return null;
		return id.substring(0, id.lastIndexOf('_'));
	}

	private static BranchModel fetchById(String id) throws Exception {
		BranchModel model = ensureModel(id);
		if (model == null) {
			throw new Exception("Invalid id");
		}
		return model;
	}

	private static BranchModel fetchByName(String schema, String name) throws Exception {
		EbeanServer ebeanServer = DGitConnection.get().getEbeanServer();
		Query<BranchModel> query = ebeanServer.createQuery(BranchModel.class);
		List<BranchModel> list = query.where().eq("schema", schema).eq("branch_name", name).setMaxRows(2).findList();
		if (list.size() == 0) {
			return null;
		}
		if (list.size() > 1) {
			throw new Exception("Corrupt data. cannot have multiple ids " + name);
		}
		return list.get(0);
	}

	private static BranchModel ensureModel(String id) throws Exception {
		EbeanServer ebeanServer = DGitConnection.get().getEbeanServer();
		Query<BranchModel> query = ebeanServer.createQuery(BranchModel.class);
		List<BranchModel> list = query.where().eq("id", id).setMaxRows(2).findList();
		if (list.size() == 0) {
			return null;
		}
		if (list.size() > 1) {
			throw new Exception("Corrupt data. cannot have multiple ids " + id);
		}
		return list.get(0);
	}

	public String nextAvailableId() {
		List<Branch> children = getChildren();
		String lastchild = this.id + "_" + 0;
		if (children.size() != 0) {
			lastchild = children.get(children.size() - 1).id;
		}
		System.out.println(id);
		System.out.println(lastchild.lastIndexOf('_'));
		System.out.println(lastchild.substring(lastchild.lastIndexOf('_')));
		int id = Integer.parseInt(lastchild.substring(lastchild.lastIndexOf('_') + 1));
		return this.id + "_" + (++id);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSchema() {
		return schema;
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}

	public String getCommitId() {
		return commitId;
	}

	public void setCommitId(String commitId) {
		this.commitId = commitId;
	}

	public static void main(String[] args) {
		Branch b = new Branch();
		b.id = "test_main";
	}
}
