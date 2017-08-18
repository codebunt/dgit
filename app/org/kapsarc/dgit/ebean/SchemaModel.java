package org.kapsarc.dgit.ebean;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.avaje.ebean.annotation.Index;

@Entity
@Table(name = "v_schema", uniqueConstraints = @UniqueConstraint(columnNames = { "name", "branch" }))
public class SchemaModel extends DBaseModel {
	@Index
	public String name;
}
