package com.schiller.veriasa.web.shared.feedback;

import java.io.Serializable;

public abstract class RequiresFeedback extends Feedback implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String annotatedBody;
	private boolean sufficient;
	
	@SuppressWarnings("unused")
	private RequiresFeedback(){
	}
	
	/**
	 * @param annotatedBody the annotated method body
	 * @param sufficient <code>true</code> iff the precondition set was sufficient to eliminate all the warnings
	 */
	public RequiresFeedback(String annotatedBody, boolean sufficient) {
		super();
		this.annotatedBody = annotatedBody;
		this.sufficient = sufficient;
	}
	
	/**
	 * @return the annotated method body
	 */
	public String getAnnotatedBody() {
		return annotatedBody;
	}
	
	/**
	 * @return <code>true</code> iff the precondition set was sufficient to eliminate all the warnings
	 */
	public boolean isSufficient() {
		return sufficient;
	}
}
