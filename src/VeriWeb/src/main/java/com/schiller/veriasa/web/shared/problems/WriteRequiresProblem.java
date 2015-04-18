package com.schiller.veriasa.web.shared.problems;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.schiller.veriasa.web.shared.core.MethodContract;
import com.schiller.veriasa.web.shared.core.Clause;

public class WriteRequiresProblem extends MethodProblem implements Serializable, WriteProblem {
	private static final long serialVersionUID = 1L;

	private List<Clause> known;
	private boolean sufficient;
	
	private WriteRequiresProblem(){
		super(null,null);
	}
	
	public WriteRequiresProblem(MethodContract function, 
			String annotatedBody,
			List<Clause> known,
			boolean sufficient) {
		
		super(function, annotatedBody);
		this.known = new LinkedList<Clause>(known);
		this.sufficient = sufficient;
	}

	@Override
	public List<Clause> getKnown() {
		return Collections.unmodifiableList(known);
	}

	public boolean isSufficient() {
		return sufficient;
	}
	

	@Override
	public String toString() {
		return "WriteRequires [" + super.getFunction().getSignature() + "]";
	}

	public void setKnown(List<Clause> known) {
		this.known = new LinkedList<Clause>(known);
	}

	public void setSufficient(boolean sufficient) {
		this.sufficient = sufficient;
	}
}
