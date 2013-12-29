package com.schiller.veriasa.web.shared.update;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.schiller.veriasa.web.shared.core.Clause;

public class SelectRequiresUpdate extends Update implements Serializable {
	private static final long serialVersionUID = 1L;

	private List<Clause> selected;

	@SuppressWarnings("unused")
	private SelectRequiresUpdate(){
	}

	public SelectRequiresUpdate(List<Clause> selected) {
		super();
		this.selected = new ArrayList<Clause>(selected);
	}

	public List<Clause> getSelected() {
		return new ArrayList<Clause>(selected);
	}
}
