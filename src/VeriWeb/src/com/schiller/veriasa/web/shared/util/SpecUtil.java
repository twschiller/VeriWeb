package com.schiller.veriasa.web.shared.util;

import java.util.ArrayList;
import java.util.List;


import com.schiller.veriasa.web.shared.core.Clause;
import com.schiller.veriasa.web.shared.core.Clause.Status;

public class SpecUtil {

	/**
	 * <code>true</code> iff <code>specification</code>'s status is {@link Status#KNOWN_GOOD} or
	 * {@link Status#PENDING}.
	 * @param specification
	 * @return true iff <code>specification</code> is good or pending
	 */
	public static boolean isGoodOrPending(Clause specification){
		Clause.Status status = specification.getStatus();
		return status == Status.KNOWN_GOOD || status == Status.PENDING;
	}
	
	/**
	 * Returns a new list consisting of the {@link Status#KNOWN_GOOD} and {@link Status#PENDING} elements of <code>specifications</code>
	 * @param specifications the original specifications
	 * @return  a new list consisting of the good and pending elements of <code>specifications</code>
	 */
	public static List<Clause> goodOrPending(List<Clause> specifications){
		List<Clause> result = new ArrayList<Clause>();
		for (Clause specification : specifications){
			if (isGoodOrPending(specification)){
				result.add(specification);
			}
		}
		return result;
	}
	
	/**
	 * true iff the contracts are equal modulo leading / trailing whitespace
	 * @param lhs the first contracts
	 * @param rhs the other contracts
	 * @return true iff the contracts are equal modulo leading / trailing whitespace
	 */
	public static boolean invEqual(Clause lhs, String rhs){
		return lhs.getClause().trim().equals(rhs.trim());
	}
	
	/**
	 * true iff the contracts are equal modulo leading / trailing whitespace
	 * @param lhs the first contracts
	 * @param rhs the other contracts
	 * @return true iff the contracts are equal modulo leading / trailing whitespace
	 */
	public static boolean invEqual(Clause lhs, Clause rhs){
		return lhs.getClause().trim().equals(rhs.getClause().trim());
	}
	
	/**
	 * true iff any of the contracts in {@code specs} are equal to {@code n} modulo
	 * leading / trailing whitespace
	 * @param contracts contracts to test against
	 * @param query the query
	 * @return true if {@code n} is contained in {@code specs}
	 */
	public static boolean anyEqual(Iterable<Clause> contracts, String query){
		for (Clause s : contracts){
			if (invEqual(s,query)){
				return true;
			}
		}
		return false;
	}
	

	/**
	 * Remove @ symbol and line breaks from a string, then trim surrounding whitespace
	 * @param str
	 * @return
	 */
	public static String clean(String str){
		return str
			.replaceAll("@", " ")
			.replaceAll("\r\n", " ")
			.replaceAll("\n", " ")
			.trim();
	}
}
