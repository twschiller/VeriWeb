package com.schiller.veriasa.web.shared.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fluently build method specifications
 * @author Todd Schiller
 */
public class MethodSpecBuilder {
	private String signature;
	private List<String> params;
	private boolean pub;
	
	private SourceElement info;
	private List<Clause> modifies;
	private List<Clause> ensures;
	
	/**
	 * <i>Simple</i> Exception Name -> List of Exsures Clauses
	 */
	private Map<String,List<Clause>> exsures;
	
	private List<Clause> requires;
	
	public MethodSpecBuilder(MethodContract base){
		this.signature = base.getSignature();
		this.params = new ArrayList<String>(base.getParameters());
		this.pub = base.isPublic();
		this.info = base.getInfo();
		this.modifies = new ArrayList<Clause>(base.getModifies());
		this.requires = new ArrayList<Clause>(base.getRequires());
		this.ensures = new ArrayList<Clause>(base.getEnsures());
		this.exsures = new HashMap<String,List<Clause>>(base.getExsures());
	}
	
	public static MethodSpecBuilder builder(MethodContract base){
		return new MethodSpecBuilder(base);
	}
	public MethodSpecBuilder setModifies(List<Clause> modifies){
		this.modifies = new ArrayList<Clause>(modifies);
		return this;
	}
	public MethodSpecBuilder setRequires(List<Clause> requires){
		this.requires = new ArrayList<Clause>(requires);
		return this;
	}
	public MethodSpecBuilder setEnsures(List<Clause> ensures){
		this.ensures = new ArrayList<Clause>(ensures);
		return this;
	}
	public MethodSpecBuilder setExsures(Map<String,List<Clause>> exsures){
		this.exsures = new HashMap<String,List<Clause>>(exsures);
		return this;
	}
	
	public MethodSpecBuilder setExsures(String exception, List<Clause> exsures){
		this.exsures = new HashMap<String,List<Clause>>();
		this.exsures.put(exception, new ArrayList<Clause>(exsures));
		return this;
	}
	
	public MethodContract getSpec(){
		return new MethodContract(signature, params, pub, info, modifies,requires, ensures, exsures);
	}
}
