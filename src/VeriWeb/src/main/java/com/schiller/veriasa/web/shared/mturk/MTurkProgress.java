package com.schiller.veriasa.web.shared.mturk;

import java.io.Serializable;

/**
 * Information about the problems solved by an MTurk worker, and the money
 * earned by the MTurk worker
 * @author Todd Schiller
 */
public class MTurkProgress implements Serializable {
	private static final long serialVersionUID = 1L;

	private int numSolved;
	private int numPreviewSolved;
	private double rate;
	private double earned;
	
	@SuppressWarnings("unused")
	private MTurkProgress(){
	}
	
	/**
	 * @param numSolved number of problems the worker has solved
	 * @param numPreviewSolved number of (preview) problems the worker has solved
	 * @param rate the amount paid the worker per problem
	 * @param earned the total amount the worker has earned
	 */
	public MTurkProgress(int numSolved, int numPreviewSolved, double rate, double earned) {
		
		super();
		this.numSolved = numSolved;
		this.numPreviewSolved = numPreviewSolved;
		this.rate = rate;
		this.earned = earned;
	}

	/**
	 * @return the number of problems the worker has solved
	 */
	public int getNumSolved() {
		return numSolved;
	}

	/**
	 * @return the number of (preview) problems the worker has solved
	 */
	public int getNumPreviewSolved() {
		return numPreviewSolved;
	}

	/**
	 * @return the amount paid the worker per problem
	 */
	public double getRate() {
		return rate;
	}

	/**
	 * @return the total amount the worker has earned
	 */
	public double getEarned() {
		return earned;
	}
}
