package org.kapsarc.dgit.ebean;

import java.sql.Timestamp;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.annotation.DbJson;
import com.avaje.ebean.annotation.Index;
import com.fasterxml.jackson.databind.JsonNode;

@Entity
@Table(name = "v_root")
public class RootModel extends DBaseModel {

	@Index
    @Temporal(TemporalType.TIMESTAMP)
    @CreatedTimestamp
	public Timestamp ts;

	@Index
	public String branch;

	@Id
	public UUID id;

	@Index
	public String uid;

	@Index
	public String type;

	@DbJson
	public JsonNode content;
}
