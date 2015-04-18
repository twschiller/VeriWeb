package com.schiller.veriasa.web.shared.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A method specification
 * @author Todd Schiller
 */
public class MethodContract implements Serializable, HasQualifiedSignature{
	private static final long serialVersionUID = 2L;
	
	private String signature;
	private List<String> params;
	private boolean publik;
	
	private SourceElement info;
	
	private List<Clause> modifies;
	private List<Clause> requires;
	private List<Clause> ensures;
	
	/**
	 * <i>Simple</i> Exception Name -> List of Exsures Clauses
	 */
	private Map<String,List<Clause>> exsures;
	
	@SuppressWarnings("unused")
	private MethodContract(){
	}
	
	public MethodContract(String signature, List<String> params, boolean isPublic, SourceElement element){
		this(signature, params, isPublic, element, 
				new ArrayList<Clause>(),
				new ArrayList<Clause>(),
				new ArrayList<Clause>(),
				new HashMap<String, List<Clause>>());
	}
	
	public MethodContract(String signature, List<String> params, boolean isPublic, SourceElement element, List<String> exceptions){
		this(signature, params, isPublic, element, 
				new ArrayList<Clause>(),
				new ArrayList<Clause>(),
				new ArrayList<Clause>(),
				new HashMap<String, List<Clause>>());
		
		this.exsures = new HashMap<String, List<Clause>>();
		for (String ex : exceptions){
			this.exsures.put(ex, new ArrayList<Clause>());
		}
	}
	
	public MethodContract(String signature, 
			List<String> params,
			boolean isPublic,
			SourceElement info, 
			List<Clause> modifies,
			List<Clause> requires,
			List<Clause> ensures, 
			Map<String,List<Clause>> exsures) {
		
		this.params = new ArrayList<String>(params);
		this.info = info;
		this.publik = isPublic;
		this.signature = signature;
		this.modifies = new ArrayList<Clause>(modifies);
		this.ensures = new ArrayList<Clause>(ensures);
		
		// TODO the lists can leak out. fix the rep exposure
		this.exsures = new HashMap<String,List<Clause>>(exsures);
		
		this.requires = new ArrayList<Clause>(requires);
	}

	public SourceElement getInfo() {
		return info;
	}

	public List<Clause> getModifies() {
		return modifies;
	}

	public List<Clause> getEnsures() {
		return ensures;
	}

	public List<String> getParameters() {
		return params;
	}

	public boolean isPublic() {
		return publik;
	}

	/**
	 * Get a map from <i>simple</i> exception names to the list
	 * of exsures clauses for the exception
	 * @return a map from <i>simple</i> exception names to the list
	 * of exsures clauses for the exception
	 */
	public Map<String,List<Clause>> getExsures() {
		return exsures;
	}

	public List<Clause> getRequires() {
		return requires;
	}

	public String getSignature(){
		// TODO replace usage of this method with qualifiedSignature()
		
		return signature;
	}
	
	@Override
	public String qualifiedSignature() {
		return signature;
	}
	
	@Override
	public String toString(){
		return getSignature();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ensures == null) ? 0 : ensures.hashCode());
		result = prime * result + ((exsures == null) ? 0 : exsures.hashCode());
		result = prime * result + ((info == null) ? 0 : info.hashCode());
		result = prime * result
				+ ((modifies == null) ? 0 : modifies.hashCode());
		result = prime * result + ((params == null) ? 0 : params.hashCode());
		result = prime * result + (publik ? 1231 : 1237);
		result = prime * result
				+ ((requires == null) ? 0 : requires.hashCode());
		result = prime * result
				+ ((signature == null) ? 0 : signature.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MethodContract other = (MethodContract) obj;
		if (ensures == null) {
			if (other.ensures != null)
				return false;
		} else if (!ensures.equals(other.ensures))
			return false;
		if (exsures == null) {
			if (other.exsures != null)
				return false;
		} else if (!exsures.equals(other.exsures))
			return false;
		if (info == null) {
			if (other.info != null)
				return false;
		} else if (!info.equals(other.info))
			return false;
		if (modifies == null) {
			if (other.modifies != null)
				return false;
		} else if (!modifies.equals(other.modifies))
			return false;
		if (params == null) {
			if (other.params != null)
				return false;
		} else if (!params.equals(other.params))
			return false;
		if (publik != other.publik)
			return false;
		if (requires == null) {
			if (other.requires != null)
				return false;
		} else if (!requires.equals(other.requires))
			return false;
		if (signature == null) {
			if (other.signature != null)
				return false;
		} else if (!signature.equals(other.signature))
			return false;
		return true;
	}
}
