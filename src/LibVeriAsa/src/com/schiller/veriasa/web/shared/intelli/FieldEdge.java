package com.schiller.veriasa.web.shared.intelli;

import java.io.Serializable;

/**
 * An edge representing a field relationship
 */
public class FieldEdge extends IntelliEdge implements Serializable{
	private static final long serialVersionUID = 1L;

	boolean isArray;
	
	@SuppressWarnings("unused")
	private FieldEdge(){
	}
	
	/**
	 * Create a new field edge / relationship
	 * @param name the name of the field
	 * @param docHtml the field's documentation
	 * @param isArray <tt>true</tt> iff the field is an array
	 */
	public FieldEdge(String name, String docHtml, boolean isArray) {
		super(name, docHtml);
		this.isArray = isArray;
	}

	/**
	 * @return <tt>true</tt> iff the field is an array
	 */
	public boolean isArray(){
		return isArray;
	}
}
