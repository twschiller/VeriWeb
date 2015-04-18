package com.schiller.veriasa.web.shared.executejml;

import java.io.Serializable;

import com.extjs.gxt.ui.client.data.BaseTreeModel;

public class VarTreeNode extends BaseTreeModel implements Serializable  {
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unused")
	private VarTreeNode(){
	}
	
	public VarTreeNode(String name, String before, String after) {
		super();
		set("name",name);
		set("before",before);
		set("after",after);
	}
	
	public void addChild(VarTreeNode n){
		children.add(n);
	}
	
	public String getName() {
		return (String) get("name");
	}

	public String getBeforeValue() {
		return (String) get("before");
	}

	public String getAfterValue() {
		return (String) get("after");
	}
}
