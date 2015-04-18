package com.schiller.veriasa.web.shared.dnd;

import java.io.Serializable;

public class InvLocal extends InvElement implements Maybe<String>, Stylish, Serializable{
	private static final long serialVersionUID = 1L;
	
	private String value;
	
	public InvLocal(RefType type){
		super(new InvRef[] {},type);
		this.value = null;
	}
	
	@SuppressWarnings("unused")
	private InvLocal(){
		this(RefType.Expression);
	}
	
	public InvLocal(String value, RefType type){
		super(new InvRef[] {},type);
		this.value = value;
	}
	
	
	@Override
	public InvElement duplicate() {
		return new InvLocal(value,super.getRefType());
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
		return hasHole() ? "hole" : "inv-local";
	}
}