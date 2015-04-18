package com.schiller.veriasa.callgraph;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Mutable call graph node with pointers to callees and callers
 * @author Todd Schiller
 */
public class CallGraphNode implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private String qualifiedMethodName;
	private List<CallGraphNode> callees = new ArrayList<CallGraphNode>();
	private List<CallGraphNode> callers = new ArrayList<CallGraphNode>();
	
	@SuppressWarnings("unused")
	private CallGraphNode(){
		
	}

	public CallGraphNode(String qualifiedMethodName) {
		this.qualifiedMethodName = qualifiedMethodName;
	}

	public String getQualifiedMethodName() {
		return qualifiedMethodName;
	}
	
	public List<CallGraphNode> getCallees() {
		return callees;
	}
	
	public List<CallGraphNode> getCallers() {
		return callers;
	}
	
	public void addCaller(CallGraphNode node){
		callers.add(node);
	}
	
	public void addCallee(CallGraphNode node){
		callees.add(node);
	}
	
	@Override
	public String toString() {
		return "CallGraphNode [name=" + qualifiedMethodName + "]";
	}
}
