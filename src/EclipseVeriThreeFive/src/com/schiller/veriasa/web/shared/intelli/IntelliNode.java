package com.schiller.veriasa.web.shared.intelli;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("serial")
public class IntelliNode implements Serializable{
	
	private String fullyQualifiedName;
	private String docHtml;
	
	private HashMap<IntelliEdge, IntelliNode> children;
	
	@SuppressWarnings("unused")
	private IntelliNode(){
		
	}
	
	public IntelliNode(String fullyQualifiedName, String docHtml) {
		this(fullyQualifiedName,docHtml,new HashMap<IntelliEdge, IntelliNode>());
	}

	public IntelliNode(String fullyQualifiedName,
			String docHtml,
			Map<IntelliEdge, IntelliNode> children) {
		super();
		this.fullyQualifiedName = fullyQualifiedName;
		this.docHtml = docHtml;
		this.children = new HashMap<IntelliEdge, IntelliNode>(children);
	}

	public String getFullyQualifiedName() {
		return fullyQualifiedName;
	}

	public String getDocHtml() {
		return docHtml;
	}

	public String toString(){
		return "Type:" + fullyQualifiedName;
	}
	
	public void setChildren(Map<IntelliEdge, IntelliNode> children) {
		this.children = new HashMap<IntelliEdge,IntelliNode>(children);
	}

	public void setDocHtml(String docHtml) {
		this.docHtml = docHtml;
	}

	public Map<IntelliEdge, IntelliNode> getChildren() {
		return Collections.unmodifiableMap(children);
	}
}
