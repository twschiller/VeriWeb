package com.schiller.veriasa.web.shared.solutions;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.schiller.veriasa.web.shared.core.Clause;

public class WriteExsuresSolution extends MethodSolution implements HasImpossibleInfo {

	private static final long serialVersionUID = 2L;

	private List<Clause> statements;
	
	private List<Clause> approvedObjectInvariants;
	
	private ImpossibleInfo info;
	
	@SuppressWarnings("unused")
	private WriteExsuresSolution(){
		
	}

	public WriteExsuresSolution(List<Clause> statements, List<Clause> approvedObjectInvariants) {
		super();
		this.statements = new LinkedList<Clause>(statements);
		this.approvedObjectInvariants = new LinkedList<Clause>(approvedObjectInvariants);
		this.info = null;
	}
	
	public WriteExsuresSolution(List<Clause> statements, List<Clause> approvedObjectInvariants, ImpossibleInfo info) {
		this(statements, approvedObjectInvariants);
		this.info = info;
	}

	public List<Clause> getStatements() {
		return Collections.unmodifiableList(statements);
	}

	public List<Clause> getApprovedObjectInvariants(){
		return Collections.unmodifiableList(approvedObjectInvariants);
	}
	
	@Override
	public ImpossibleInfo getInfo() {
		return info;
	}

	@Override
	public boolean hasInfo() {
		return info != null;
	}
	
}
