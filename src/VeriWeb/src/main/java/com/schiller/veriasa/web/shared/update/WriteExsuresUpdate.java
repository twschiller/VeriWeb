package com.schiller.veriasa.web.shared.update;

import java.io.Serializable;

import com.schiller.veriasa.web.shared.core.Clause;

public class WriteExsuresUpdate extends Update implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private Clause clause;

	@SuppressWarnings("unused")
	private WriteExsuresUpdate(){
		
	}
	
	public WriteExsuresUpdate(Clause clause) {
		super();
		this.clause = clause;
	}
	
	public Clause getClause() {
		return clause;
	}	
}
