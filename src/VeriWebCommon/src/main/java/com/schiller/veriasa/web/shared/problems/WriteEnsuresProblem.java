package com.schiller.veriasa.web.shared.problems;

import java.util.LinkedList;
import java.util.List;

import com.schiller.veriasa.web.shared.core.MethodContract;
import com.schiller.veriasa.web.shared.core.Clause;
public class WriteEnsuresProblem extends MethodProblem implements WriteProblem {
	private static final long serialVersionUID = 1L;

	private List<Clause> requires;
	private List<Clause> known;
	
	private WriteEnsuresProblem(){
		super(null,null);
	}

	public WriteEnsuresProblem(MethodContract function, 
			String annotatedBody,
			List<Clause> requires, 
			List<Clause> known) {
		
		super(function,annotatedBody);
		this.requires = new LinkedList<Clause>(requires);
		this.known = new LinkedList<Clause>(known);
	}

	
	public List<Clause> getRequires() {
		return requires;
	}

	@Override
	public List<Clause> getKnown() {
		return known;
	}
	
	public void setRequires(List<Clause> requires) {
		this.requires = new LinkedList<Clause>(requires);
	}

	public void setKnown(List<Clause> known) {
		this.known = new LinkedList<Clause>(known);
	}

	@Override
	public String toString() {
		return "WriteEnsures [" + super.getFunction().getSignature() + "]";
	}
}
