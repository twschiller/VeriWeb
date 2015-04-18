package com.schiller.veriasa.web.shared.feedback;

import java.io.Serializable;

import com.schiller.veriasa.web.shared.core.Clause;
import com.schiller.veriasa.web.shared.feedback.DynamicFeedback.HasDynamicFeedback;

public class WriteExsuresFeedback extends Feedback implements Serializable, HasDynamicFeedback{
	private static final long serialVersionUID = 2L;

	private Clause statement;
	
	private DynamicFeedback dynamicFeedback;
	
	/**
	 * <code>true</code> iff the user should be prompted to promote the statement
	 * to be an object invariant
	 */
	private boolean promptAsObjectInvariant;
	
	@SuppressWarnings("unused")
	private WriteExsuresFeedback(){
		
	}

	/**
	 * @param statement the exsures contract that the feedback corresponds to
	 * @param dynamicFeedback dynamic feedback for the contract, or <code>null</code>
	 * @param promptAsObjectInvariant <code>true</code> iff the user should be prompted to promote the statement
	 * to be an object invariant
	 */
	public WriteExsuresFeedback(Clause statement, DynamicFeedback dynamicFeedback, boolean promptAsObjectInvariant) {
		super();
		this.statement = statement;
		this.dynamicFeedback = dynamicFeedback;
		this.promptAsObjectInvariant = promptAsObjectInvariant;
	}

	public Clause getStatement() {
		return statement;
	}

	@Override
	public DynamicFeedback getDynamicFeedback() {
		return dynamicFeedback;
	}

	@Override
	public boolean hasDynamicFeedback() {
		return dynamicFeedback != null;
	}
	
	/**
	 * @return <code>true</code> iff the user should be prompted to promote the statement
	 * to be an object invariant
	 */
	public boolean promptAsObjectInvariant() {
		return promptAsObjectInvariant;
	}
}
