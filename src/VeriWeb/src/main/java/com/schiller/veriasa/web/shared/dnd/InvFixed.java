package com.schiller.veriasa.web.shared.dnd;

import java.io.Serializable;

/**
 * A fixed value fragment
 * @author Todd Schiller
 */
public class InvFixed extends InvElement implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private String value;

	public InvFixed(String value, RefType type){
		super(new InvRef[] {},type);
		this.value = value;
	}
	
	@SuppressWarnings("unused")
	private InvFixed(){
		this("",RefType.Expression);
	}
	
	@Override
	public InvElement duplicate() {
		return new InvFixed(value, super.getRefType());
	}

	@Override
	public String getValue(){
		return value;
	}
	@Override
	public String toString(){
		return value;
	}
}
