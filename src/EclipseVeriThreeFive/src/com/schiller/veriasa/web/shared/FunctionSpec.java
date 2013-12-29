package com.schiller.veriasa.web.shared;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("serial")
public class FunctionSpec implements Serializable {
	private String signature;
	private List<String> params;
	private boolean pub;
	
	private ElementInfo info;
	private List<Spec> modifies;
	private List<Spec> ensures;
	
	/**
	 * <i>Simple</i> Exception Name -> List of Exsures Clauses
	 */
	private Map<String,List<Spec>> exsures;
	
	private List<Spec> requires;
	
	@SuppressWarnings("unused")
	private FunctionSpec(){
		
	}
	
	@SuppressWarnings("unchecked")
	public FunctionSpec(String signature, List<String> params, boolean isPublic,ElementInfo fun){
		this(signature,
				params,
				isPublic,
				fun, 
				Collections.EMPTY_LIST,
				Collections.EMPTY_LIST,
				Collections.EMPTY_LIST,
				Collections.EMPTY_MAP);
	}
	
	@SuppressWarnings("unchecked")
	public FunctionSpec(String signature,List<String> params, boolean isPublic,ElementInfo fun, List<String> exceptions){
		this(signature,
				params,
				isPublic,
				fun, 
				Collections.EMPTY_LIST,
				Collections.EMPTY_LIST,
				Collections.EMPTY_LIST,
				Collections.EMPTY_MAP);
		
		this.exsures = new HashMap<String, List<Spec>>();
		
		for (String ex : exceptions){
			this.exsures.put(ex, new LinkedList<Spec>());
		}
	}
	
	public FunctionSpec(String signature, 
			List<String> params,
			boolean isPublic,
			ElementInfo info, List<Spec> modifies,
			List<Spec> requires,
			List<Spec> ensures, 
			Map<String,List<Spec>> exsures) {
		super();
		this.info = info;
		this.pub = isPublic;
		this.params = new LinkedList<String>(params);
		this.signature = signature;
		this.modifies = new LinkedList<Spec>(modifies);
		this.ensures = new LinkedList<Spec>(ensures);
		
		//TODO: the lists can leak out. fix the rep exposure
		this.exsures = new HashMap<String,List<Spec>>(exsures);
		
		this.requires = new LinkedList<Spec>(requires);
	}

	public ElementInfo getInfo() {
		return info;
	}

	public String getSignature(){
		return signature;
	}
	
	public List<Spec> getModifies() {
		return Collections.unmodifiableList(modifies);
	}

	public List<Spec> getEnsures() {
		return Collections.unmodifiableList(ensures);
	}

	public List<String> getParams() {
		return params;
	}

	public boolean isPublic() {
		return pub;
	}

	/**
	 * Get a map from <i>simple</i> exception names to the list
	 * of exsures clauses for the exception
	 * @return a map from <i>simple</i> exception names to the list
	 * of exsures clauses for the exception
	 */
	public Map<String,List<Spec>> getExsures() {
		return Collections.unmodifiableMap(exsures);
	}

	public List<Spec> getRequires() {
		return Collections.unmodifiableList(requires);
	}

	@Override
	public String toString(){
		return getSignature();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
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
		result = prime * result + (pub ? 1231 : 1237);
		result = prime * result
				+ ((requires == null) ? 0 : requires.hashCode());
		result = prime * result
				+ ((signature == null) ? 0 : signature.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FunctionSpec other = (FunctionSpec) obj;
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
		if (pub != other.pub)
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
