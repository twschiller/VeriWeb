package com.schiller.veriasa.web.shared.dnd;

import java.io.Serializable;

public class InvMethod extends InvElement implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private String value;
	
	public InvMethod(RefType type){
		super(new InvRef[] {},type);
		this.value = null;
	}
	
	@SuppressWarnings("unused")
	private InvMethod(){
		this(RefType.Expression);
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
}
