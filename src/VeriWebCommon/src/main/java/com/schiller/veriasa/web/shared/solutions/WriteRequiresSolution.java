package com.schiller.veriasa.web.shared.solutions;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.schiller.veriasa.web.shared.core.Clause;

public class WriteRequiresSolution extends MethodSolution implements HasImpossibleInfo {

	private static final long serialVersionUID = 2L;
	
	private List<Clause> statements;
	private ImpossibleInfo info;
	
	@SuppressWarnings("unused")
	private WriteRequiresSolution(){
		
	}
	
	@Override
	public ImpossibleInfo getInfo() {
		return info;
	}
	
	@Override
	public boolean hasInfo(){
		return info != null;
	}
	
	public WriteRequiresSolution(List<Clause> statements) {
		this(statements, null);
	}

	public WriteRequiresSolution(List<Clause> statements, ImpossibleInfo info) {
		super();
		this.statements = new LinkedList<Clause>(statements);
		this.info = info;
	}

	public List<Clause> getStatements() {
		return Collections.unmodifiableList(statements);
	}
	
}
