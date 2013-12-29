package com.schiller.veriasa.distance;

import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Iterables.find;
import static com.google.common.collect.Sets.difference;
import static com.google.common.collect.Sets.filter;
import static com.google.common.collect.Sets.newHashSet;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.schiller.veriasa.distance.util.Pod;
import com.schiller.veriasa.experiment.DataPoint;
import com.schiller.veriasa.web.server.SpecUtil;
import com.schiller.veriasa.web.server.Util;
import com.schiller.veriasa.web.shared.core.MethodContract;
import com.schiller.veriasa.web.shared.core.Clause;
import com.schiller.veriasa.web.shared.core.TypeSpecification;

/**
 * Methods for finding verification distance from one specification to a goal specification
 * @author Todd Schiller
 */
public class Distance {
	private static final Logger log = Logger.getLogger(Distance.class);
	
	public static final boolean DETECT_COMPLEX_SYNONYMS = false;
	
	public enum DistanceType {ECLIPSE, VERIWEB}
	
	/**
	 * Create a predicate that returns true iff an element is not a synonym
	 * for a subset of <code>cmp</code>
	 * @param cmp the comparison subset
	 * @return a predicate that returns true iff an element is not a synonym
	 * for a subset of <code>cmp</code>
	 */
	private static Predicate<String> notSynonym(final Collection<String> cmp){
		return new Predicate<String>(){
			@Override
			public boolean apply(String arg0) {
				return !Synonyms.same(arg0, cmp);
			}
		};
	}

	/**
	 * Extract invariants from <code>specs</code>
	 * @param x a collection of specifications
	 * @return the set of textual specifications from <code>x</code>
	 */
	private static Set<String> ex(Iterable<Clause> x){
		return newHashSet(Iterables.transform(x, SpecUtil.INV));
	}
	
	
	/**
	 * Return the subset of clauses in <code>x</code> that are synonyms for a subset of clauses in <code>y</code>
	 * @param x
	 * @param y
	 * @return the subset of clauses in <code>x</code> that are synonyms for a subset of clauses in <code>y</code>
	 */
	private static Set<String> synonyms(Set<String> x, Set<String> y){
		Set<String> ok1 = Sets.newHashSet();
		for (Set<String> s : Sets.powerSet(x)){
			if (Synonyms.same(s, y)){
				ok1.addAll(s);
			}
		}
		return ok1;
	}
	
	/**
	 * Calculate the difference between method preconditions
	 * @param normalizedCurrent the base preconditions set
	 * @param normalizedGoal the target (modified) preconditions set
	 * @param ignore predicate indicating clauses to ignore
	 * @return the difference between <code>normalizedCurrent</code> and <code>normalizedGoal</code>
	 */
	public static SpecDiff preDistance(Set<String> normalizedCurrent, Set<String> normalizedGoal, Predicate<String> ignore){
		
		final Collection<String> toRemove = filter(difference(normalizedCurrent, normalizedGoal), not(ignore));
		final Collection<String> toAdd = filter((difference(normalizedGoal, normalizedCurrent)), not(ignore));
				
		Collection<String> toRemoveRest = filter(toRemove, notSynonym(toAdd));
		Collection<String> toAddRest = filter(toAdd, notSynonym(toRemove));
	
		if (DETECT_COMPLEX_SYNONYMS){
			toRemoveRest.removeAll(synonyms(normalizedCurrent, normalizedGoal));
			toAddRest.removeAll(synonyms(normalizedGoal, normalizedCurrent));
		}
		
		SpecDiff d = new SpecDiff(newHashSet(toAddRest),newHashSet(toRemoveRest));
	
		return d;
	}
	public static SpecDiff postDistanceVeriWeb(Set<String> normalizedActive,Set<String> normalizedInactive, Set<String> normalizedGoal, Predicate<String> ignore){
		//the regular add remove set
		SpecDiff eclipse = postDistanceEclipse(normalizedActive, normalizedGoal, ignore);
		
		//remove inactive postconditions from add set
		Collection<String> active = filter(eclipse.getAdded(), notSynonym(normalizedInactive));
		
		//TODO: remove inactive postcondition based on multi equality
		
		return new SpecDiff(newHashSet(active), eclipse.getRemoved());
	}
	public static SpecDiff invDistance(Set<String> normalizedActive, Set<String> normalizedGoal, Predicate<String> ignore){
		final Collection<String> toRemove = filter(Sets.difference(normalizedActive, normalizedGoal), not(ignore));
		final Collection<String> toAdd = filter((Sets.difference(normalizedGoal, normalizedActive)), not(ignore));
		
		Collection<String> toRemoveRest = filter(toRemove, notSynonym(toAdd)); //to remove w/o synonym in add set
		Collection<String> toAddRest = filter(toAdd, notSynonym(toRemove));
		
		return new SpecDiff(newHashSet(toAddRest),newHashSet(toRemoveRest));
	}
	
	public static SpecDiff postDistanceEclipse(Set<String> normalizedActive, Set<String> normalizedGoal, Predicate<String> ignore){
		//textual differences
		final Collection<String> toRemove = filter(Sets.difference(normalizedActive, normalizedGoal), not(ignore));
		final Collection<String> toAdd = filter((Sets.difference(normalizedGoal, normalizedActive)), not(ignore));
		
		//remove 1-1 synonyms
		Collection<String> toRemoveRest = filter(toRemove, notSynonym(toAdd)); //to remove w/o synonym in add set
		Collection<String> toAddRest = filter(toAdd, notSynonym(toRemove));
		
		if (DETECT_COMPLEX_SYNONYMS){
			toRemoveRest.removeAll(synonyms(normalizedActive, normalizedGoal));
			toAddRest.removeAll(synonyms(normalizedGoal, normalizedActive));
		}
		
		return new SpecDiff(newHashSet(toAddRest), newHashSet(toRemoveRest));
	}
	
	/**
	 * Get the specification closest to <code>query</code> contained in <code>goals</code>.
	 * @param query
	 * @param goals 
	 * @param ignore a predicate decided specifications to ignore when calculating distance
	 * @param type the distance calculation to perform
	 * @param penalty
	 * @return
	 */
	public static TypeDistance nearest(
			TypeSpecification query, 
			HashMap<String,TypeSpecification> goals, 
			Predicate<String> ignore, 
			DistanceType type,
			int penalty){
		
		if (goals.isEmpty()){
			throw new IllegalArgumentException();
		}
		
		TypeDistance min = null;
		
		for (String goalName : goals.keySet()){
			TypeSpecification goal = goals.get(goalName);
			TypeDistance n = distance(query, goalName, goal, ignore, type, penalty);
			
			if (min == null || n.getDistance() < min.getDistance()){
				min = n;
			}			
		}
		return min;
	}
	
	public static TypeDistance distance(TypeSpecification query, String goalName, TypeSpecification goal, Predicate<String> ignore, DistanceType type, int penalty){

		HashMap<String, MethodDistance> ms = Maps.newHashMap();
		
		for (MethodContract queryM : query.getMethods()){
			MethodContract targetM = find(goal.getMethods(), new Util.SameSignature(queryM));
			
			SpecDiff pre = preDistance(ex(filter(queryM.getRequires(),SpecUtil.ACCEPT_GOOD)),ex(targetM.getRequires()),ignore);
			SpecDiff post = (type == DistanceType.ECLIPSE)
					? postDistanceEclipse(ex(queryM.getEnsures()), ex(targetM.getEnsures()), ignore)
					: postDistanceVeriWeb(ex(filter(queryM.getEnsures(),SpecUtil.ACCEPT_GOOD)), ex(queryM.getEnsures()), ex(targetM.getEnsures()),ignore);
				
			SpecDiff ex = null;
			
			if (targetM.getExsures().isEmpty() && queryM.getExsures().isEmpty()){
				ex = null;
			}else{
				Collection<Clause> queryAll = queryM.getExsures().get("RuntimeException");
				Collection<Clause> targetAll = targetM.getExsures().get("RuntimeException");
				
				queryAll = (queryAll == null) ? Sets.<Clause>newHashSet() : queryAll;
				targetAll = (targetAll == null) ? Sets.<Clause>newHashSet() : targetAll;
				
				ex = (type == DistanceType.ECLIPSE)
					? postDistanceEclipse(ex(queryAll),ex(targetAll),ignore)
					: postDistanceVeriWeb(ex(filter(queryAll,SpecUtil.ACCEPT_GOOD)), ex(queryAll), ex(targetAll),ignore);
			}
				
			MethodDistance xx = new MethodDistance(pre,post,ex);
			
			if (xx.getDistance() > 0){
				ms.put(queryM.qualifiedSignature(),xx);
			}
		}
		
		SpecDiff inv = invDistance(ex(query.getInvariants()), ex(goal.getInvariants()),ignore);
		
		return new TypeDistance(goalName, goal, query,  inv, ms, penalty);
	}
	
	
	public static DataPoint<TypeDistance> distance(
			String qualifiedType, 
			Pod entry, 
			HashMap<String,TypeSpecification> goals, 
			Predicate<String> ignore,
			Distance.DistanceType type,
			int penalty){
		
		TypeSpecification direct = entry.getSpec().forType(qualifiedType);
		TypeSpecification query = Normalize.normalize(direct);		
		return new DataPoint<TypeDistance>(entry.getTimestamp(),Distance.nearest(query, goals, ignore, type, penalty));
	}
	
	public static List<DataPoint<TypeDistance>> distance(
			String qualifiedType, 
			Iterable<Pod> entries, 
			HashMap<String,TypeSpecification> goals, 
			Predicate<String> ignore,
			Distance.DistanceType type){
		
		List<DataPoint<TypeDistance>> xs = Lists.newArrayList();
		for (Pod e : entries){
			try{
				DataPoint<TypeDistance> x = distance(qualifiedType, e, goals, ignore, type, e.getPenalty());
				xs.add(x);
			}catch(RuntimeException err){
				log.warn("Unexpected exception calculating distance; discarding datapoint", err);
			}
		}
		return xs;
	}

}

