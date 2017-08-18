package org.kapsarc.dgit;

import java.util.HashMap;
import java.util.Iterator;

import com.fasterxml.jackson.databind.JsonNode;

class Row {
	private HashMap<String, Column> columns;
	private HashMap<String, Object> values = new HashMap<>();
	private boolean update;
	private Table table;

	public Row(JsonNode node, Table table /* TODO this has to be immutable */) throws Exception {
		this.table = table;
		this.columns = table.columns();
		Iterator<String> fieldNames = node.fieldNames();
		while (fieldNames.hasNext()) {
			String fieldName = fieldNames.next();
			JsonNode fieldValue = node.get(fieldName);
			if (fieldValue.isObject()) {
				System.out.println(fieldName + " :");
				throw new Exception("Nested Objects not supported");
			} else {
				if (columns.containsKey(fieldName)) {
					values.put(fieldName, convert(fieldValue, columns.get(fieldName)));
				} else {
					throw new Exception("Unknown field " + fieldName);
				}
			}
		}
		JsonNode id = node.get("_id");
		this.update = id != null && id.asText() != null && id.asText().equals("");
	}

	private Object convert(JsonNode fieldValue, Column column) {
		if (column.getType().equalsIgnoreCase("TEXT")) {
			return fieldValue.asText();
		}
		if (column.getType().equalsIgnoreCase("INTEGER")) {
			return fieldValue.asInt();
		}
		if (column.getType().equalsIgnoreCase("FLOAT")) {
			return fieldValue.asDouble();
		}
		return null;
	}

	String toSql() {
		String sql = "";
		if (update) {
			Iterator<String> iterator = values.keySet().iterator();
			DList<Object> list = new DList<>();
			while (iterator.hasNext()) {
				String col = iterator.next();
				Object object = values.get(col);
				if (object instanceof Double || object instanceof Integer || object instanceof Float) {
					list.add(col + "=" + object.toString());
				} else {
					list.add(col + "= '" + object.toString() + "'");
				}
			}
			sql = "UPDATE " + table.getName() + " SET " + list.toString() + " WHERE _id = " + values.get("_id");
		} else {
			Iterator<String> iterator = values.keySet().iterator();
			DList<Object> list = new DList<>();
			DList<Object> clist = new DList<>();
			while (iterator.hasNext()) {
				String col = iterator.next();
				clist.add(col);
				Object object = values.get(col);
				if (object instanceof Double || object instanceof Integer || object instanceof Float) {
					list.add(object.toString());
				} else {
					list.add("'" + object.toString() + "'");
				}
			}
			sql = "INSERT INTO " + table.getName() + clist.toString("(",")") + " VALUES " + list.toString("(",")");
		}
		return sql;
	}
}
