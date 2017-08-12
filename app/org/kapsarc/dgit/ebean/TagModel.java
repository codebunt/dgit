package org.kapsarc.dgit.ebean;

import java.sql.Timestamp;

import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.annotation.Index;
import com.avaje.ebean.annotation.UpdatedTimestamp;

public class TagModel {
	@Index
	@Temporal(TemporalType.TIMESTAMP)
	@CreatedTimestamp
	public Timestamp cts;

	@Index
	@Temporal(TemporalType.TIMESTAMP)
	@UpdatedTimestamp
	public Timestamp ts;

	@Index
	@Temporal(TemporalType.TIMESTAMP)
	@CreatedTimestamp
	@Id
	public Timestamp id;

	public String name;

}
