package com.schiller.veriasa.web.shared.feedback;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.schiller.veriasa.web.shared.executejml.ValFragment;
import com.schiller.veriasa.web.shared.executejml.VarTreeNode;

/**
 * Feedback from executing a specification against a Chicory trace
 * @author Todd Schiller
 */
public class DynamicFeedback implements Serializable{
	
	private static final long serialVersionUID = 2L;
	
	private List<VarTreeNode> slice;
	
	private ValFragment fragment;
	
	public static interface HasDynamicFeedback {
		DynamicFeedback getDynamicFeedback();
		boolean hasDynamicFeedback();
	}

	public DynamicFeedback(List<VarTreeNode> slice, ValFragment fragment) {
		super();
		this.fragment = fragment;
		this.slice = new ArrayList<VarTreeNode>(slice);
	}
	
	@SuppressWarnings("unused")
	private DynamicFeedback(){
		this.fragment = null;
		this.slice = new ArrayList<VarTreeNode>();
	}
	
	public ValFragment getFragment() {
		return fragment;
	}
	public List<VarTreeNode> getSlice() {
		return slice;
	}	
}
