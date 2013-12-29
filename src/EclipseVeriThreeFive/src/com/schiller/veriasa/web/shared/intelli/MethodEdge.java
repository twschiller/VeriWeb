package com.schiller.veriasa.web.shared.intelli;

import java.io.Serializable;


@SuppressWarnings("serial")
public class MethodEdge extends IntelliEdge implements Serializable{

	private int arrity;
	
	public MethodEdge(String name, int arrity, String docHtml) {
		super(name, docHtml);
		this.arrity = arrity;
	}

	@SuppressWarnings("unused")
	private MethodEdge(){
		super();
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
