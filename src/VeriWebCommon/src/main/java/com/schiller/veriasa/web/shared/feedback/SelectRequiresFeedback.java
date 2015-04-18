package com.schiller.veriasa.web.shared.feedback;

import java.io.Serializable;


public class SelectRequiresFeedback extends RequiresFeedback implements Serializable{
	private static final long serialVersionUID = 1L;

	private SelectRequiresFeedback(){
		super(null,false);
	}
	
	/**
	 * @param annotatedBody the annotated method body
	 * @param sufficient <code>true</code> iff the precondition set was sufficient to eliminate all the warnings
	 */
	public SelectRequiresFeedback(String annotatedBody, boolean sufficient) {
		super(annotatedBody, sufficient);
	}
}
