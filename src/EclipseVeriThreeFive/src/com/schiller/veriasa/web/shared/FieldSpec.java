package com.schiller.veriasa.web.shared;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("serial")
public class FieldSpec implements Serializable{

	private String name;
	private SrcLoc location;
	private List<Spec> invariants;
	
	@SuppressWarnings("unused")
	private FieldSpec(){
		
	}
	
	public FieldSpec(String name, SrcLoc location, List<Spec> invariants){
		super();
		this.name = name;
		this.location = location;
		this.invariants = new LinkedList<Spec>(invariants);
	}
	
	public FieldSpec(String name, SrcLoc location) {
		this(name,location, new LinkedList<Spec>());
	}

	public String getName() {
		return name;
	}

	public SrcLoc getLocation() {
		return location;
	}

	public List<Spec> getInvariants() {
		return Collections.unmodifiableList(invariants);
	}
	
	@Override
	public String toString(){
		return getName();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((invariants == null) ? 0 : invariants.hashCode());
		result = prime * result
				+ ((location == null) ? 0 : location.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FieldSpec other = (FieldSpec) obj;
		if (invariants == null) {
			if (other.invariants != null)
				return false;
		} else if (!invariants.equals(other.invariants))
			return false;
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
