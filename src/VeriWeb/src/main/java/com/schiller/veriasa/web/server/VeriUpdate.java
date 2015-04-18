package com.schiller.veriasa.web.server;

import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Iterables.find;
import static com.google.common.collect.Lists.transform;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.schiller.veriasa.web.server.escj.EscJUtil;
import com.schiller.veriasa.web.shared.core.MethodContract;
import com.schiller.veriasa.web.shared.core.ProjectSpecification;
import com.schiller.veriasa.web.shared.core.Clause;
import com.schiller.veriasa.web.shared.core.Clause.Status;
import com.schiller.veriasa.web.shared.core.TypeSpecification;
import com.schiller.veriasa.web.shared.escj.AnnotatedFile;
import com.schiller.veriasa.web.shared.escj.Chunk;
import com.schiller.veriasa.web.shared.escj.MethodResult;
import com.schiller.veriasa.web.shared.escj.ProjectResult;
import com.schiller.veriasa.web.shared.escj.TypeResult;
import com.schiller.veriasa.web.shared.problems.MethodProblem;
import com.schiller.veriasa.web.shared.problems.SelectRequiresProblem;
import com.schiller.veriasa.web.shared.problems.WriteEnsuresProblem;
import com.schiller.veriasa.web.shared.problems.WriteExsuresProblem;
import com.schiller.veriasa.web.shared.problems.WriteRequiresProblem;

/**
 * Methods for updating VeriWeb subproblems with ESC/Java2 results
 * @author Todd Schiller
 */
public abstract class VeriUpdate {

	/**
	 * The update performs the following actions:
	 * <ul>
	 *  <li>Mark statements with associated ESC/Java2 warnings as bad</li>
	 * 	<li>Add active object invariants and preconditions for the method as pending postconditions</li>
	 *  <li>Update method source markup</li>
	 * </ul>
	 * 
	 * @param descriptor the project descriptor
	 * @param project the project specification
	 * @param result the ESC/Java2 result for <code>project</code>
	 * @param problems the problems to update
	 */
	public static void updateWriteExsuresProblems(
			ProjectState state,
			ProjectResult result){
		
		ProjectSpecification project = state.getActiveSpec();
		ProjectDescriptor descriptor = state.getDescriptor();
		Set<WriteExsuresProblem> problems = state.getProblems().all(WriteExsuresProblem.class);
		
		for (TypeSpecification type : project.getTypeSpecs()){

			TypeResult typeResult = result.getTypeResult(type.getFullyQualifiedName());
			AnnotatedFile lineMap = result.getLineMaps().get(type.getLocation().getCompilationUnit());
			
			for (MethodContract method : filter(type.getMethods(), Util.HAS_EXSURES)){
				MethodResult methodResult = Util.lookupMethodResult(typeResult, method);	
				WriteExsuresProblem problem = find(problems, sameLocation(method));
			
				//TODO: this needs to be made more general to support functions with more than one thrown exceptions
				List<Clause> exsures = Lists.newLinkedList(method.getExsures().values()).getFirst();

				for (Chunk warning : filter(methodResult.getWarnings(), EscJUtil.ONLY_POST)){
					if (warning.getAssociatedDeclaration() != null){
						if (warning.getAssociatedDeclaration().getContents().contains("throws")){
							//do nothing for a Runtime Exception that is not declared, this should be handled
							//by the requires problem
						}else{
							Clause associated = lineMap.getSpec(warning.getAssociatedDeclaration().getLine() - 1);
							SpecUtil.resetStatus(associated, exsures, Status.KNOWN_BAD, warning.getMessage());
						}
					}else{
						Clause associated = lineMap.getSpec(warning.getLine() - 1);
						SpecUtil.resetStatus(associated, exsures, Status.KNOWN_BAD, warning.getMessage());
					}
				}

				// Mark all statements without an associated warning as good
				SpecUtil.changeStatus(exsures, Status.PENDING, Status.KNOWN_GOOD);
	
				// Create known set of specifications: marked exsures statements, method preconditions, previously known statements
				List<Clause> oldKnown = transform(problem.getKnown(), SpecUtil.RESET_PENDING);
				List<Clause> known = SpecUtil.extend(exsures, SpecUtil.extend(oldKnown, borrowRequires(method)));
				
				// Assumed preconditions for the problem are the method's preconditions and object invariants
				List<Clause> assumedPreconditions = SpecUtil.extend(
						filter(method.getRequires(), SpecUtil.ACCEPT_GOOD),
						filter(type.getInvariants(), SpecUtil.ACCEPT_GOOD));
				
				problem.setAnnotatedBody(annotateBody(descriptor, project, method));
				problem.setRequires(assumedPreconditions);
				problem.setKnown(known);	
			}
		}
	}
	
	/**
	 * The update performs the following actions:
	 * <ul>
	 *  <li>Mark statements with associated ESC/Java2 warnings as bad</li>
	 * 	<li>Add active object invariants and preconditions for the method as pending postconditions</li>
	 * 	<li>Update method source markup</li>
	 * </ul>
	 * 
	 * @param descriptor the project descriptor
	 * @param project the project specification
	 * @param result the ESC/Java2 result for <code>project</code>
	 * @param problems the problems to update
	 */
	public static void updateWriteEnsuresProblems(
			ProjectState state,
			ProjectResult result){
	
		ProjectSpecification project = state.getActiveSpec();
		ProjectDescriptor descriptor = state.getDescriptor();
		Set<WriteEnsuresProblem> problems = state.getProblems().all(WriteEnsuresProblem.class);
		
		for (TypeSpecification type : project.getTypeSpecs()){

			TypeResult typeResult = result.getTypeResult(type.getFullyQualifiedName());
			AnnotatedFile lineMap = result.getLineMaps().get(type.getLocation().getCompilationUnit());
			
			for (MethodContract method : type.getMethods()){
				MethodResult methodResult = Util.lookupMethodResult(typeResult, method);	
				WriteEnsuresProblem problem = find(problems, sameLocation(method));
				
				List<Clause> ensures = method.getEnsures();

				for (Chunk warning : filter(methodResult.getWarnings(), EscJUtil.ONLY_POST)){
					if (warning.getAssociatedDeclaration() != null){
						if (warning.getAssociatedDeclaration().getContents().contains("throws")){
							//do nothing for a Runtime Exception that is not declared, this should be handled
							//by the requires problem
						}else{
							Clause associated = lineMap.getSpec(warning.getAssociatedDeclaration().getLine() - 1);
							SpecUtil.resetStatus(associated, ensures, Status.KNOWN_BAD, warning.getMessage());
						}
					}else{
						Clause associated = lineMap.getSpec(warning.getLine() - 1);
						SpecUtil.resetStatus(associated, ensures, Status.KNOWN_BAD, warning.getMessage());
					}
				}	

				// Mark all statements without an associated warning as good
				SpecUtil.changeStatus(ensures, Status.PENDING, Status.KNOWN_GOOD);
				
				// Create known set of specifications: marked ensures statements, method preconditions, previously known statements
				List<Clause> oldKnown = transform(problem.getKnown(), SpecUtil.RESET_PENDING);
				List<Clause> known = SpecUtil.extend(ensures, SpecUtil.extend(oldKnown, borrowRequires(method)));
				
				// Assumed preconditions for the problem are the method's preconditions and object invariants
				List<Clause> assumedPreconditions = SpecUtil.extend(
						filter(method.getRequires(), SpecUtil.ACCEPT_GOOD),
						filter(type.getInvariants(), SpecUtil.ACCEPT_GOOD));

				problem.setAnnotatedBody(annotateBody(descriptor, project, method));
				problem.setRequires(assumedPreconditions);
				problem.setKnown(known);
			}
		}
	}
	
	/**
	 * The update performs the following actions:
	 * <ul>
	 * 	<li>Set the known preconditions to the set of statements that are known to be good</li>
	 *  <li>Markup the method source for display</li>
	 *  <li>Update whether or not the known preconditions are sufficient to eliminate all warnings</li>
	 * </ul>
	 * 
	 * @param descriptor the project descriptor
	 * @param project the project specification
	 * @param result the ESC/Java2 result for <code>project</code>
	 * @param problems the problems to update
	 */
	public static void updateWriteRequiresProblems(
			ProjectState state,
			ProjectResult result){
		
		ProjectSpecification project = state.getActiveSpec();
		ProjectDescriptor descriptor = state.getDescriptor();
		Set<WriteRequiresProblem> problems = state.getProblems().all(WriteRequiresProblem.class);
		
		for (TypeSpecification type : project.getTypeSpecs()){
			TypeResult typeResult = result.getTypeResult(type.getFullyQualifiedName());
			
			for (MethodContract method : type.getMethods()){
				MethodResult methodResult = Util.lookupMethodResult(typeResult, method);	
				WriteRequiresProblem problem = find(problems, sameLocation(method));
				
				List<Clause> known = Lists.newArrayList(filter(method.getRequires(), SpecUtil.ACCEPT_GOOD));
				boolean sufficient = EscJUtil.isSufficient(methodResult.getWarnings());
				
				problem.setAnnotatedBody(Util.annotateBody(descriptor, project, method, result, typeResult));
				problem.setKnown(known);
				problem.setSufficient(sufficient);
			}
		}
	}
	
	/**
	 * The update performs the following actions:
	 * <ul>
	 * 	<li>Make set of choices the set of preconditions (with syntactically bad preconditions marked as bad) and
	 *  method invariants from other public methods</li>
	 *  <li>Make set of active choices the set of known preconditions</li>
	 * 	<li>Markup the method source for display</li>
	 *  <li>Update whether or not the known preconditions are sufficient to eliminate all warnings</li>
	 * </ul>
	 * 
	 * @param descriptor the project descriptor
	 * @param project the project specification
	 * @param result the ESC/Java2 result for <code>project</code>
	 * @param problems the problems to update
	 */
	public static void updateSelectRequiresProblems(
			ProjectState state,
			ProjectResult result){
		
		ProjectSpecification project = state.getActiveSpec();
		ProjectDescriptor descriptor = state.getDescriptor();
		Set<SelectRequiresProblem> problems = state.getProblems().all(SelectRequiresProblem.class);
		
		for (TypeSpecification type : project.getTypeSpecs()){
			
			TypeResult typeResult = result.getTypeResult(type.getFullyQualifiedName());
			List<Clause> methodInvariants = ObjectInvariants.methodInvariants(type);
			
			for (MethodContract method : type.getMethods()){
				
				MethodResult methodResult = Util.lookupMethodResult(typeResult, method);
				SelectRequiresProblem problem = find(problems, sameLocation(method));
				
				boolean sufficient = EscJUtil.isSufficient(methodResult.getWarnings());
				
				// Deactivate choices that have invalid syntax (so they won't appear)
				List<Clause> goodSyntax = Lists.newArrayList(problem.getChoices());
				SpecUtil.resetStatus(goodSyntax, Status.SYNTAX_BAD, Status.KNOWN_BAD);
				
				//TODO should invariants be pushed into the set of choices? If so, need to handle constructors differently
				List<Clause> newChoices = SpecUtil.extend(method.getRequires(), SpecUtil.extend(goodSyntax, methodInvariants));
				Set<Clause> activeChoices = Sets.newHashSet(method.getRequires());
				
				problem.setAnnotatedBody(Util.annotateBody(descriptor, project, method, result, typeResult));
				problem.setChoices(newChoices);
				problem.setActive(activeChoices);
				problem.setSufficient(sufficient);
			}
		}
	}
	
	/**
	 * Add documentation and slots to the method source
	 * @param descriptor project descriptor
	 * @param project project specification
	 * @param method the method
	 * @return annotated method source
	 */
	private static String annotateBody(
			ProjectDescriptor descriptor, 
			ProjectSpecification project, 
			MethodContract method){
		
		String documentedBody = Util.addDocumentationTags(descriptor.getDefMap(), method, Util.allMethods(project));
		String slottedBody = Util.addSlots(documentedBody);
		return slottedBody;
	}
	


	/**
	 * Creates a predicate that matches problems associated with same source location as provided method
	 * @param <P> a {@link MethodProblem} subtype
	 * @param method to look for based on source location
	 * @return a predicate that matches problems associated with same source location as method
	 */
	private static <P extends MethodProblem> Predicate<P> sameLocation(final MethodContract method){
		return new Predicate<P>(){
			@Override
			public boolean apply(P methodProblem) {
				return methodProblem.getFunction().getInfo().getLocation().equals(method.getInfo().getLocation());
			}
		};
	}
	
	/**
	 * Returns the good preconditions for <code>method</code>;
	 * the statements all have status {@link Status#PENDING}.
	 * @param method the method
	 * @return the good preconditions for <code>method</code>
	 */
	private static Collection<Clause> borrowRequires(MethodContract method){
		return Collections2.transform(filter(method.getRequires(), SpecUtil.ACCEPT_GOOD), SpecUtil.RESET_PENDING);
	}
}
