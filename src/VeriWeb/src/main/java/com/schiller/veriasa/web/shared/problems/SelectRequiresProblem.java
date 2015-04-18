package com.schiller.veriasa.web.shared.problems;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.schiller.veriasa.web.shared.core.MethodContract;
import com.schiller.veriasa.web.shared.core.Clause;

public class SelectRequiresProblem extends MethodProblem {
	//TODO: separate predicates for (1) sufficient to prove no runtime exceptions (2) sufficient to meet all method callee pre-conditions
	
	private static final long serialVersionUID = 1L;

	private Set<Clause> active;
	private List<Clause> choices;
	private boolean sufficient;
	
	private SelectRequiresProblem(){
		super(null,null);
	}

	/**
	 * Create a problem where the user selects requires clauses from a list
	 * @param method information about the function
	 * @param annotatedBody the method body, annotated with object information and errors
	 * @param choices the candidate set of requires clauses
	 * @param sufficient true iff the KNOWN_GOOD specs are sufficient 
	 */
	public SelectRequiresProblem(
			MethodContract method, 
			String annotatedBody,
			List<Clause> choices,
			Set<Clause> active,
			boolean sufficient) {
		
		super(method,annotatedBody);
		this.sufficient = sufficient;
		this.active = new HashSet<Clause>(active);
		this.choices = new LinkedList<Clause>(choices);
	}

	/**
	 * Get an <i>unmodifiable</i> list of specs to select from
	 * @return the list of specs to select from
	 */
	public List<Clause> getChoices() {
		return Collections.unmodifiableList(choices);
	}

	public void setActive(Set<Clause> active) {
		this.active = active;
	}
	
	public void setChoices(List<Clause> choices) {
		this.choices = choices;
	}

	public void setSufficient(boolean sufficient) {
		this.sufficient = sufficient;
	}
	
	/**
	 * Check if the KNOWN_GOOD specs are sufficient
	 * @return true iff the KNOWN_GOOD specs are sufficient 
	 */
	public boolean isSufficient() {
		return sufficient;
	}

	public Set<Clause> getActive() {
		return active;
	}

	@Override
	public String toString() {
		return "SelectRequires [" + super.getFunction().getSignature() + "]";
	}
}
