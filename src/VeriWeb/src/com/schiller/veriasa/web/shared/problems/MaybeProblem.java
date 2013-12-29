package com.schiller.veriasa.web.shared.problems;

import java.io.Serializable;

/**
 * A {@link Problem}, or information for why no problem is given ({@link NoProblemInfo}).
 * @author Todd Schiller
 */
public class MaybeProblem implements Serializable {
	private static final long serialVersionUID = 2L;
	
	private Problem problem;
	private NoProblemInfo info;
	private long userId;
	
	@SuppressWarnings("unused")
	private MaybeProblem(){	
	}
	
	public MaybeProblem(long userId, Problem problem){
		this.userId = userId;
		this.problem = problem;
	}

	public MaybeProblem(long userId, NoProblemInfo info){
		this.userId = userId;
		this.info = info;
	}
	
	/**
	 * @return user id
	 */
	public long getUserId(){
		return userId;
	}
	
	/**
	 * @return the problem
	 */
	public Problem getProblem() {
		return problem;
	}
	
	/**
	 * @return the reason there is no problem
	 */
	public NoProblemInfo getInfo() {
		return info;
	}
	
	/**
	 * @return <code>true</code> iff a problem is present
	 */
	public boolean hasProblem(){
		return problem != null;
	}	
}
