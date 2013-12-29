package com.schiller.veriasa.web.shared.core;

import java.io.Serializable;

/**
 * A contract clause
 * @author Todd Schiller
 */
public class Clause implements Serializable{
	private static final long serialVersionUID = 2L;

	public static enum Status { PENDING, KNOWN_GOOD, SYNTAX_BAD, KNOWN_BAD};
	
	private String clause;
	private String provenance;
	private Status status;
	private String reason;
	
	@SuppressWarnings("unused")
	private Clause(){		
	}
	
	/**
	 * Create a new clause with status <tt>status</tt>
	 * @param clause the clause
	 * @param provenance where the clause came from (e.g., Daikon)
	 * @param status the status of the clause
	 * @param reason explanation of <tt>status</tt>
	 */
	public Clause(String clause, String provenance, Status status, String reason){
		super();
		this.clause = clause;
		this.provenance = provenance;
		this.status = status;
		this.reason = reason;
	}
	
	/**
	 * Create a new {@link Status#PENDING} clause
	 * @param clause the clause
	 * @param provenance where the clause came from (e.g., Daikon)
	 */
	public Clause(String clause, String provenance) {
		this(clause, provenance, Status.PENDING);
	}
	
	/**
	 * Create a new clause with status <tt>status</tt> and no associated reason
	 * @param clause the clause
	 * @param provenance where the clause came from (e.g., Daikon)
	 * @param status the status of the clause
	 */
	public Clause(String clause, String provenance, Status status){
		this(clause, provenance, status, "");
	}
	
	/**
	 * @return the clause
	 */
	public String getClause() {
		return clause;
	}

	/**
	 * @return the source of the clause (e.g., Daikon)
	 */
	public String getProvenance() {
		return provenance;
	}

	/**
	 * @return the status of the clause
	 */
	public Status getStatus() {
		return status;
	}

	/**
	 * @return the reason for the clause's status
	 */
	public String getReason() {
		return reason;
	}

	@Override
	public String toString() {
		return "Spec [" + clause + ", provenance=" + provenance + ", status=" + status + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((clause == null) ? 0 : clause.hashCode());
		result = prime * result
				+ ((provenance == null) ? 0 : provenance.hashCode());
		result = prime * result + ((reason == null) ? 0 : reason.hashCode());
		result = prime * result + ((status == null) ? 0 : status.hashCode());
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
		Clause other = (Clause) obj;
		if (clause == null) {
			if (other.clause != null)
				return false;
		} else if (!clause.equals(other.clause))
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
