package com.schiller.veriasa.web.shared.problems;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.schiller.veriasa.web.shared.core.Clause;

public class MethodDocumentation extends Documentation implements Serializable {

	private static final long serialVersionUID = 2L;

	private String signature;
	private List<Clause> requires;
	private List<Clause> ensures;
	private Map<String,List<Clause>> exsures;
	
	private Set<Clause> badRequires;
	private Set<Clause> badEnsures;
	private Map<String,Set<Clause>> badExsures;
	
	private MethodDocumentation(){
		super("");	
	}
	
	public MethodDocumentation(String signature,
			String documentationHtml,
			List<Clause> requires,
			List<Clause> ensures,
			Map<String, List<Clause>> exsures,
			Set<Clause> badRequires,
			Set<Clause> badEnsures,
			Map<String,Set<Clause>> badExsures){
		
		super(documentationHtml);
		this.signature = signature;
		this.requires = new LinkedList<Clause>(requires);
		this.ensures = new LinkedList<Clause>(ensures);
		this.exsures = new HashMap<String,List<Clause>>(exsures);
		this.badRequires = new HashSet<Clause>(badRequires);
		this.badEnsures = new HashSet<Clause>(badEnsures);
		this.badExsures = new HashMap<String,Set<Clause>>(badExsures);
	}

	public String getSignature(){
		return signature;
	}
	
	public List<Clause> getRequires() {
		return requires;
	}

	public List<Clause> getEnsures() {
		return ensures;
	}

	public Map<String,List<Clause>> getExsures() {
		return exsures;
	}

	public Map<String, Set<Clause>> getBadExsures() {
		return badExsures;
	}

	public Set<Clause> getBadEnsures() {
		return badEnsures;
	}

	public Set<Clause> getBadRequires() {
		return badRequires;
	}
}
