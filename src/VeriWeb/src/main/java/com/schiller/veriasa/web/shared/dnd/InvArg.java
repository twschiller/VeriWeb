package com.schiller.veriasa.web.shared.dnd;

import java.io.Serializable;

public class InvArg extends InvElement implements Maybe<String>, Stylish, Serializable{
	private static final long serialVersionUID = 1L;
	
	private String value;
	
	@SuppressWarnings("unused")
	private InvArg(){
		this(null);
	}
	
	public InvArg(RefType type){
		super(new InvRef[] {},type);
		this.value = null;
	}
	
	public InvArg(String value, RefType type){
		super(new InvRef[] {},type);
		this.value = value;
	}
	
	@Override
	public InvElement duplicate() {
		return new InvArg(value,super.getRefType());
	}
	
	@Override
	public String getValue(){
		return value;
	}
	
	@Override
	public String toString(){
		return value;
	}
	
	@Override
	public boolean hasHole(){
		return value == null;
	}

	@Override
	public boolean hasValue() {
		return !hasHole();
	}

	@Override
	public String getStyle() {
		return hasHole() ? "hole" : "inv-arg";
	}
}
