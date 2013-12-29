package com.schiller.veriasa.web.shared;

import java.io.Serializable;

@SuppressWarnings("serial")
public final class Spec implements Serializable{
	public static enum Status { PENDING, KNOWN_GOOD, SYNTAX_BAD, KNOWN_BAD};
	
	private String invariant;
	private String provenance;
	private Status status;
	private String reason;
	
	@SuppressWarnings("unused")
	private Spec(){
		
	}
	
	public Spec(String invariant, String provenance) {
		this(invariant, provenance, Status.PENDING);
	}

	public Spec(String invariant, String provenance, Status status, String reason){
		super();
		this.invariant = invariant;
		this.provenance = provenance;
		this.status = status;
		this.reason = reason;
	}
	
	public Spec(String invariant, String provenance, Status status){
		this(invariant,provenance, status, "");
	}
	
	public String getInvariant() {
		return invariant;
	}

	public String getProvenance() {
		return provenance;
	}

	public Status getStatus() {
		return status;
	}

	public String getReason() {
		return reason;
	}

	@Override
	public String toString() {
		return "Spec [" + invariant + ", provenance=" + provenance
				+ ", status=" + status + "]";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((invariant == null) ? 0 : invariant.hashCode());
		result = prime * result
				+ ((provenance == null) ? 0 : provenance.hashCode());
		result = prime * result + ((reason == null) ? 0 : reason.hashCode());
		result = prime * result + ((status == null) ? 0 : status.hashCode());
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
		Spec other = (Spec) obj;
		if (invariant == null) {
			if (other.invariant != null)
				return false;
		} else if (!invariant.equals(other.invariant))
			return false;
		if (provenance == null) {
			if (other.provenance != null)
				return false;
		} else if (!provenance.equals(other.provenance))
			return false;
		if (reason == null) {
			if (other.reason != null)
				return false;
		} else if (!reason.equals(other.reason))
			return false;
		if (status != other.status)
			return false;
		return true;
	}

	
	
}
