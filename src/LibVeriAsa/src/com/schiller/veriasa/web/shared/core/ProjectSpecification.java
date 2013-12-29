package com.schiller.veriasa.web.shared.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * A project specification
 * @author Todd Schiller
 */
public class ProjectSpecification implements Serializable{
	private static final long serialVersionUID = 2L;
	
	private String name;
	private List<TypeSpecification> types;
	
	@SuppressWarnings("unused")
	private ProjectSpecification(){
		
	}
	
	/**
	 * Create a new project specification
	 * @param name the project name
	 * @param types the type specifications
	 */
	public ProjectSpecification(String name, List<TypeSpecification> types){
		this.name = name;
		this.types = new ArrayList<TypeSpecification>(types);
	}

	/**
	 * Get the type specification for <tt>qualifiedType</tt>, or <tt>null</tt> iff the type
	 * does not exist in the project
	 * @param qualifiedType the fully qualified type
	 * @return the type specification for <tt>qualifiedType</tt>
	 */
	public TypeSpecification forType(String qualifiedType){
		for (TypeSpecification type : types){
			if (type.getFullyQualifiedName().equals(qualifiedType)){
				return type;
			}
		}
		return null;
	}
	
	/**
	 * Returns the type specifications (returns a new list)
	 * @return the type specifications
	 */
	public List<TypeSpecification> getTypeSpecs() {
		return new LinkedList<TypeSpecification>(types);
	}
	
	/**
	 * @return the name of the project
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the set of compilation units in the project
	 */
	public Set<String> getCompilationUnits(){
		Set<String> result = new HashSet<String>();
		for (TypeSpecification type : types){
			result.add(type.getLocation().getCompilationUnit());
		}
		return result;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result
				+ ((types == null) ? 0 : types.hashCode());
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
		ProjectSpecification other = (ProjectSpecification) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (types == null) {
			if (other.types != null)
				return false;
		} else if (!types.equals(other.types))
			return false;
		return true;
	}
}
