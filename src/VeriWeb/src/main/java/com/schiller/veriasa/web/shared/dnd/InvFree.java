package com.schiller.veriasa.web.shared.dnd;

import java.io.Serializable;

public class InvFree extends InvElement implements Maybe<String>, Serializable{
	private static final long serialVersionUID = 1L;
	
	private String value;
	
	public InvFree(RefType type){
		super(new InvRef[] {},type);
		this.value = null;
	}
	
	@SuppressWarnings("unused")
	private InvFree(){
		this(RefType.Expression);
	}
	
	public InvFree(String value, RefType type){
		super(new InvRef[] {},type);
		this.value = value;
	}
	
	
	public InvElement duplicate() {
		return new InvFree(value, super.getRefType());
	}

	@Override
	public String getValue(){
		return value;
	}
	
	public void setValue(String value){
		this.value = value;
	}
	
	@Override
	public String toString(){
		return value;
	}
	
	@Override
	public boolean hasHole(){
		return value == null || value.trim() == "";
	}

	@Override
	public boolean hasValue() {
		return !hasHole();
	}
}
