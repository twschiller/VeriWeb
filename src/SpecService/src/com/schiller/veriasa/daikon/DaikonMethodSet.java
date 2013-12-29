package com.schiller.veriasa.daikon;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DaikonMethodSet implements Serializable{
	private static final long serialVersionUID = 2L;
	
	private String qualifiedName;
	private Map<String, List<String>> invsAtPoint;
	
	@SuppressWarnings("unused")
	private DaikonMethodSet(){
	}
	
	public DaikonMethodSet(String qualifiedName){
		this(qualifiedName, new HashMap<String, List<String>>());
	}

	public DaikonMethodSet(String qualifiedName, Map<String, List<String>> invsAtPoint){
		this.qualifiedName = qualifiedName;
		this.invsAtPoint = new HashMap<String, List<String>>(invsAtPoint);
	}

	public void addInvariant(String point, String inv){
		if (inv.trim().isEmpty()){
			return;
		}
		
		if (!invsAtPoint.containsKey(point)){
			invsAtPoint.put(point, new LinkedList<String>());
		}
		
		invsAtPoint.get(point).add(inv);
		
	}
	
	public String getQualifiedName() {
		return qualifiedName;
	}

	public Map<String, List<String>> getInvsAtPoint() {
		return new HashMap<String, List<String>>(invsAtPoint);
	}
}
