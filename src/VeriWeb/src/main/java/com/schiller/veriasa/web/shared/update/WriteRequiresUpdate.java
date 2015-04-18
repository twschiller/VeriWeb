package com.schiller.veriasa.web.shared.update;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.schiller.veriasa.web.shared.core.Clause;

public class WriteRequiresUpdate extends Update implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private List<Clause> specs;

	@SuppressWarnings("unused")
	private WriteRequiresUpdate(){
		
	}
	
	public WriteRequiresUpdate(List<Clause> clauses) {
		super();
		this.specs = new ArrayList<Clause>(clauses);
	}
	
	public List<Clause> getSpecs() {
		return Collections.unmodifiableList(specs);
	}	
}
