package org.kapsarc.dgit;

public class Column {
	private String name;
	private String type;

	public Column(String name, String type) {
		super();
		this.name = name;
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}
	
	@Override
	public int hashCode() {
		return (name + "|" + type).hashCode();
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Column) {
			return (name.equals(((Column) other).name)
					&& type.equals(((Column) other).type));
		}
		return false;
	}

}
