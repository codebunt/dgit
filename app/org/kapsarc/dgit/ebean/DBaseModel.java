package org.kapsarc.dgit.ebean;

import java.sql.Timestamp;
import java.util.UUID;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.annotation.DbJson;
import com.avaje.ebean.annotation.Index;
import com.fasterxml.jackson.databind.JsonNode;

@MappedSuperclass
public class DBaseModel extends Model {

}
