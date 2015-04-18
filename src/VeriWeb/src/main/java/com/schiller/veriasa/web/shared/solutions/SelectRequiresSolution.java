package com.schiller.veriasa.web.shared.solutions;

import java.util.LinkedList;
import java.util.List;

import com.schiller.veriasa.web.shared.core.Clause;

public class SelectRequiresSolution extends MethodSolution implements HasImpossibleInfo {

	private static final long serialVersionUID = 2L;
	
	private List<Clause> selected;
	private ImpossibleInfo info;
	
	@SuppressWarnings("unused")
	private SelectRequiresSolution(){
		
	}

	public SelectRequiresSolution(List<Clause> selected, ImpossibleInfo info) {
		super();
		this.selected = new LinkedList<Clause>(selected);
		this.info = info;
	}

	public SelectRequiresSolution(List<Clause> selected) {
		this(selected, null);
	}

	public List<Clause> getSelected() {
		return new LinkedList<Clause>(selected);
	}
	
	@Override
	public boolean hasInfo(){
		return info != null;
	}
	
	@Override
	public ImpossibleInfo getInfo() {
		return info;
	}
}
