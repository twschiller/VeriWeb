package com.schiller.veriasa.web.shared.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Fluently build type specifications
 * @author Todd Schiller
 */
public class TypeSpecBuilder {

	private String fullyQualifiedName;
	
	private List<Clause> invariants;
	
	private List<FieldSpec> fields;
	private List<MethodContract> methods;
		
	private SourceLocation location;
	
	public TypeSpecBuilder(TypeSpecification base){
		this.fullyQualifiedName = base.getFullyQualifiedName();
		this.invariants = new ArrayList<Clause>(base.getInvariants());
		this.fields = new ArrayList<FieldSpec>(base.getFields());
		this.methods = new ArrayList<MethodContract>(base.getMethods());
		this.location = base.getLocation();
	}

	public static TypeSpecBuilder builder(TypeSpecification base){
		return new TypeSpecBuilder(base);
	}
	
	public TypeSpecification getType(){
		return new TypeSpecification(fullyQualifiedName, location, invariants, fields, methods);
	}
	
	/**
	 * @param fullyQualifiedName the fullyQualifiedName to set
	 */
	public TypeSpecBuilder setFullyQualifiedName(String fullyQualifiedName) {
		this.fullyQualifiedName = fullyQualifiedName;
		return this;
	}

	/**
	 * @param invariants the invariants to set
	 */
	public TypeSpecBuilder setInvariants(List<Clause> invariants) {
		this.invariants = new ArrayList<Clause>(invariants);
		return this;
	}

	/**
	 * @param fields the fields to set
	 */
	public TypeSpecBuilder setFields(List<FieldSpec> fields) {
		this.fields = new ArrayList<FieldSpec>(fields);
		return this;
	}

	/**
	 * @param methods the methods to set
	 */
	public TypeSpecBuilder setMethods(List<MethodContract> methods) {
		this.methods = new ArrayList<MethodContract>(methods);
		return this;
	}

	/**
	 * @param location the location to set
	 */
	public TypeSpecBuilder setLocation(SourceLocation location) {
		this.location = location;
		return this;
	}
}
