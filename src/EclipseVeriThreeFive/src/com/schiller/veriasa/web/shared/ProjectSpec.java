package com.schiller.veriasa.web.shared;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@SuppressWarnings("serial")
public class ProjectSpec implements Serializable{

	private String name;
	private List<TypeSpec> typeSpecs;
	
	@SuppressWarnings("unused")
	private ProjectSpec(){
		
	}
	
	public ProjectSpec(String name, List<TypeSpec> typeSpecs){
		this.name = name;
		this.typeSpecs = new ArrayList<TypeSpec>(typeSpecs);
	}

	public List<TypeSpec> getTypeSpecs() {
		return new LinkedList<TypeSpec>(typeSpecs);
	}
	
	public TypeSpec forType(String type){
		for (TypeSpec t : typeSpecs){
			if (t.getFullyQualifiedName().equals(type)){
				return t;
			}
		}
		
		return null;
	}

	public String getName() {
		return name;
	}

	public Set<String> getCompilationUnits(){
		Set<String> cus = new HashSet<String>();
		
		for (TypeSpec t : typeSpecs){
			cus.add(t.getLocation().getCompilationUnit());
		}
		return cus;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result
				+ ((typeSpecs == null) ? 0 : typeSpecs.hashCode());
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
		ProjectSpec other = (ProjectSpec) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (typeSpecs == null) {
			if (other.typeSpecs != null)
				return false;
		} else if (!typeSpecs.equals(other.typeSpecs))
			return false;
		return true;
	}
}
