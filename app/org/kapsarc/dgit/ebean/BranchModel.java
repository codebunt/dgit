package org.kapsarc.dgit.ebean;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.annotation.Index;
import com.avaje.ebean.annotation.UpdatedTimestamp;

@Entity
@Table(name = "v_branch")
public class BranchModel extends Model {
	@Index
	@Temporal(TemporalType.TIMESTAMP)
	@CreatedTimestamp
	public Timestamp cts;

	@Index
	@Temporal(TemporalType.TIMESTAMP)
	@UpdatedTimestamp
	public Timestamp ts;

	@Id
	@Column(unique=true)
	public String id;

	public String branchName;

	public String schema;
	
	public String commitId;

}
