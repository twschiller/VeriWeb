package com.schiller.veriasa.web.server;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Iterables.filter;

import org.apache.log4j.Level;

import antlr.RecognitionException;
import antlr.TokenStreamException;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import com.schiller.veriasa.web.server.escj.EscJUtil;
import com.schiller.veriasa.web.server.logging.LogEntry;
import com.schiller.veriasa.web.server.logging.TrySpecsAction;
import com.schiller.veriasa.web.server.slicing.DynamicFeedbackUtil;
import com.schiller.veriasa.web.shared.config.EscJResponseException;
import com.schiller.veriasa.web.shared.core.MethodContract;
import com.schiller.veriasa.web.shared.core.MethodSpecBuilder;
import com.schiller.veriasa.web.shared.core.ProjectSpecification;
import com.schiller.veriasa.web.shared.core.Clause;
import com.schiller.veriasa.web.shared.core.Clause.Status;
import com.schiller.veriasa.web.shared.core.TypeSpecification;
import com.schiller.veriasa.web.shared.escj.AnnotatedFile;
import com.schiller.veriasa.web.shared.escj.Chunk;
import com.schiller.veriasa.web.shared.escj.Chunk.ChunkType;
import com.schiller.veriasa.web.shared.escj.MethodResult;
import com.schiller.veriasa.web.shared.escj.ProjectResult;
import com.schiller.veriasa.web.shared.escj.TypeResult;
import com.schiller.veriasa.web.shared.feedback.DynamicFeedback;
import com.schiller.veriasa.web.shared.feedback.SelectRequiresFeedback;
import com.schiller.veriasa.web.shared.feedback.WriteEnsuresFeedback;
import com.schiller.veriasa.web.shared.feedback.WriteExsuresFeedback;
import com.schiller.veriasa.web.shared.feedback.WriteRequiresFeedback;
import com.schiller.veriasa.web.shared.problems.SelectRequiresProblem;
import com.schiller.veriasa.web.shared.problems.WriteEnsuresProblem;
import com.schiller.veriasa.web.shared.problems.WriteExsuresProblem;
import com.schiller.veriasa.web.shared.problems.WriteRequiresProblem;
import com.schiller.veriasa.web.shared.update.SelectRequiresUpdate;
import com.schiller.veriasa.web.shared.update.WriteEnsuresUpdate;
import com.schiller.veriasa.web.shared.update.WriteExsuresUpdate;
import com.schiller.veriasa.web.shared.update.WriteRequiresUpdate;

/**
 * Methods providing solution feedback to the user
 * @author Todd Schiller
 */
public class VeriFeedback {

	/**
	 * Log level to record for the user
	 */
	private static Level FEEDBACK_LEVEL = Level.TRACE;

	/**
	 * returns <code>true</code> iff <code>user</code> should be prompted for invariants
	 * for <code>project</code>
	 * @param user the user
	 * @param project the project specification
	 * @return <code>true</code> iff <code>user</code> should be prompted for invariants
	 * for <code>project</code>
	 * @deprecated
	 */
	private static boolean promptInvariants(User user, ProjectSpecification project){
		// TODO remove this method
		return false;
		//return !project.getName().equals("FixedSizeSet");
	}
	
	/**
	 * Calculate feedback for a "select requires" problem
	 * @param solution the update to provide feedback for
	 * @param project the current project specification
	 * @param problem the problem
	 * @param descriptor project descriptor
	 * @param user the user
	 * @return feedback for a "select requires" problem
	 * @throws EscJResponseException
	 */
	protected static SelectRequiresFeedback selRequiresFeedback(
			SelectRequiresUpdate solution,
			SelectRequiresProblem problem,
			ProjectDescriptor descriptor,
			User user) throws EscJResponseException {
		
		ProjectSpecification project = user.getLocalSpecification();
		MethodContract method = Util.lookupMethod(project, problem);
		
		user.log(FEEDBACK_LEVEL, "SELECT REQ " + method);
		VeriServiceImpl.tryLog(new LogEntry(
				new TrySpecsAction(user, problem, solution.getSelected()),
				user.getProject()));
		
		// Mark all of the specifications the user has selected as good
		MethodContract modified = MethodSpecBuilder.builder(method)
				.setRequires(Lists.transform(solution.getSelected(), SpecUtil.RESET_GOOD))
				.getSpec();
		ProjectSpecification modifiedProject = Util.modifySpec(project, modified);

		// Calculate the result
		ProjectResult result = VeriServiceImpl.getResult(modifiedProject);
		user.log(FEEDBACK_LEVEL, "RESULT ID:" + result.getId());
		
		TypeResult typeResult = Util.getTypeResult(result, Util.getCompilationUnit(method));
		MethodResult methodResult = Util.lookupMethodResult(typeResult, method);
	
		return new SelectRequiresFeedback(
				Util.annotateBody(descriptor, modifiedProject, method, result, typeResult),
				EscJUtil.isSufficient(methodResult.getWarnings()));
	}
	
	protected static WriteRequiresFeedback writeRequiresFeedback(
			WriteRequiresUpdate update, 
			WriteRequiresProblem problem,
			ProjectDescriptor descriptor,
			User user) throws EscJResponseException{
		
		ProjectSpecification project = user.getLocalSpecification();
		MethodContract method = Util.lookupMethod(project, problem);
		String cu = Util.getCompilationUnit(method);
		
		user.log(FEEDBACK_LEVEL, "WRITE REQ " + method);
		
		// store old values, and reuse if there is a syntax error
		String annotatedBody = problem.getAnnotatedBody();
		boolean sufficient = problem.isSufficient();
		
		// Remove preconditions that are known to be bad
		List<Clause> userWritten = Lists.newArrayList(filter(((WriteRequiresUpdate) update).getSpecs(), SpecUtil.REJECT_BAD));
		
		VeriServiceImpl.tryLog(new LogEntry(
				new TrySpecsAction(user, problem, userWritten),
				user.getProject()));
		
		DynamicFeedback dynamicFeedback = null;
		
		if (!userWritten.isEmpty()){
			// skip if user deleted last remaining postcondition
			
			Clause lastWritten = userWritten.get(userWritten.size() - 1);
			try {
				dynamicFeedback = DynamicFeedbackUtil.executeJmlPrecondition(lastWritten.getClause(), method, descriptor);
			}catch (RecognitionException e){
				//let ESC/Java2 deal with parse errors
				dynamicFeedback = null;
			}catch (TokenStreamException e){
				//let ESC/Java2 deal with parse errors
				dynamicFeedback = null;
			}catch (Exception e){
				//let ESC/Java2 deal with parse errors
				dynamicFeedback = null;
			}
			
			if (dynamicFeedback != null){
				userWritten.set(userWritten.size() - 1, new Clause(lastWritten.getClause(), lastWritten.getProvenance(), Status.KNOWN_BAD, null));
				return new WriteRequiresFeedback(Util.addSlots(annotatedBody), sufficient, userWritten, dynamicFeedback);
			}
		}
		
		// Create specification with modified preconditions
		MethodContract modified = MethodSpecBuilder.builder(method).setRequires(userWritten).getSpec();
		ProjectSpecification modifiedProject = Util.modifySpec(project, modified);
		
		ProjectResult result = VeriServiceImpl.getResult(modifiedProject);
		
		user.log(FEEDBACK_LEVEL, "RESULT ID:" + result.getId());

		for (Chunk warning : result.getSpecErrors()){
			AnnotatedFile lineMap = result.getLineMaps().get(EscJUtil.getCompilationUnit(warning));
			SpecUtil.resetStatus(lineMap.getSpec(warning.getLine()-1), userWritten, Status.SYNTAX_BAD, warning.getMessage());	
		}

		if (!result.hasFatalProjError()){
			TypeResult typeResult = Util.getTypeResult(result, cu);
			AnnotatedFile lineMap = result.getLineMaps().get(cu);

			for (Chunk warning : typeResult.getWarnings()){
				if (warning.getMessageType() == ChunkType.CAUTION || warning.getMessageType() == ChunkType.UNKNOWN){
					continue;
				}else if (!EscJUtil.isJavaSourceWarning(warning)){
					throw new RuntimeException(warning.getMessage());
				}else{
					//subtract one because the map is 0-indexed
					SpecUtil.resetStatus(lineMap.getSpec(warning.getLine()-1), userWritten, Status.SYNTAX_BAD, warning.getMessage());
				}
			}

			if (!typeResult.isAborted()){
				MethodResult methodResult = Util.lookupMethodResult(typeResult, method);

				sufficient = EscJUtil.isSufficient(methodResult.getWarnings());

				for (Chunk warning : methodResult.getWarnings()){

					//ignore unsatisfied preconditions (WHY?)
					if (EscJUtil.isUnsatisfiedPreconditionWarning(warning)){
						continue;
					}

					//subtract one because the map is 0-indexed
					int line = warning.getAssociatedDeclaration() != null 
							? warning.getAssociatedDeclaration().getLine() - 1 
							: warning.getLine() - 1;

					if (lineMap.refersToSpec(line)){
						SpecUtil.resetStatus(lineMap.getSpec(line), userWritten, Status.KNOWN_BAD, warning.getMessage());
					}
				}

				// Mark all statements without an associated warning as good
				SpecUtil.changeStatus(userWritten, Status.PENDING, Status.KNOWN_GOOD);
				
				annotatedBody = Util.annotateBody(descriptor, modifiedProject, method, result, typeResult);
			}
		}
		return new WriteRequiresFeedback(Util.addSlots(annotatedBody), sufficient, userWritten, dynamicFeedback);
	}
	
	protected static WriteEnsuresFeedback ensuresFeedback(
			WriteEnsuresUpdate update,
			WriteEnsuresProblem problem,
			User user) throws EscJResponseException{
		
		ProjectSpecification project = user.getLocalSpecification();
		MethodContract method = Util.lookupMethod(project, problem);
		String cu = Util.getCompilationUnit(method);
		
		user.log(FEEDBACK_LEVEL, "ENSURES UPDATE " + method.getSignature() + " " + update.getClause().getClause());

		LinkedList<Clause> userWritten = Lists.newLinkedList(method.getEnsures());
		userWritten.add(update.getClause());
		
		VeriServiceImpl.tryLog(new LogEntry(
				new TrySpecsAction(user, problem, userWritten),
				user.getProject()));
		
		// Create a new project specification by modifying the ensures clauses
		MethodContract modified = MethodSpecBuilder.builder(method).setEnsures(userWritten).getSpec();	
		ProjectSpecification modifiedProject = Util.modifySpec(project, modified);
		
		ProjectResult result = VeriServiceImpl.getResult(modifiedProject);
		
		DynamicFeedback dynamicFeedback = null;
		try {
			dynamicFeedback = DynamicFeedbackUtil.executeJmlPostcondition(update.getClause().getClause(), method, user.getProject().getDescriptor());
		} catch (Exception e){
			//let ESC/Java2 deal with parse errors
		}
		
		if (dynamicFeedback != null){
			Clause statementAdded = update.getClause();
			statementAdded = new Clause(statementAdded.getClause(), statementAdded.getProvenance(), Status.KNOWN_BAD, null);
			return new WriteEnsuresFeedback(statementAdded, dynamicFeedback, false);
		}
		
		user.log(FEEDBACK_LEVEL, "RESULT ID:" + result.getId());

		for (Chunk warning : result.getSpecErrors()){
			AnnotatedFile lineMap = result.getLineMaps().get(EscJUtil.getCompilationUnit(warning));
			SpecUtil.resetStatus(lineMap.getSpec(warning.getLine() - 1), userWritten, Status.SYNTAX_BAD, warning.getMessage());
		}

		if (!result.hasFatalProjError()){
			TypeResult typeResult = Util.getTypeResult(result, cu);
			AnnotatedFile lineMap = result.getLineMaps().get(cu);

			for (Chunk warning : typeResult.getWarnings()){
				if (warning.getMessageType() == ChunkType.CAUTION || warning.getMessageType() == ChunkType.UNKNOWN){
					continue;
				}else if (!EscJUtil.isJavaSourceWarning(warning)){
					throw new RuntimeException(warning.getMessage());
				}else{
					//subtract one because the map is 0-indexed
					SpecUtil.resetStatus(lineMap.getSpec(warning.getLine()-1), userWritten, Status.SYNTAX_BAD, warning.getMessage());	
				}
			}

			if (!typeResult.isAborted()){
				MethodResult methodResult = Util.lookupMethodResult(typeResult, method);

				for (Chunk warning : methodResult.getWarnings()){
					Clause badStatement = warning.getAssociatedDeclaration() != null 
							? lineMap.getSpec(warning.getAssociatedDeclaration().getLine()-1) 
							: lineMap.getSpec(warning.getLine() - 1);
					SpecUtil.resetStatus(badStatement, userWritten, Status.KNOWN_BAD, warning.getMessage());
				}

				// Mark all statements without an associated warning as good
				SpecUtil.changeStatus(userWritten, Status.PENDING, Status.KNOWN_GOOD);
			}
		}
		
		Clause last = userWritten.getLast();
	
		TypeSpecification modifiedType = Util.findType(modifiedProject, method);
		boolean promptAsObjectInvariant = false;
		if (last.getStatus() == Status.KNOWN_GOOD 
			&& ObjectInvariants.exceedsThreshold(modifiedType, last)
			&& !ObjectInvariants.alreadyObjectInvariant(modifiedType, last)){
			
			if (!ObjectInvariants.isInvariantSafe(modifiedProject, result, modifiedType, last)){
				promptAsObjectInvariant = true;
			}
		}
		
		return new WriteEnsuresFeedback(last, dynamicFeedback, promptAsObjectInvariant && promptInvariants(user, project));
	}
	
	protected static WriteExsuresFeedback exsuresFeedback(
			WriteExsuresUpdate update, 
			WriteExsuresProblem problem,
			User user) throws EscJResponseException{
		
		ProjectSpecification project = user.getLocalSpecification();
		
		MethodContract method = Util.lookupMethod(project, problem);
		String cu = Util.getCompilationUnit(method);
		String exception = problem.getException();
		
		user.log(FEEDBACK_LEVEL, "EXSURES UPDATE " + method + " " + update.getClause().getClause());
		
		LinkedList<Clause> userWritten = Lists.newLinkedList(method.getExsures().get(exception));
		userWritten.add(update.getClause());
		
		VeriServiceImpl.tryLog(new LogEntry(
				new TrySpecsAction(user, problem, userWritten),
				user.getProject()));
		
		Map<String, List<Clause>> allExsures = Maps.newHashMap(method.getExsures());
		allExsures.put(exception, userWritten);
		
		// Create a new specification by modifying the exsures clauses
		MethodContract modified = MethodSpecBuilder.builder(method).setExsures(allExsures).getSpec();
		ProjectSpecification modifiedProject = Util.modifySpec(project, modified);
		
		ProjectResult result = VeriServiceImpl.getResult(modifiedProject);

		user.log(FEEDBACK_LEVEL, "RESULT ID:" + result.getId());

		for (Chunk warning : result.getSpecErrors()){
			AnnotatedFile lineMap = result.getLineMaps().get(EscJUtil.getCompilationUnit(warning));
			SpecUtil.resetStatus(lineMap.getSpec(warning.getLine() - 1), userWritten, Status.SYNTAX_BAD, warning.getMessage());
		}

		if (!result.hasFatalProjError()){
			TypeResult typeResult = Util.getTypeResult(result, cu);
			AnnotatedFile lineMap = result.getLineMaps().get(cu);

			for (Chunk warning : typeResult.getWarnings()){
				if (!EscJUtil.isJavaSourceWarning(warning)){
					throw new RuntimeException(warning.getMessage());
				}
				SpecUtil.resetStatus(lineMap.getSpec(warning.getLine() - 1), userWritten, Status.SYNTAX_BAD, warning.getMessage());	
			}

			if (!typeResult.isAborted()){
				MethodResult methodResult = Util.lookupMethodResult(typeResult, method);

				for (Chunk warning : methodResult.getWarnings()){
					Clause badStatement = warning.getAssociatedDeclaration() != null 
							? lineMap.getSpec(warning.getAssociatedDeclaration().getLine() - 1) 
							: lineMap.getSpec(warning.getLine() - 1);

					SpecUtil.resetStatus(badStatement, userWritten, Status.KNOWN_BAD, warning.getMessage());
				}

				SpecUtil.changeStatus(userWritten, Status.PENDING, Status.KNOWN_GOOD);
			}
		}
		
		Clause last = userWritten.getLast();
		
		TypeSpecification modifiedType = Util.findType(modifiedProject, method);
		boolean promptAsObjectInvariant = false;
		if (last.getStatus() == Status.KNOWN_GOOD 
				&& ObjectInvariants.exceedsThreshold(modifiedType, last)
				&& !ObjectInvariants.alreadyObjectInvariant(modifiedType, last)){
			
			if (!ObjectInvariants.isInvariantSafe(modifiedProject, result, modifiedType, last)){
				promptAsObjectInvariant = true;
			}
		}
		
		return new WriteExsuresFeedback(last, null, promptAsObjectInvariant && promptInvariants(user, project));
	}
	
}
