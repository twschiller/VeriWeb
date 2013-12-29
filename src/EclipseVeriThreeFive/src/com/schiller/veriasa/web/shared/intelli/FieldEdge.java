package com.schiller.veriasa.web.shared.intelli;

import java.io.Serializable;

@SuppressWarnings("serial")
public class FieldEdge extends IntelliEdge implements Serializable{
	boolean isArray;
	
	public FieldEdge(String name, String docHtml, boolean isArray) {
		super(name, docHtml);
		this.isArray = isArray;
	}

	public boolean isArray(){
		return isArray();
	}
	
	@SuppressWarnings("unused")
	private FieldEdge(){
		super();
	}
}
