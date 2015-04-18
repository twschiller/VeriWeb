package com.schiller.veriasa.web.shared.solutions;

import java.io.Serializable;

import com.schiller.veriasa.web.shared.core.Clause;

public class ImpossibleInfo implements Serializable {

	private static final long serialVersionUID = 2L;

	public static enum Reason { STRONG_REQ, WEAK_ENS, WEAK_EXS, BUG, NOT_LISTED}
	
	private Reason reason;
	private Clause associatedStatement;
	private String method;
	private String comment;
	
	@SuppressWarnings("unused")
	private ImpossibleInfo(){
		
	}
	
	public ImpossibleInfo(Reason reason, String method, String comment) {
		this(reason, method, null, comment);
	}

	public ImpossibleInfo(Reason reason, String method, Clause associatedStatement, String comment) {
		super();
		this.associatedStatement = associatedStatement;
		this.reason = reason;
		this.method = method;
		this.comment = comment;
	}
	
	public boolean hasAssociatedStatement(){
		return associatedStatement != null;
	}
	
	public Clause getAssociatedStatement(){
		return associatedStatement;
	}
	
	public Reason getReason() {
		return reason;
	}
	
	public String getMethod() {
		return method;
	}
	
	public String getComment() {
		return comment;
	}
}
