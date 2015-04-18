package com.schiller.veriasa.web.server.callgraph;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.apache.log4j.Logger;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.schiller.veriasa.web.server.User;
import com.schiller.veriasa.web.shared.core.HasQualifiedSignature;
import com.schiller.veriasa.web.shared.problems.MethodProblem;
import com.schiller.veriasa.web.shared.problems.Problem;
import com.schiller.veriasa.web.shared.problems.SelectRequiresProblem;
import com.schiller.veriasa.web.shared.problems.WriteExsuresProblem;
import com.schiller.veriasa.web.shared.problems.WriteRequiresProblem;

/**
 * Mutable subproblem graph
 * @author Todd Schiller
 */
public class ProblemGraph implements Serializable{
	
	
	private static final long serialVersionUID = 3L;
	
	private static Logger log = Logger.getLogger(ProblemGraph.class);
	
	/**
	 * The subproblems in the graph
	 */
	private Set<ProblemGraphNode> problems = Sets.newHashSet();
	
	/**
	 * Map of problems currently in progress to the user working on the problem
	 */
	private transient HashMap<ProblemGraphNode, User> inProgress = Maps.newHashMap();
	
	/**
	 * Problems current in progress that have been invalidated by other workers
	 */
	private transient HashSet<ProblemGraphNode> invalidated = Sets.newHashSet();
	
	public void init(){
		inProgress = Maps.newHashMap();
		invalidated = Sets.newHashSet();
	}
	
	/**
	 * Create an empty subproblem graph
	 */
	public ProblemGraph(){
	}
	
	/**
	 * true iff the graph node is a leaf, i.e.:
	 * <ul>
	 * 	<li>the node is not enabled or in progress</li>
	 * 	<li> the node's parents are not enabled or in progress</li>
	 * </ul>
	 * @param problem the subproblem
	 * @return true iff the graph node is a leaf
	 */
	private synchronized boolean isLeaf(ProblemGraphNode problem){
		if (!problem.isEnabled() || problem.inProgress){
			return false;
		}
		
		for (ProblemGraphNode parent : problem.dependsOn){
			if (parent.isEnabled() || parent.inProgress){
				return false;
			}
		}
		
		return true;
	}

	/**
	 * @param problem  the problem
	 * @return <code>true</code> if the problem has been invalidated since the worker started it
	 */
	public synchronized boolean isInvalidated(ProblemGraphNode problem){
		return invalidated.contains(problem);
	}
	
	/**
	 * @return all problems in the graph that are either enabled or in progress
	 */
	public synchronized List<ProblemGraphNode> getEnabled(){
		List<ProblemGraphNode> result = Lists.newArrayList();
		for (ProblemGraphNode problem : problems){
			if (problem.isEnabled() || problem.isInProgress()){
				result.add(problem);
			}
		}
		return result;
	}
	
	/**
	 * @return <code>true</code> if any problem in the graph is enabled or in progress
	 */
	public synchronized boolean hasEnabled(){
		for (ProblemGraphNode problem : problems){
			if (problem.isEnabled() || problem.isInProgress()){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * @param clazz the problem class
	 * @return a list of problems of type <code>clazz</code>
	 */
	@SuppressWarnings("unchecked")
	public synchronized <T extends Problem> Set<T> all(Class<T> clazz){
		Set<T> result = Sets.newHashSet();
		
		for (ProblemGraphNode problem : problems){
			if (clazz.getCanonicalName().equals(problem.getProblem().getClass().getCanonicalName())){
				result.add((T) problem.getProblem());
			}
		}
		return result;
	}
	
	/**
	 * @param clazz the method problem type
	 * @param signature the method signature
	 * @return the problem of type <code>clazz</code> for method <code>signature</code>, or <code>null</code> iff no problem exists
	 */
	public synchronized ProblemGraphNode find(@SuppressWarnings("rawtypes") Class clazz, String signature){
		for (ProblemGraphNode problem : problems){
			if (clazz.getCanonicalName().equals(problem.getProblem().getClass().getCanonicalName()) 
				&& signature.equals(((MethodProblem) problem.getProblem()).getFunction().getSignature())){
				
				return problem;
			}
		}
		return null;
	}
	
	/**
	 * @return the set of leaves in the graph
	 * @see {@link #isLeaf(ProblemGraphNode)}
	 */
	public synchronized Set<ProblemGraphNode> getLeaves(){
		return Sets.newHashSet(Iterables.filter(problems, new Predicate<ProblemGraphNode>(){
			@Override
			public boolean apply(ProblemGraphNode n) {
				return isLeaf(n);
			}
		}));
	}
	
	/**
	 * Add <code>problems</code> to the graph, with <code>flags</code>
	 * @param problems the problems
	 * @param flags the problem flags
	 */
	public void addAll(Collection<Problem> problems, int flags){
		for (Problem problem : problems){
			this.problems.add(new ProblemGraphNode(problem, flags));
		}
	}
	
	/**
	 * Mark all problems matching <code>predicate</code> as dependencies for <code>node</code>
	 * @param methodProblem the problem
	 * @param predicate predicate to determine dependencies
	 */
	public void addDependencies(ProblemGraphNode methodProblem, Predicate<ProblemGraphNode> predicate){
		String method = ((MethodProblem) methodProblem.getProblem()).getFunction().getSignature();
		
		for (ProblemGraphNode problem : problems){
			String otherMethod = ((MethodProblem) problem.getProblem()).getFunction().getSignature();
			if (predicate.apply(problem) && !method.equals(otherMethod)){
				methodProblem.addDependency(problem);
			}	
		}
	}
	
	/**
	 * Mark any in-progress problems that depend on <code>problem</code>
	 * as invalid
	 * @param problem the problem
	 */
	public synchronized void invalidate(ProblemGraphNode problem){
		for (ProblemGraphNode dependent : problem.dependencyFor){
			if (inProgress.containsKey(problem)){
				log.debug("Invalidated " + dependent + " (being worked on by " + inProgress.get(dependent) + ")");
				invalidated.add(problem);
			}
			invalidate(dependent);
		}
	}
	
	/**
	 * update enabled state for all problems
	 * @see {@link ProblemGraphNode#updateEnabled()}
	 */
	public synchronized void updateEnabled(){
		for (ProblemGraphNode problem : problems){
			problem.updateEnabled();
		}	
	}
	
	/**
	 * Enable directly dependent problems satisfying <code>predicate</code> indicating
	 * <code>reason</code>
	 * @param predicate the predicate
	 * @param reason the reason to record
	 */
	public synchronized void enableDependentProblems(ProblemGraphNode problem, Predicate<ProblemGraphNode> predicate, String reason){
		for (ProblemGraphNode dependent : problem.dependencyFor){
			if (predicate.apply(dependent)){
				dependent.setEnabled(true, reason);
			}
		}
	}
	
	public synchronized void enable(ProblemGraphNode problem, String reason){
		problem.setEnabled(true, reason);
	}
	
	public synchronized void enable(@SuppressWarnings("rawtypes") Class clazz, HasQualifiedSignature signature, String reason){
		enable(clazz, signature.toString(), reason);
	}
	
	public synchronized void enable(@SuppressWarnings("rawtypes") Class clazz, String signature, String reason){
		ProblemGraphNode problem = find(clazz, signature);		
		if (problem == null){
			throw new RuntimeException("Cannot find problem of type " + clazz.toString() + " for method " + signature);
		}
		problem.setEnabled(true, reason);
	}
	
	public synchronized void cancel(User user, ProblemGraphNode problem){	
		inProgress.remove(problem);
		invalidated.remove(problem);
		problem.setInProgress(false);
	}
	
	public synchronized void start(User user, ProblemGraphNode problem){
		if (inProgress.containsKey(problem)){
			throw new IllegalArgumentException("Problem " + problem + " is being worked on by " + user);
		}
		inProgress.put(problem, user);
		problem.setInProgress(true);
	}
	
	public synchronized void finish(User user, ProblemGraphNode problem, String message){
		inProgress.remove(problem);
		invalidated.remove(problem);
		
		problem.setEnabled(false, message);
		problem.setInProgress(false);
		problem.clearSpecialFlags();
	}
	
	public static class ProblemGraphNode implements Serializable{
		private static final long serialVersionUID = 2L;
		
		public static final int NONE = 0;
		public static final int ENABLED = 1;
		public static final int IGNORE_SUFFICIENT = 2;
		public static final int IN_PROGRESS = 4;

		private Problem problem;
		
		private boolean enabled;
		private boolean ignoreSufficient;
		private boolean inProgress;
		
		/**
		 * reasons the problem is either 
		 */
		private Stack<String> activeReasons = new Stack<String>();
		
		/**
		 * direct dependencies
		 */
		private Set<ProblemGraphNode> dependsOn = Sets.newHashSet();
		
		/**
		 * problems that depend directly on this problem
		 */
		private Set<ProblemGraphNode> dependencyFor = Sets.newHashSet();
		
		@SuppressWarnings("unused")
		private ProblemGraphNode(){
		}
		
		public ProblemGraphNode(Problem problem, int flags) {
			this.problem = problem;
		
			ignoreSufficient = ((flags & IGNORE_SUFFICIENT) != 0);
			
			setEnabled(((flags & ENABLED) != 0),"Initial problem creation");
			
			inProgress = ((flags & IN_PROGRESS) != 0);
		}
		
		public Problem getProblem() {
			return problem;
		}
		
		/**
		 * @return the set of problems this problem directly depends on
		 */
		public Set<ProblemGraphNode> getDependsOn() {
			return Collections.unmodifiableSet(dependsOn);
		}
		
		/**
		 * @return the set of problems this problem is a direct dependency for
		 */
		public Set<ProblemGraphNode> getDependencyFor() {
			return Collections.unmodifiableSet(dependencyFor);
		}
	
		/**
		 * @return <code>true</code> if a worker currently working on the problem
		 */
		public boolean isInProgress() {
			return inProgress;
		}
		
		/**
		 * @return <code>true</code> iff sufficiency should be ignored for precondition problems
		 */
		public boolean ignoreSufficient(){
			return ignoreSufficient;
		}
		
		public void setIgnoreSufficient(boolean ignore){
			ignoreSufficient = ignore; 
		}
		
		private void clearSpecialFlags(){
			ignoreSufficient = false;
		}
		
		public boolean isEnabled() {
			return enabled;
		}
		
		/**
		 * Set whether the problem is enabled or disabled, logging <code>reason</code>
		 * @param enabled
		 * @param reason 
		 */
		private void setEnabled(boolean enabled, String reason) {
			this.activeReasons.push(reason);
			this.enabled = enabled;
		}
		
		private void setInProgress(boolean inProgress) {
			this.inProgress = inProgress;
		}
		
		/**
		 * If <code>dependency != null</code>, marks problem as being dependent on <code>dependency</code>
		 * @param dependency the problem dependency
		 * @deprecated graph structure should be modified via {@link ProblemGraph}
		 */
		public void addDependency(ProblemGraphNode dependency){
			if (dependency != null){
				this.dependsOn.add(dependency);
				dependency.dependencyFor.add(this);
			}
		}
		
		/**
		 * Marks problem as being dependent on all problems in <code>dependencies</code>
		 * @param dependencies 
		 * @see {{@link #addDependency(ProblemGraphNode)}
		 */
		public void addDependencies(ProblemGraphNode [] dependencies){
			for (ProblemGraphNode problem : dependencies){
				addDependency(problem);
			}
		}
		
		public final static Predicate<ProblemGraphNode> IS_ANY = Predicates.alwaysTrue();
		
		public final static Predicate<ProblemGraphNode> IS_SELREQ = new Predicate<ProblemGraphNode>(){
				@Override
				public boolean apply(ProblemGraphNode problem) {
					return problem.getProblem() instanceof SelectRequiresProblem;
				}
		};
		
		public final static Predicate<ProblemGraphNode> IS_WRITEREQ = new Predicate<ProblemGraphNode>(){
			@Override
			public boolean apply(ProblemGraphNode problem) {
				return problem.getProblem() instanceof WriteRequiresProblem;
			}
		};
		
		public final static Predicate<ProblemGraphNode> IS_POST = new Predicate<ProblemGraphNode>(){
			@Override
			public boolean apply(ProblemGraphNode problem) {
				return problem.getProblem() instanceof WriteRequiresProblem ||
					problem.getProblem() instanceof WriteExsuresProblem;
			}
		};
			
		@Override
		public String toString() {
			return "ProblemNode [id=" + Integer.toHexString(hashCode()) + " " + problem + ", enabled="
					+ isEnabled() + "]";
		}
	
		/**
		 * Updates the status of <i>precondition</i> problems according to the following:
		 * <ul>
		 * 		<li>SelectRequires:
		 * 			<ul>
		 * 				<li>Disable if there are no known choices</li>
		 * 				<li>Disable if the selected conditions are sufficient, unless "ignore sufficient" flag is set</li>
		 * 				<li>Enable the problem, unless the corresponding WriteRequires problem is enabled
		 * 			</ul>
		 *		</li>
		 *		<li>WriteRequires:
		 * 			<ul>
		 * 				<li>Disable if the selected conditions are sufficient, unless "ignore sufficient" flag is set</li>
		 * 				<li>Otherwise, enable the problem</li>
		 * 			</ul>
		 *		</li>
		 * 	</ul>
		 */
		private void updateEnabled(){
			if (problem instanceof SelectRequiresProblem){
				final SelectRequiresProblem selectRequires = (SelectRequiresProblem) problem;
				
				if (selectRequires.getChoices().isEmpty()){
					enabled = false;
				}else if (selectRequires.isSufficient()){
					enabled = ignoreSufficient;
				}else{
					//!sufficient
					
					ProblemGraphNode dependency = Iterables.find(this.dependencyFor, new Predicate<ProblemGraphNode>(){
						@Override
						public boolean apply(ProblemGraphNode problem) {
							return problem.getProblem() instanceof WriteRequiresProblem 
								&& ((MethodProblem) problem.getProblem()).qualifiedSignature().equals(selectRequires.getFunction().qualifiedSignature());
						}
					}, null);
						
					//Only show the problem if the corresponding write requires problem is not enabled
					if (dependency == null){
						enabled = true;
					}else{
						dependency.updateEnabled();
						enabled = !dependency.ignoreSufficient();
					}
				}
			}else if (problem instanceof WriteRequiresProblem){
				WriteRequiresProblem writeRequires = (WriteRequiresProblem) problem;	
				enabled = writeRequires.isSufficient() ? ignoreSufficient : true;
			}
		}
	}
}
