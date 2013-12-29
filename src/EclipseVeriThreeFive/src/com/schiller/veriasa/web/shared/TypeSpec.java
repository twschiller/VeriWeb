package com.schiller.veriasa.web.shared;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("serial")
public class TypeSpec implements Serializable{

	private String fullyQualifiedName;
	
	private List<Spec> invariants;
	
	private List<FieldSpec> fields;
	private List<FunctionSpec> functions;
		
	private SrcLoc location;
	
	public String getFullyQualifiedName() {
		return fullyQualifiedName;
	}

	public List<FunctionSpec> getFunctions() {
		return Collections.unmodifiableList(functions);
	}

	public List<Spec> getInvariants() {
		return Collections.unmodifiableList(invariants);
	}

	public List<FieldSpec> getFields() {
		return Collections.unmodifiableList(fields);
	}

	public void setFields(List<FieldSpec> fields) {
		this.fields = fields;
	}

	public SrcLoc getLocation() {
		return location;
	}

	public void setLocation(SrcLoc location) {
		this.location = location;
	}

	@SuppressWarnings("unused")
	private TypeSpec(){
		
	}
	
	public TypeSpec(String typeName, 
			SrcLoc location,
			List<Spec> invs, 
			List<FieldSpec> fields,  
			List<FunctionSpec> functions){
		
		this.fullyQualifiedName = typeName;
		this.functions = new LinkedList<FunctionSpec>(functions);
		this.fields = new LinkedList<FieldSpec>(fields);
		this.invariants = new LinkedList<Spec>(invs);
		this.location = location;
	}
	
	@Override
	public String toString(){
		return getFullyQualifiedName();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fields == null) ? 0 : fields.hashCode());
		result = prime
				* result
				+ ((fullyQualifiedName == null) ? 0 : fullyQualifiedName
						.hashCode());
		result = prime * result
				+ ((functions == null) ? 0 : functions.hashCode());
		result = prime * result
				+ ((invariants == null) ? 0 : invariants.hashCode());
		result = prime * result
				+ ((location == null) ? 0 : location.hashCode());
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
		TypeSpec other = (TypeSpec) obj;
		if (fields == null) {
			if (other.fields != null)
				return false;
		} else if (!fields.equals(other.fields))
			return false;
		if (fullyQualifiedName == null) {
			if (other.fullyQualifiedName != null)
				return false;
		} else if (!fullyQualifiedName.equals(other.fullyQualifiedName))
			return false;
		if (functions == null) {
			if (other.functions != null)
				return false;
		} else if (!functions.equals(other.functions))
			return false;
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
		return true;
	}
}
