package org.kapsarc.dgit.ebean;

import javax.persistence.Entity;
import javax.persistence.Table;

import com.avaje.ebean.annotation.Index;

@Entity
@Table(name = "v_schema")
public class SchemaModel extends DBaseModel {
	@Index
	public String name;
}
