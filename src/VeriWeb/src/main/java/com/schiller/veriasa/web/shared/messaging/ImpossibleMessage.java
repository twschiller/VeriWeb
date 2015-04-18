package com.schiller.veriasa.web.shared.messaging;

import java.io.Serializable;

import com.schiller.veriasa.web.shared.core.Clause;
import com.schiller.veriasa.web.shared.solutions.ImpossibleInfo;

/**
 * A user message indicating that a problem was impossible
 * @author Todd Schiller
 */
public class ImpossibleMessage extends UserMessage implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private ImpossibleInfo.Reason reason;
	
	public ImpossibleMessage(long userId, String sourceMethod, Clause sourceSpec, String comment,
			Vote vote, ImpossibleInfo.Reason reason) {
		super(userId, sourceMethod, sourceSpec == null ? null : sourceSpec.getClause(), comment, vote);
		this.reason = reason;
	}
	
	private ImpossibleMessage(long userId, String sourceMethod, String sourceSpec, String comment,
			Vote vote) {
		super(userId, sourceMethod, sourceSpec, comment, vote);
	}
	
	public ImpossibleMessage(long userId, String sourceMethod, String sourceSpec, String comment,
			Vote vote, ImpossibleInfo.Reason reason) {
		super(userId, sourceMethod, sourceSpec, comment, vote);
		this.reason = reason;
	}
	
	@SuppressWarnings("unused")
	private ImpossibleMessage(){
		this(0L,"",(Clause) null,null, Vote.NO_VOTE, null);
	}
	
	public ImpossibleInfo.Reason getReason() {
		return reason;
	}	
}
