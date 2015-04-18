package com.schiller.veriasa.web.shared.intelli;

import java.io.Serializable;

/**
 * An edge representing a method relationship
 * @author Todd Schiller
 */
public class MethodEdge extends IntelliEdge implements Serializable{

	private static final long serialVersionUID = 2L;
	
	private int arrity;
	
	@SuppressWarnings("unused")
	private MethodEdge(){
	}

	/**
	 * Create a new method edge / relationship
	 * @param name the name of the method
	 * @param arrity the number of formal parameters
	 * @param docHtml the documentation for the method
	 */
	public MethodEdge(String name, int arrity, String docHtml) {
		super(name, docHtml);
		this.arrity = arrity;
	}

	/**
	 * @return the number of formal parameters of the method
	 */
	public int getArrity(){
		return arrity;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + arrity;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		MethodEdge other = (MethodEdge) obj;
		if (arrity != other.arrity)
			return false;
		return true;
	}	
}
