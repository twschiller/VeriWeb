package com.schiller.veriasa.daikon;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DaikonTypeSet implements Serializable{
	private static final long serialVersionUID = 2L;

	private String qualifiedName;
	private List<String> invariants;
	private Map<String, DaikonMethodSet> invsByMethod;
	
	@SuppressWarnings("unused")
	private DaikonTypeSet(){
	}

	public DaikonTypeSet(String qualifiedName){
		this(
				qualifiedName, 
				new LinkedList<String>(), 
				new HashMap<String, DaikonMethodSet>());
	}

	public String getQualifiedName() {
		return qualifiedName;
	}

	public List<String> getInvariants() {
		return new LinkedList<String>(invariants);
	}
	
	public Map<String, DaikonMethodSet> getInvsByFunction() {
		return new HashMap<String, DaikonMethodSet>(invsByMethod);
	}
	
	public void addObjInvariant(String invariant){
		invariants.add(invariant);
	}
	
	public void addFunctionSet(DaikonMethodSet set){
		invsByMethod.put(set.getQualifiedName(), set);
	}

	public boolean hasFunction(String function){
		return invsByMethod.containsKey(function);
	}
	
	public void addFunctionInvariant(String method, String point, String inv){
		if (!invsByMethod.containsKey(method)){
			throw new IllegalArgumentException("No data for function " + method);
		}
		
		invsByMethod.get(method).addInvariant(point, inv);
	}

	
	
	public DaikonTypeSet(String qualifiedName, List<String> invariants,
			Map<String, DaikonMethodSet> invsByFunction) {
		super();
		this.qualifiedName = qualifiedName;
		this.invariants = new LinkedList<String>(invariants);
		this.invsByMethod = new HashMap<String, DaikonMethodSet>(invsByFunction);
	}
}
