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

	public void save() {
		BranchModel bm = new BranchModel();
		bm.branchName = this.name;
		bm.id = this.id;
		bm.commitId = this.commitId;
		bm.schema = this.schema;
		bm.save();
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

	private BranchModel fetchById(String id) throws Exception {
		EbeanServer ebeanServer = DGitConnection.get().getEbeanServer();
		Query<BranchModel> query = ebeanServer.createQuery(BranchModel.class);
		List<BranchModel> list = query.where().eq("id", id).setMaxRows(2).findList();
		if (list.size() == 0) {
			throw new Exception("Invalid id");
		}
		if (list.size() > 1) {
			throw new Exception("Corrupt data. cannot have multiple ids " + id);
		}
		return list.get(0);
	}

	public String nextAvailableId() {
		List<Branch> children = getChildren();
		Branch lastchild = children.get(children.size() - 1);
		int id = Integer.parseInt(lastchild.id.substring(lastchild.id.lastIndexOf('_')));
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
}
