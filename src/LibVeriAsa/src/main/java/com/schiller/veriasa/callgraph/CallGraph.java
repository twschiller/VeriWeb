package com.schiller.veriasa.callgraph;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Todd Schiller
 */
public class CallGraph implements Serializable{
	private static final long serialVersionUID = 1L;

	private Map<String, CallGraphNode> nodes = new HashMap<String, CallGraphNode>();
	
	private static Pattern nodePattern = Pattern.compile("^Node: (.*?)$");
	private static Pattern edgePattern = Pattern.compile("^Edge: (.*?) \\-\\> (.*?)$");
	
	private CallGraph(){
	}
	
	/**
	 * Returns the call graph node for method with signature <tt>qualifiedSignature</tt>
	 * @param qualifiedSignature
	 * @return the call graph node for method with signature <tt>qualifiedSignature</tt>
	 */
	public CallGraphNode getNode(String qualifiedSignature){
		return nodes.get(qualifiedSignature);
	}

	/**
	 * Returns methods directly or indirectly called by <tt>root</tt>
	 * @param root qualified signature of query
	 * @return methods directly or indirectly called by <tt>root</tt>
	 */
	public List<CallGraphNode> getCallees(String root){
		if (!nodes.containsKey(root)){
			throw new IllegalArgumentException("Graph does not contain node " + root);
		}
		return getCallees(nodes.get(root));
	}
		
	/**
	 * Returns methods directly or indirectly called by <tt>root</tt>
	 * @param root the method
	 * @return methods directly or indirectly called by <tt>root</tt>
	 */
	public List<CallGraphNode> getCallees(CallGraphNode root){
		if (root == null){
			throw new IllegalArgumentException("Root cannot be null");
		}
		
		LinkedList<CallGraphNode> result = new LinkedList<CallGraphNode>();
		for (CallGraphNode callee : root.getCallees()){
			result.add(callee);
		}
		for (CallGraphNode callee : root.getCallees()){
			result.addAll(getCallees(callee));
		}
		return result;
	}
	
	/**
	 * Read a call graph description from a file
	 * @param file the file
	 * @param allowImplicit true iff undeclared methods can be used in edges
	 * @return the callgraph
	 * @throws IOException
	 */
	public static CallGraph readFromFile(File file, boolean allowImplicit) throws IOException{
		BufferedReader in = new BufferedReader(new FileReader(file));
		
		CallGraph result = new CallGraph();
		
		String line;
		while ((line = in.readLine()) != null){
			Matcher nodeMatch = nodePattern.matcher(line);
			Matcher edgeMatch = edgePattern.matcher(line);
			
			if (nodeMatch.matches()){
				String name = nodeMatch.group(1);
				result.nodes.put(name, new CallGraphNode(name));
			}else if(edgeMatch.matches()){
				String caller = edgeMatch.group(1).trim();
				String callee = edgeMatch.group(2).trim();
				
				if (!result.nodes.containsKey(caller)){
					if (allowImplicit){
						result.nodes.put(caller, new CallGraphNode(caller));
					}else{
						throw new RuntimeException("Undeclared method used in edge: " + caller);
					}
				}
				
				if (!result.nodes.containsKey(callee)){
					if (allowImplicit){
						result.nodes.put(caller, new CallGraphNode(callee));
					}else{
						throw new RuntimeException("Undeclared method used in edge: " + callee);
					}
				}
				
				result.nodes.get(caller).addCallee(result.nodes.get(callee));
				result.nodes.get(callee).addCaller(result.nodes.get(caller));
			}
		}
		
		in.close();
		return result;
	}
	
	/**
	 * @return topologically sorted list of nodes in the callgraph
	 */
	public List<CallGraphNode> topologicalSort(){
		List<CallGraphNode> result = new ArrayList<CallGraphNode>();
		
		Set<CallGraphNode> visited = new HashSet<CallGraphNode>();
		for (CallGraphNode node : nodes.values()){
			if (node.getCallers().isEmpty()){
				topoSortHelper(result, visited, node);
			}
		}
		
		return result;
	}
	
	private void topoSortHelper(List<CallGraphNode> sorted, Set<CallGraphNode> visited, CallGraphNode node){
		if (!visited.contains(node)){
			visited.add(node);
		
			for (CallGraphNode callee : node.getCallees()){
				topoSortHelper(sorted, visited, callee);
			}
			
			sorted.add(node);
		}
	}
}
