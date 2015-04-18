package com.schiller.veriasa.web.shared.core;

import java.io.Serializable;

/**
 * Information about a field in a type
 * @author Todd Schiller
 */
public class FieldSpec implements Serializable{
	private static final long serialVersionUID = 2L;

	private String name;
	private SourceLocation location;
	
	@SuppressWarnings("unused")
	private FieldSpec(){
	}

	public FieldSpec(String name, SourceLocation location) {
		this.name = name;
		this.location = location;
	}

	public String getName() {
		return name;
	}

	public SourceLocation getLocation() {
		return location;
	}

	
	@Override
	public String toString(){
		return getName();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((location == null) ? 0 : location.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FieldSpec other = (FieldSpec) obj;
		if (location == null) {
			if (other.location != null)
				return false;
		} else if (!location.equals(other.location))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
}
