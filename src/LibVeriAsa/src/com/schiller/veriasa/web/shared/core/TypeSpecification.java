package com.schiller.veriasa.web.shared.core;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * A type specification
 * @author Todd Schiller
 */
public class TypeSpecification implements Serializable{
	private static final long serialVersionUID = 2L;

	private String fullyQualifiedName;
	
	private List<Clause> invariants;
	
	private List<FieldSpec> fields;
	private List<MethodContract> methods;
		
	private SourceLocation location;
	
	@SuppressWarnings("unused")
	private TypeSpecification(){	
	}
	
	public TypeSpecification(
			String fullyQualifiedName, 
			SourceLocation location,
			List<Clause> invariants, 
			List<FieldSpec> fields,  
			List<MethodContract> methods){
		
		this.fullyQualifiedName = fullyQualifiedName;
		this.methods = new LinkedList<MethodContract>(methods);
		this.fields = new LinkedList<FieldSpec>(fields);
		this.invariants = new LinkedList<Clause>(invariants);
		this.location = location;
	}
	
	public String getFullyQualifiedName() {
		return fullyQualifiedName;
	}
	
	public List<MethodContract> getMethods() {
		return Collections.unmodifiableList(methods);
	}

	public List<Clause> getInvariants() {
		return Collections.unmodifiableList(invariants);
	}

	public List<FieldSpec> getFields() {
		return Collections.unmodifiableList(fields);
	}

	public void setFields(List<FieldSpec> fields) {
		this.fields = fields;
	}

	public SourceLocation getLocation() {
		return location;
	}

	@Override
	public String toString(){
		return getFullyQualifiedName();
	}

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
				+ ((methods == null) ? 0 : methods.hashCode());
		result = prime * result
				+ ((invariants == null) ? 0 : invariants.hashCode());
		result = prime * result
				+ ((location == null) ? 0 : location.hashCode());
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
		TypeSpecification other = (TypeSpecification) obj;
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
		if (methods == null) {
			if (other.methods != null)
				return false;
		} else if (!methods.equals(other.methods))
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
