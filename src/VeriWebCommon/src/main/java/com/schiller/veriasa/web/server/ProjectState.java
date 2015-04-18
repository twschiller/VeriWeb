package com.schiller.veriasa.web.server;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.schiller.veriasa.callgraph.CallGraphNode;
import com.schiller.veriasa.web.server.callgraph.ProblemGraph;
import com.schiller.veriasa.web.server.callgraph.ProblemGraph.ProblemGraphNode;
import com.schiller.veriasa.web.server.escj.EscJUtil;
import com.schiller.veriasa.web.shared.core.HasQualifiedSignature;
import com.schiller.veriasa.web.shared.core.MethodContract;
import com.schiller.veriasa.web.shared.core.MethodSpecBuilder;
import com.schiller.veriasa.web.shared.core.ProjectSpecification;
import com.schiller.veriasa.web.shared.core.Clause;
import com.schiller.veriasa.web.shared.core.TypeSpecification;
import com.schiller.veriasa.web.shared.core.TypeSpecBuilder;
import com.schiller.veriasa.web.shared.escj.MethodResult;
import com.schiller.veriasa.web.shared.escj.ProjectResult;
import com.schiller.veriasa.web.shared.escj.TypeResult;
import com.schiller.veriasa.web.shared.messaging.UserMessageThread;
import com.schiller.veriasa.web.shared.problems.MethodProblem;
import com.schiller.veriasa.web.shared.problems.Problem;
import com.schiller.veriasa.web.shared.problems.SelectRequiresProblem;
import com.schiller.veriasa.web.shared.problems.WriteEnsuresProblem;
import com.schiller.veriasa.web.shared.problems.WriteExsuresProblem;
import com.schiller.veriasa.web.shared.problems.WriteRequiresProblem;

/**
 * The state of a project being verified
 * @author Todd Schiller
 */
public class ProjectState implements Serializable{
	private static final long serialVersionUID = 4L;

	private static long nextId = System.currentTimeMillis();
	
	private long id;
	
	private ProjectDescriptor descriptor;
	
	/**
	 * the owner of the project, or <code>null</code> iff the project is shared
	 */
	private transient User owner;
	
	private transient Logger log;
	
	private ProjectSpecification active;
	
	private ProblemGraph problems = new ProblemGraph();
	
	private Set<ProblemGraphNode> visited = Sets.newHashSet();
	
	private HashMap<String, List<UserMessageThread>> messageThreads = Maps.newHashMap();

	private boolean solved = false;
	
	/**
	 * Initialize a new shared project (a project with no owner)
	 * @param descriptor the project
	 */
	public ProjectState(ProjectDescriptor descriptor) {
		this(descriptor, null);
	}
	
	/**
	 * Initialize a new project for <code>descriptor</code> owned by <code>owner</code>
	 * @param descriptor the project
	 * @param owner the project's owner
	 */
	public ProjectState(ProjectDescriptor descriptor, User owner) {
		super();
		this.descriptor = descriptor;
		this.owner = owner;
		this.id = nextId++;
		restoreLogging();
		init();
	}
	
	/**
	 * Set the owner of the project to <code>owner</code>
	 * @param owner the new owner
	 * @throws IllegalStateException iff the project is already owned
	 */
	public void setOwner(User owner){
		if (isOwned()){
			throw new IllegalStateException("Project is already owned");
		}
		this.owner = owner;
	}
	
	/**
	 * @param user the query
	 * @return <code>true</code> iff the project is owned by <code>user</code>
	 */
	public boolean isOwnedBy(User user){
		return owner == user;
	}
	
	/**
	 * @return <code>true</code> iff the project has an owner (i.e., it is not shared)
	 */
	public boolean isOwned(){
		return owner != null;
	}
	
	/**
	 * Initialize the logger for the project
	 */
	public void restoreLogging(){
		this.log = Logger.getLogger("ProjectLogger." + this.id);
	}
	
	/**
	 * @return the project descriptor
	 */
	public ProjectDescriptor getDescriptor(){
		return descriptor;
	}

	/**
	 * @return the project name
	 */
	public String getName(){
		return descriptor.getProjectName();
	}
	
	/**
	 * @return a reference to the project's problem graph
	 */
	public ProblemGraph getProblems(){
		return problems;
	}
	
	public void log(Level level, String msg){
		log.log(level, "PROJECT " + id + " " + msg);
	}
	
	public void addMessage(String signature, UserMessageThread thread){
		messageThreads.get(signature).add(thread);
	}
	
	public List<UserMessageThread> getMessages(String signature){
		return messageThreads.get(signature);
	}
	
	public List<UserMessageThread> getMessages(HasQualifiedSignature signature){
		return getMessages(signature.qualifiedSignature());
	}
	
	public ProblemGraphNode find(@SuppressWarnings("rawtypes") Class clazz, HasQualifiedSignature signature){
		return find(clazz, signature.qualifiedSignature());
	}
	
	public ProblemGraphNode find(@SuppressWarnings("rawtypes") Class clazz, String signature){
		return problems.find(clazz,signature);
	}
		
	public void markVisited(ProblemGraphNode problem){
		visited.add(problem);
	}
	
	public ProjectSpecification getActiveSpec(){
		return active;
	}

	/**
	 * Update the specification for <code>method</code>
	 * @param method the new method specification
	 */
	public void update(MethodContract method){
		active = Util.modifySpec(active, method);
	}
	
	/**
	 * Mark that the project is solved (i.e., has no more warnings).
	 */
	public void markSolved(){
		solved = true;
	}
	
	/**
	 * @return <code>true</code> iff the project has no more warnings
	 */
	public boolean isSolved(){
		return solved;
	}
	
	public void calculateObjectInvariants(){
		active = ObjectInvariants.calculateObjectInvariants(active, visited);
	}
	
	private void init(){
		for (MethodContract method : Util.allMethods(descriptor.getBaseSpec())){
			messageThreads.put(method.getSignature(), new LinkedList<UserMessageThread>());
		}
		
		ProjectResult baseResult = descriptor.getBaseResult();
		ProjectSpecification inferred = descriptor.getInferredSpec();
		
		problems.addAll(generateSelectRequiresProblems(descriptor, baseResult,inferred), ProblemGraphNode.NONE);
		problems.addAll(generateWriteRequiresProblems(descriptor, baseResult,inferred), ProblemGraphNode.NONE);
		problems.addAll(generateWriteEnsuresProblems(descriptor, inferred), ProblemGraphNode.NONE);
		problems.addAll(generateWriteExsuresProblems(descriptor, inferred), ProblemGraphNode.NONE);
	
		for (final String method : descriptor.getOrder()){
			CallGraphNode n = descriptor.getCallGraph().getNode(method);
			
			ProblemGraphNode srp = problems.find(SelectRequiresProblem.class, method);
			ProblemGraphNode wrp = problems.find(WriteRequiresProblem.class, method);
			ProblemGraphNode wenp = problems.find(WriteEnsuresProblem.class, method);
			ProblemGraphNode wexp = problems.find(WriteExsuresProblem.class, method);
			
			if (srp == null && wrp == null && wenp == null && wexp == null){
				continue;
			}
			
			//Add intra method dependencies
			wrp.addDependency(srp);
			wenp.addDependency(srp); wenp.addDependency(wrp);
			if (wexp != null){
				wexp.addDependency(srp); wexp.addDependency(wrp);
			}
			
			//Add intermethod dependencies
			for (CallGraphNode callee : n.getCallees()){
				String calleeSig = callee.getQualifiedMethodName();
				
				ProblemGraphNode o_srp = problems.find(SelectRequiresProblem.class, calleeSig);
				ProblemGraphNode o_wrp = problems.find(WriteRequiresProblem.class, calleeSig);
				ProblemGraphNode o_wenp = problems.find(WriteEnsuresProblem.class, calleeSig);
				ProblemGraphNode o_wexp = problems.find(WriteExsuresProblem.class, calleeSig);
				
				//things the sel req problem relies on
				srp.addDependencies(new ProblemGraphNode [] {o_srp, o_wrp, o_wenp, o_wexp});
				
				//things the write req problem relies on
				wrp.addDependencies(new ProblemGraphNode [] {o_srp, o_wrp, o_wenp, o_wexp});
				
				//things the write ensures problem relies on
				
				wenp.addDependencies(new ProblemGraphNode [] {o_wenp, o_wexp});
				
				if (o_wenp != null && !((MethodProblem) o_wenp.getProblem()).qualifiedSignature().contains("Check")){
					problems.enable(o_wenp, "Problem Initialization");
				}
				if (o_wexp != null && !((MethodProblem) o_wexp.getProblem()).qualifiedSignature().contains("Check")){
					problems.enable(o_wexp, "Problem Initialization");
				}
				
				//things the write ensures problem relies on
				if (wexp != null){
					wexp.addDependencies(new ProblemGraphNode [] {o_wenp, o_wexp});
				}	
			}
		}
	
		active = descriptor.getBaseSpec();
		problems.updateEnabled();
	}
	
	/**
	 * "Add" an object invariant by updating the method preconditions in <code>type</code> and
	 * reactivating problems as necessary due to weakened preconditions. Does the following:
	 * <ul>
	 * 	<li>Activates postcondition problems for constructor(s), if the invariant is not a postcondition</li>
	 *  <li>Add <code>invariant</code> as a precondition to every method in the type (excluding constructors)</li>
	 *  <li>Activate problems as necessary due to weakened preconditions</li>
	 * </ul>
	 * @param type the type specification
	 * @param invariant the invariant to "add"
	 */
	public void addObjectInvariant(TypeSpecification type, Clause invariant, List<MethodContract> skip){
		TypeSpecification oldType = active.forType(type.getFullyQualifiedName());
		
		List<MethodContract> newMethods = Lists.newArrayList();
		
		for (MethodContract oldMethod : oldType.getMethods()){	
			if (Iterables.any(skip, new Util.SameSignature(oldMethod))){
				
				newMethods.add(oldMethod);
	
			}else if (Util.isCtor(oldMethod)){
				// for constructors, activate the ensures problem if the invariant is not already in the 
				// set of postconditions.
				
				// we shouldn't need to add the invariant to the set of known conditions because
				// it already must be a method invariant
				
				// TODO propagate invariant to constructors that throw checked exceptions
				
				Collection<Clause> oldEnsures = Collections2.filter(oldMethod.getEnsures(), SpecUtil.ACCEPT_GOOD);
				if (!SpecUtil.contains(oldEnsures, invariant)){
					problems.enable(WriteEnsuresProblem.class, oldMethod, "Propogating object invariant");
				}
				
				newMethods.add(oldMethod);
			}else{
				Collection<Clause> oldRequires = Collections2.filter(oldMethod.getRequires(), SpecUtil.ACCEPT_GOOD);
				
				if (SpecUtil.contains(oldRequires, invariant)){
					// do nothing, because the invariant is already there
					newMethods.add(oldMethod);
				}else{
					// create set of statements where the invariant is known to be good
					List<Clause> modified = Lists.newArrayList(oldMethod.getRequires());
					SpecUtil.add(modified, invariant);
					MethodContract newMethod = MethodSpecBuilder.builder(oldMethod).setRequires(modified).getSpec();
			
					problems.enable(WriteEnsuresProblem.class, oldMethod, "Propogating object invariant");
					if (find(WriteExsuresProblem.class, oldMethod) != null){
						problems.enable(WriteExsuresProblem.class, oldMethod, "Propogating object invariant");
					}
					
					newMethods.add(newMethod);
				}
			}
		}
		
		active = Util.modifySpec(active, TypeSpecBuilder.builder(oldType).setMethods(newMethods).getType());
	}
	
	
	private static Collection<Problem> generateSelectRequiresProblems(
			ProjectDescriptor descriptor,
			ProjectResult result, 
			ProjectSpecification project){
		
		Collection<Problem> problems = Lists.newArrayList();
		
		for (TypeSpecification type : project.getTypeSpecs()){
			TypeResult typeResult = result.getTypeResult(type.getFullyQualifiedName());
			
			for (MethodContract method : type.getMethods()){				
				MethodResult methodResult = Util.lookupMethodResult(typeResult, method);	

				problems.add(new SelectRequiresProblem(
						method, 
						Util.annotateBody(descriptor, project, method, result, typeResult), 
						method.getRequires(), 
						new HashSet<Clause>(),
						EscJUtil.isSufficient(methodResult.getWarnings())));
			}
		}
		return problems;
	}
	
	private static Collection<Problem> generateWriteRequiresProblems(
			ProjectDescriptor descriptor,
			ProjectResult result, 
			ProjectSpecification project){
		
		Collection<Problem> problems = Lists.newArrayList();
		
		for (TypeSpecification type : project.getTypeSpecs()){
			TypeResult typeResult = result.getTypeResult(type.getFullyQualifiedName());
			
			for (MethodContract method : type.getMethods()){
				MethodResult methodResult = Util.lookupMethodResult(typeResult, method);	
				
				problems.add(new WriteRequiresProblem(
						method, 
						Util.annotateBody(descriptor, project, method, result, typeResult), 
						new LinkedList<Clause>(),//assume we don't know any requires clauses yet 
						EscJUtil.isSufficient(methodResult.getWarnings())));
			}
		}
		
		return problems;
	}
	
	private static Collection<Problem> generateWriteExsuresProblems(
			ProjectDescriptor descriptor,
			ProjectSpecification project){
		
		Collection<Problem> problems = Lists.newArrayList();
		
		for (TypeSpecification type : project.getTypeSpecs()){
			for (MethodContract method : type.getMethods()){	
				String documentedBody = Util.addDocumentationTags(descriptor.getDefMap(), method, Util.allMethods(project));
				
				for (String exception : method.getExsures().keySet()){
						problems.add(new WriteExsuresProblem(
								method, 
								documentedBody,
								exception,
								new LinkedList<Clause>(), 
								method.getEnsures()));
				}	
			}
		}
		return problems;
	}
	
	private static Collection<Problem> generateWriteEnsuresProblems(
			ProjectDescriptor descriptor,
			ProjectSpecification project){
		
		Collection<Problem> problems = Lists.newArrayList();
		
		for (TypeSpecification type : project.getTypeSpecs()){
			for (MethodContract method : type.getMethods()){	
				String documentedBody = Util.addDocumentationTags(descriptor.getDefMap(), method, Util.allMethods(project));
				
				problems.add(new WriteEnsuresProblem(
						method, 
						documentedBody, 
						new LinkedList<Clause>(), 
						method.getEnsures()));
			}
		}
		
		return problems;
	}
}
