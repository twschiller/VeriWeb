package com.schiller.veriasa.web.shared.problems;

import java.util.LinkedList;
import java.util.List;

import com.schiller.veriasa.web.shared.core.MethodContract;
import com.schiller.veriasa.web.shared.core.Clause;

public class WriteExsuresProblem extends MethodProblem implements WriteProblem {
	private static final long serialVersionUID = 2L;

	private String exception;
	private List<Clause> requires;
	private List<Clause> known;
	
	private WriteExsuresProblem(){
		super(null,null);
	}
	
	public WriteExsuresProblem(MethodContract method, 
			String annotatedBody,
			String exception,
			List<Clause> requires, 
			List<Clause> known) {
		
		super(method,annotatedBody);
		this.exception = exception;
		this.requires = new LinkedList<Clause>(requires);
		this.known = new LinkedList<Clause>(known);
	}

	public void setRequires(List<Clause> requires) {
		this.requires = new LinkedList<Clause>(requires);
	}

	public void setKnown(List<Clause> known) {
		this.known = new LinkedList<Clause>(known);
	}

	public String getException() {
		return exception;
	}

	public void setException(String exception) {
		this.exception = exception;
	}

	public List<Clause> getRequires() {
		return requires;
	}

	@Override
	public List<Clause> getKnown() {
		return known;
	}
	
	@Override
	public String toString() {
		return "WriteExsures [" + super.getFunction().getSignature() + "]";
	}

}
