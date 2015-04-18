package com.schiller.veriasa.web.shared.executejml;

import java.io.Serializable;

public class BlameRecord implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private String expr;
	private String val;
	
	@SuppressWarnings("unused")
	private BlameRecord(){
	}
	
	public BlameRecord(String expr, String val) {
		super();
		this.expr = expr;
		this.val = val;
	}
	/**
	 * @return the val
	 */
	public String getVal() {
		return val;
	}
	/**
	 * @return the expr
	 */
	public String getExpr() {
		return expr;
	}

	@Override
	public String toString() {
		return "{" + expr + " : " + val + "}";
	}
}
