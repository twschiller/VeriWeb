package com.schiller.veriasa.web.shared.intelli;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A program element (i.e., type, method, field)
 * @author Todd Schiller
 */
public class IntelliNode implements Serializable{
	private static final long serialVersionUID = 2L;

	private String fullyQualifiedName;
	private String docHtml;
	
	private HashMap<IntelliEdge, IntelliNode> children;
	
	@SuppressWarnings("unused")
	private IntelliNode(){
	}
	
	/**
	 * Create a new node with no children
	 * @param fullyQualifiedName the fully qualified name
	 * @param docHtml the associated documentation
	 */
	public IntelliNode(String fullyQualifiedName, String docHtml) {
		this(fullyQualifiedName, docHtml, new HashMap<IntelliEdge, IntelliNode>());
	}

	/**
	 * Create a new node
	 * @param fullyQualifiedName the fully qualified name
	 * @param docHtml the associated documentation
	 * @param children the node's children
	 */
	public IntelliNode(
			String fullyQualifiedName,
			String docHtml,
			Map<IntelliEdge, IntelliNode> children) {
		
		super();
		this.fullyQualifiedName = fullyQualifiedName;
		this.docHtml = docHtml;
		this.children = new HashMap<IntelliEdge, IntelliNode>(children);
	}

	/**
	 * @return the fully qualified name
	 */
	public String getFullyQualifiedName() {
		return fullyQualifiedName;
	}

	/**
	 * @return the associated documentation
	 */
	public String getDocHtml() {
		return docHtml;
	}
	
	@Override
	public String toString(){
		return fullyQualifiedName;
	}
	
	public void setChildren(Map<IntelliEdge, IntelliNode> children){
		this.children = new HashMap<IntelliEdge, IntelliNode>(children);
	}
	
	public Map<IntelliEdge, IntelliNode> getChildren() {
		return Collections.unmodifiableMap(children);
	}
}
