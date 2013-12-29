package com.schiller.veriasa.web.server;

import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Iterables.any;
import static com.google.common.collect.Lists.newArrayList;
import static com.schiller.veriasa.web.server.SpecUtil.ACCEPT_GOOD;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.schiller.veriasa.web.server.callgraph.ProblemGraph.ProblemGraphNode;
import com.schiller.veriasa.web.shared.config.EscJResponseException;
import com.schiller.veriasa.web.shared.core.HasQualifiedSignature;
import com.schiller.veriasa.web.shared.core.MethodContract;
import com.schiller.veriasa.web.shared.core.MethodSpecBuilder;
import com.schiller.veriasa.web.shared.core.ProjectSpecification;
import com.schiller.veriasa.web.shared.core.Clause;
import com.schiller.veriasa.web.shared.core.Clause.Status;
import com.schiller.veriasa.web.shared.core.TypeSpecification;
import com.schiller.veriasa.web.shared.core.TypeSpecBuilder;
import com.schiller.veriasa.web.shared.escj.ProjectResult;
import com.schiller.veriasa.web.shared.problems.MethodProblem;
import com.schiller.veriasa.web.shared.problems.Problem;
import com.schiller.veriasa.web.shared.problems.WriteEnsuresProblem;
import com.schiller.veriasa.web.shared.problems.WriteExsuresProblem;

/**
 * Methods for handling object invariants
 * @author Todd Schiller
 */
public class ObjectInvariants {

	/**
	 * Debugging logger
	 */
	protected static Logger log = Logger.getLogger(ObjectInvariants.class);
	
	/**
	 * Threshold at which VeriWeb suggests lifting common method invariants to be object
	 * invariants
	 */
	public static final double OBJECT_INVARIANT_THRESHOLD = .5;
	
	/**
	 * Returns the specifications that are both preconditions and postconditions for the method, but
	 * do not refer to a method parameter. For constructors, returns the postconditions for the method
	 * that do not refer to a postcondition.
	 * @param method the method specification
	 * @return the specifications that are both preconditions and postconditions for the method
	 */
	public static List<Clause> methodInvariants(MethodContract method){
		Collection<Clause> result = Lists.newArrayList();
		
		if (Util.isCtor(method)){
			// TODO add support for constructors with checked exceptions exception
			result = filter(method.getEnsures(), ACCEPT_GOOD);
		}else{
			Collection<Clause> preconditions = filter(method.getRequires(), ACCEPT_GOOD);
		
			// Calculate the intersection of the postconditions	
			Collection<Clause> postconditions = filter(method.getEnsures(), ACCEPT_GOOD);
			for (List<Clause> forException :  method.getExsures().values()){
				Collection<Clause> exsures = filter(forException, ACCEPT_GOOD);
				postconditions = SpecUtil.intersect(postconditions, exsures);
			}
			result = SpecUtil.intersect(preconditions, postconditions);
		}	
		
		// Filter statements that refer to parameters
		return Lists.newArrayList(filter(result, new NoParamPredicate(method, false)));
	}
	
	/**
	 * Get all of the method invariants for methods in <code>type</code>. 
	 * See {@link ObjectInvariants#methodInvariants(MethodContract)}. All statements
	 * are marked as pending, and have no associated reason.
	 * @param type the type specification
	 * @return  all of the method invariants for methods in <code>type</code>
	 */
	public static List<Clause> methodInvariants(TypeSpecification type){
		List<Clause> result = Lists.newArrayList();
		
		for (MethodContract method : type.getMethods()){
			if (method.isPublic() || Util.isCtor(method)){
				SpecUtil.extend(result, methodInvariants(method));
			}
		}
		
		log.debug(type.getFullyQualifiedName() + " has " + result.size() + " method invariants");
		return Lists.transform(result, new SpecUtil.ResetStatus(Status.PENDING));
	}
	
	/**
	 * true iff <code>query</code> meets or exceeds the requisite threshold ({@link ObjectInvariants#OBJECT_INVARIANT_THRESHOLD})
	 * to considered as an object invariant for <code>type</code>. Assumes the query is not yet part of the specification.
	 * @param type the type
	 * @param query the invariant to check
	 * @return true iff <code>query</code> meets or exceeds the requisite threshold to considered as an object invariant for <code>type</code>
	 */
	public static boolean exceedsThreshold(TypeSpecification type, Clause query){
		int numMethods = 0;
		int count = 1; // the query is assumed to not be part of the specification yet.
		
		for (MethodContract method : type.getMethods()){
			if ((method.isPublic() || Util.isCtor(method)) && !Util.isPure(method)){
				if (any(methodInvariants(method), SpecUtil.invEq(query))){
					count++;
				}
				numMethods++;
			}
		}
		
		double impact = ((double) count) / numMethods;
		log.debug("Query " + query + " has impact " + impact + " (" + count + "/" + numMethods + ")");
		return impact >= ObjectInvariants.OBJECT_INVARIANT_THRESHOLD && impact < 1.0;
	}
	
	/**
	 * <code>true</code> iff <code>query</code> is an object invariant for <code>type</code>, or appears
	 * as a precondition for every method 
	 * @param type the type
	 * @param query the invariant to check
	 * @return <code>true</code> iff <code>query</code> is an object invariant for <code>type</code>, or appears
	 * as a precondition for every method 
	 */
	public static boolean alreadyObjectInvariant(TypeSpecification type, Clause query){
		Predicate<Clause> match = SpecUtil.invEq(query);
		
		if (Iterables.any(filter(type.getInvariants(), SpecUtil.ACCEPT_GOOD), match)){
			return true;
		}
		
		for (MethodContract method : type.getMethods()){
			if (!Util.isCtor(method) && !Iterables.any(filter(method.getRequires(), SpecUtil.ACCEPT_GOOD), match)){
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Returns <code>true</code> iff adding <code>objectInvariant</code> as an object invariant for <code>type</code>
	 * will not introduce any <i>new</i> call violations
	 * @param original the original project specification (without the object invariant)
	 * @param originalResult the ESC/Java2 result for <code>original</code>
	 * @param type the type to add <code>objectInvariant</code> to
	 * @param objectInvariant the object invariant to add
	 * @return <code>true</code> iff it is safe to add <code>objectInvariant</code> as an invariant to <code>type</code>
	 * @throws EscJResponseException iff an error occurred when getting the result for the modified specification
	 */
	public static boolean isInvariantSafe(ProjectSpecification original, ProjectResult originalResult, TypeSpecification type, Clause objectInvariant) throws EscJResponseException{

		return false;
		
//		TypeSpec modifiedType = pushObjectInvariants(type, Lists.newArrayList(objectInvariant), true);
//		ProjectSpec modifiedProject = Util.modifySpec(original, modifiedType);
//		
//		ProjectResult modifiedResult = VeriServiceImpl.getResult(modifiedProject);
//		
//		log.debug("Comparing " + originalResult.getId() + " against " + modifiedResult.getId());
//		
//		if (modifiedResult.getSpecErrors().size() > originalResult.getSpecErrors().size()){
//	
//			// TODO fix the determination for whether or not an invariant is safe
//			return false;
//		}
//		
//		return true;
	}
	
	/**
	 * Create new type specification by pushing invariants to public methods, and removing all invariants
	 * @param type the type specification
	 * @return the new (modified) specification
	 */
	public static TypeSpecification pushObjectInvariants(TypeSpecification type){
		return pushObjectInvariants(type, type.getInvariants(), false);
	}
	
	/**
	 * Create new type specification by pushing <code>objectInvariants</code> to public methods, removing
	 * the object invariants
	 * @param type the original type specifications
	 * @param objectInvariants the object invariants to push to the methods
	 * @param onlyPreconditions <code>true</code> true iff the object invariants should only be added as preconditions
	 * @return the new (modified) specification
	 */
	public static TypeSpecification pushObjectInvariants(TypeSpecification type, final Collection<Clause> objectInvariants, boolean onlyPreconditions){
		if (objectInvariants.isEmpty()){
			return type;
		}
		
		List<MethodContract> methods = Lists.newArrayList();
		
		for (MethodContract method : type.getMethods()){
			MethodSpecBuilder modified = MethodSpecBuilder.builder(method);
			
			List<Clause> requires = Util.isCtor(method)
					? method.getRequires() // don't push object invariants to constructors
					: SpecUtil.extend(method.getRequires(), objectInvariants);
			modified.setRequires(requires);
			
			if (!onlyPreconditions){
				List<Clause> ensures = SpecUtil.extend(method.getEnsures(), objectInvariants);
				
				Map<String,List<Clause>> allExsures = Maps.newHashMap();
				
				for (String exception : method.getExsures().keySet()){
					List<Clause> exsures = SpecUtil.extend(method.getExsures().get(exception), objectInvariants);
					allExsures.put(exception, exsures);
				}
				
				modified.setEnsures(ensures).setExsures(allExsures);
			}
		
			methods.add(modified.getSpec());
		}
		
		// the object invariants in the type specification that are not in the set of invariants to push
		List<Clause> remainingObjectInvariants = newArrayList(filter(type.getInvariants(), new Predicate<Clause>(){
			@Override
			public boolean apply(Clause condition) {
				return !any(objectInvariants, SpecUtil.invEq(condition));
			}
		}));
		
		return TypeSpecBuilder
				.builder(type)
				.setInvariants(remainingObjectInvariants)
				.setMethods(methods)
				.getType();
	}
	
	/**
	 * Returns the set of potential object invariants for <code>type</code>: the union of the
	 * object invariants, and the postconditions of public methods that
	 * do not refer to a method parameter.
	 * @param type the type specification
	 * @return the set of potential object invariants for <code>type</code>
	 */
	public static List<Clause> getPotentialObjectInvariants(TypeSpecification type){
		List<Clause> potential = Lists.newLinkedList();
		
		potential.addAll(type.getInvariants());
		
		for (MethodContract method : type.getMethods()){
			NoParamPredicate noParameters = new NoParamPredicate(method, false);
			
			if (method.isPublic()){
				potential = SpecUtil.extend(potential, filter(method.getEnsures(), noParameters));
				
				for (List<Clause> exsures :  method.getExsures().values()){
					potential = SpecUtil.extend(potential, filter(exsures, noParameters));
				}
			}
		}
		
		return potential;
	}
	
	/**
	 * Calculate the type invariants. The type invariants proposed by a method are 
	 * the valid <i>postconditions</i> for the method that do not refer to method parameters.
	 * The object invariants are the intersection of these postconditions
	 *
	 * @param project the project specification
	 * @param visited set of problems that have already been visited
	 * @return the project specification with discovered object invariants
	 */
	public static ProjectSpecification calculateObjectInvariants(ProjectSpecification project, Set<ProblemGraphNode> visited){
		List<TypeSpecification> result = Lists.newLinkedList();
		
		for (TypeSpecification type : project.getTypeSpecs()){
			List<Clause> objectInvariants = Lists.newArrayList();
			
			// The first method is intersected with the "infinite" set of invariants. Since it's not possible to do this,
			// we need to special case the first method
			boolean first = true;
			
			// Constructors should not generate invariants by themselves. However, they should restrict the set
			// of invariants inferred
			boolean onlyCtor = true;
			
			for (final MethodContract method : type.getMethods()){
				if ((method.isPublic() || Util.isCtor(method)) && !Util.isPure(method)){
					ProblemGraphNode ensuresProblem = Iterables.find(visited, nodeForMethod(WriteEnsuresProblem.class, method), null);
					ProblemGraphNode exsuresProblem = Iterables.find(visited, nodeForMethod(WriteExsuresProblem.class, method), null);
					
					if (ensuresProblem != null && (method.getExsures().isEmpty() || exsuresProblem != null)){
						objectInvariants = first 
								? Lists.newArrayList(methodInvariants(method))
								: Lists.newArrayList(SpecUtil.intersect(objectInvariants, methodInvariants(method)));
						
						//Update the special case variables
						first = false;
						onlyCtor &= Util.isCtor(method);
					}
				}
			}
			
			if (onlyCtor || first){
				log.debug("Skipping object invariant inference for " + type.getFullyQualifiedName());
				result.add(type);
			}else{
				log.debug("Found " + objectInvariants.size() + " object invariants for " + type.getFullyQualifiedName());
				
				result.add(TypeSpecBuilder.builder(type)
						.setInvariants(objectInvariants)
						.getType());
			}
		}
		
		return new ProjectSpecification(project.getName(), result);
	}
	
	/**
	 * Creates a predicate that accepts problem graph node corresponding to the given method and class
	 * @param method the method (given by qualified signature)
	 * @return a predicate that accepts problem graph node corresponding to the given method
	 */
	private static Predicate<ProblemGraphNode> nodeForMethod(final Class<? extends Problem> clazz, final HasQualifiedSignature method){
		return new Predicate<ProblemGraphNode>(){
			@Override
			public boolean apply(ProblemGraphNode node) {
				return  node.getProblem().getClass() == clazz
						&& ((MethodProblem) node.getProblem()).getFunction().getSignature().equals(method.qualifiedSignature());
			}
		};
	}
}
