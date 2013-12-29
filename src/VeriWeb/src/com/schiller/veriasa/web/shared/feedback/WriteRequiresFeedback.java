package com.schiller.veriasa.web.shared.feedback;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.schiller.veriasa.web.shared.core.Clause;
import com.schiller.veriasa.web.shared.feedback.DynamicFeedback.HasDynamicFeedback;

public class WriteRequiresFeedback extends RequiresFeedback implements HasDynamicFeedback{
	private static final long serialVersionUID = 2L;

	private List<Clause> feedback;
	private DynamicFeedback dynamicFeedback;
	
	private WriteRequiresFeedback(){
		super(null, false);
	}
	
	public WriteRequiresFeedback(String annotatedBody, boolean sufficient, List<Clause> feedback, DynamicFeedback dynamicFeedback) {
		super(annotatedBody, sufficient);
		this.feedback = new LinkedList<Clause>(feedback);
		this.dynamicFeedback = dynamicFeedback;
	}

	public List<Clause> getFeedback() {
		return Collections.unmodifiableList(feedback);
	}
	
	@Override
	public DynamicFeedback getDynamicFeedback() {
		return dynamicFeedback;
	}

	@Override
	public boolean hasDynamicFeedback() {
		return dynamicFeedback != null;
	}
}
