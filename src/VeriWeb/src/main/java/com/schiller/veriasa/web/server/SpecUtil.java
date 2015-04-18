package com.schiller.veriasa.web.server;

import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.schiller.veriasa.web.shared.core.Clause;
import com.schiller.veriasa.web.shared.core.Clause.Status;

/**
 * Specification statement utility functions
 * @author Todd Schiller
 */
public abstract class SpecUtil {

	/**
	 * Clear reason and set status to {@link Status#KNOWN_GOOD}
	 */
	public static ResetStatus RESET_GOOD = new ResetStatus(Status.KNOWN_GOOD);
	
	/**
	 * Clear reason and set status to {@link Status#PENDING}
	 */
	public static ResetStatus RESET_PENDING = new ResetStatus(Status.PENDING);
	
	/**
	 * true iff spec's status is not syntax bad
	 */
	public static final Predicate<Clause> REJECT_SYNTAX_BAD = Predicates.not(makeStatusPredicate(Status.SYNTAX_BAD));
	
	/**
	 * true iff spec's status is pending or known bad
	 */
	public static final Predicate<Clause> ACCEPT_BAD = Predicates.or(makeStatusPredicate(Status.KNOWN_BAD), makeStatusPredicate(Status.SYNTAX_BAD));

	/**
	 * true iff spec's status is not known bad or syntax bad
	 */
	public static final Predicate<Clause> REJECT_BAD = Predicates.not(ACCEPT_BAD);
	
	/**
	 * true iff spec's status if known good
	 */
	public static final Predicate<Clause> ACCEPT_GOOD = makeStatusPredicate(Status.KNOWN_GOOD);
	
	/**
	 * true iff <code>lhs</code> and <code>rhs</code> are textually equivalent modulo
	 * surrounding whitespace and capitalization
	 * @param lhs
	 * @param rhs
	 * @return true iff <code>lhs</code> and <code>rhs</code> are textually equivalent
	 */
	private static boolean specEq(Clause lhs, Clause rhs){
		return lhs.getClause().trim().equalsIgnoreCase(rhs.getClause().trim());
	}
	
	/**
	 * Remove surrounding whitespace from JML statement, and add a trailing semicolon iff needed
	 * @param jmlStatement the JML statement
	 * @return Remove surrounding whitespace from JML statement, and add a trailing semicolon iff needed
	 */
	public static String clean(String jmlStatement){
		String trimmed = jmlStatement.trim();	
		return trimmed.endsWith(";") ? trimmed : (trimmed + ";");
	}
	
	/**
	 * returns a predicate that returns true iff the specifications are textually equivalent, 
	 * ignoring surrounding whitespace and capitalization
	 * @param query the specification to compare to
	 * @return a predicate determining whether a specification is equivalent to <code>query</code>
	 */
	public static Predicate<Clause> invEq(final Clause query){
		return new Predicate<Clause>(){
			@Override
			public boolean apply(Clause t) {
				return specEq(t,query);
			}
		};
	}
	
	/**
	 * Returns the specification string from a specification
	 */
	public static final Function<Clause, String> INV = new Function<Clause, String>(){
		@Override
		public String apply(Clause specification) {
			return specification.getClause();
		}
	};
	
	/**
	 * Changes the status without resetting the reason
	 * @author Todd Schiller
	 */
	public static class ChangeStatus implements Function<Clause, Clause>{
		private final Status status;
		
		public ChangeStatus(Status status){
			this.status = status;
		}
		
		@Override
		public Clause apply(Clause specification) {
			return new Clause(specification.getClause(), specification.getProvenance(), status, specification.getReason());
		}
	}
	
	
	/**
	 * Changes the status and resets the reason
	 * @author Todd Schiller
	 */
	public static class ResetStatus implements Function<Clause,Clause>{
		private final Status status;
		
		public ResetStatus(Status status){
			this.status = status;
		}
		
		@Override
		public Clause apply(Clause specification) {
			return new Clause(specification.getClause(), specification.getProvenance(), status, null);
		}
	}
	
	/**
	 * Is there something in the new set that wasn't in the old set?
	 * @param before
	 * @param after
	 * @return
	 */
	public static boolean isPreconditionSetStronger(Iterable<Clause> before, Iterable<Clause> after){
		for (Clause good : Iterables.filter(after, SpecUtil.ACCEPT_GOOD)){
			try{
				Clause a = Iterables.find(before, invEq(good));
				if (!a.getStatus().equals(Status.KNOWN_GOOD)){
					return true;
				}
			}catch(NoSuchElementException ex){
				return true;
			}
		}
		return false;
	}
	

	/**
	 * Is there something in the old set that isn't in the new set?
	 * @param before the old preconditions
	 * @param after the new preconditions
	 * @return 
	 */
	public static boolean isPreconditionSetWeaker(Iterable<Clause> before, Iterable<Clause> after){
		for (Clause good : Iterables.filter(before, SpecUtil.ACCEPT_GOOD)){
			try{
				Clause a = Iterables.find(after, invEq(good));
				if (!a.getStatus().equals(Status.KNOWN_GOOD)){
					return true;
				}
			}catch(NoSuchElementException ex){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Is there something in the old set that isn't in the new set?
	 * @param before the old postconditions
	 * @param after the new postconditions
	 * @return
	 */
	public static boolean isPostconditionSetWeaker(Iterable<Clause> before, Iterable<Clause> after){
		for (Clause good : Iterables.filter(before,SpecUtil.ACCEPT_GOOD)){
			try{
				Clause a = Iterables.find(after, invEq(good));
				if (!a.getStatus().equals(Status.KNOWN_GOOD)){
					return true;
				}
			}catch(NoSuchElementException ex){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Extend <code>base</code> specifications with specifications in <code>additional</code>.
	 * Uses {@link SpecUtil#invEq(Clause)} to determine the new specifications.
	 * @param base the base specifications
	 * @param additional the additional specifications
	 * @return the extended specifications
	 */
	public static List<Clause> extend(Collection<Clause> base, Collection<Clause> additional){
		List<Clause> result = Lists.newArrayList(base);
		for (Clause x : additional){
			// skip duplicates
			if (!Iterables.any(result, invEq(x))){
				result.add(x);
			}
		}	
		return result;
	}
	
	/**
	 * Update the status and reason for specification statement <code>x</code> in specifications <code>xs</code>
	 * @param x the specification to update
	 * @param xs list of specifications
	 * @param status the new status
	 * @param reason the new reason
	 */
	public static void resetStatus(Clause x, List<Clause> xs, Status status, String reason){
		for (int i = 0; i < xs.size(); i++){
			if (specEq(x, xs.get(i))){
				xs.set(i, new Clause(x.getClause(), x.getProvenance(), status, reason));
				return;
			}
		}
		throw new RuntimeException(x + " does not occur in the list");
	}
	
	/**
	 * Update specifications with <code>oldStatus</code> to have the <code>newStatus</code>, preserving
	 * the reason
	 * @param xs list of specifications
	 * @param status the old status
	 * @param reason the new status
	 */
	public static void changeStatus(List<Clause> xs, Status oldStatus, Status newStatus){
		for (int i = 0; i < xs.size(); i++){
			Clause x = xs.get(i);
			
			if (x.getStatus() == oldStatus){
				xs.set(i, new Clause(x.getClause(), x.getProvenance(), newStatus, x.getReason()));
				return;
			}
		}
	}
	
	/**
	 * Add <code>statement</code> to <code>statements</code>. If a textually equivalent statement
	 * already exists, then it is overwritten
	 * @param statements the old set of statements
	 * @param statement the statement to add
	 */
	public static void add(List<Clause> statements, Clause statement){
		for (int i = 0; i < statements.size(); i++){
			Clause x = statements.get(i);
			
			if (specEq(x, statement)){
				statements.set(i, statement);
				return;
			}
		}
		
		statements.add(statement);
	}
	
	/**
	 * Update specifications with <code>oldStatus</code> to have the <code>newStatus</code>, clearing
	 * the reason
	 * @param xs list of specifications
	 * @param status the old status
	 * @param reason the new status
	 */
	public static void resetStatus(List<Clause> xs, Status oldStatus, Status newStatus){
		for (int i = 0; i < xs.size(); i++){
			Clause x = xs.get(i);
			
			if (x.getStatus() == oldStatus){
				xs.set(i, new Clause(x.getClause(), x.getProvenance(), newStatus, null));
				return;
			}
		}
	}


	/**
	 * Calculate the intersection between <code>lhs</code> and <code>rhs</code>.
	 * Uses {@link SpecUtil#invEq(Clause)} to determine equality.
	 * @param lhs 
	 * @param rhs
	 * @return the intersection between <code>lhs</code> and <code>rhs</code>
	 */
	public static Set<Clause> intersect(Iterable<Clause> lhs, Iterable<Clause> rhs){
		Set<Clause> result = Sets.newHashSet();
		for (Clause x : lhs){
			if (Iterables.any(rhs, invEq(x))){
				result.add(x);
			}
		}
		return result;
	}

	
	/**
	 * true iff <code>lhs</code> is a subset of <code>rhs</code>. Uses
	 * {@link SpecUtil#invEq(Clause)} to determine specification equality.
	 * @param lhs
	 * @param rhs
	 * @return true iff <code>lhs</code> is a subset of <code>rhs</code>
	 */
	public static boolean isSubset(Iterable<Clause> lhs, Iterable<Clause> rhs){
		for (Clause a : lhs){
			if (!Iterables.any(rhs, invEq(a))){
				return false;
			}
		}
		return true;
	}
	
	/**
	 * true iff <code>collection</code> contains <code>query</code>. Uses
	 * {@link SpecUtil#invEq(Clause)} to determine specification equality.
	 * @param collection collection of statements
	 * @param query the query
	 * @return true iff <code>collection</code> contains <code>query</code>
	 */
	public static boolean contains(Collection<Clause> collection, Clause query){
		return Iterables.any(collection, invEq(query));
	}
	
	/**
	 * true iff <code>lhs</code> is a subset of <code>rhs</code>, and <code>rhs</code> 
	 * is a subset of <code>lhs</code>. Uses {@link SpecUtil#isSubset(Iterable, Iterable)}.
	 * @param lhs a collection of specifications
	 * @param rhs a collection of specifications
	 * @return true iff <code>lhs</code> is equivalent to <code>rhs</code>
	 */
	public static boolean equals(Iterable<Clause> lhs, Iterable<Clause> rhs){
		return isSubset(lhs,rhs) && isSubset(rhs, lhs);
	}
		
	/**
	 * Get a predicate that accepts specifications with status <code>accept</code>
	 * @param accept the status to accept
	 * @return  a predicate that accepts specifications with status <code>accept</code>
	 */
	private static Predicate<Clause> makeStatusPredicate(final Status accept){
		return new Predicate<Clause>(){
			@Override
			public boolean apply(Clause specification) {
				return specification != null && specification.getStatus() == accept;
			}
		};
	}
}
