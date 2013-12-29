package com.schiller.veriasa.web.shared.intelli;

import java.io.Serializable;

@SuppressWarnings("serial")
public abstract class IntelliEdge implements Serializable{

	private String name;
	private String docHtml;
	
	protected IntelliEdge(){
		
	}
	
	public IntelliEdge(String name, String docHtml) {
		super();
		this.name = name;
		this.docHtml = docHtml;
	}
	public String getName() {
		return name;
	}
	public String getDocHtml() {
		return docHtml;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((docHtml == null) ? 0 : docHtml.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		IntelliEdge other = (IntelliEdge) obj;
		if (docHtml == null) {
			if (other.docHtml != null)
				return false;
		} else if (!docHtml.equals(other.docHtml))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
}
