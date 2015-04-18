package com.schiller.veriasa.web.shared.escj;

import java.io.Serializable;

import com.schiller.veriasa.web.shared.core.ProjectSpecification;

/**
 * ESC/Java2 server request
 * @author Todd Schiller
 */
public class VerificationRequest implements Serializable{
	private static final long serialVersionUID = 2L;
	
	private ProjectSpecification specification;
	
	@SuppressWarnings("unused")
	private VerificationRequest(){	
	}

	/**
	 * A request to check <tt>specification</tt>
	 * @param specification the specification to check
	 */
	public VerificationRequest(ProjectSpecification specification) {
		super();
		this.specification = specification;
	}

	/**
	 * @return the project specification
	 */
	public ProjectSpecification getProject() {
		return specification;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((specification == null) ? 0 : specification.hashCode());
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
		VerificationRequest other = (VerificationRequest) obj;
		if (specification == null) {
			if (other.specification != null)
				return false;
		} else if (!specification.equals(other.specification))
			return false;
		return true;
	}
}
