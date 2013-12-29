package com.schiller.veriasa.web.server;

import static com.google.common.collect.Collections2.filter;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Level;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.schiller.veriasa.web.server.callgraph.ProblemGraph;
import com.schiller.veriasa.web.server.callgraph.ProblemGraph.ProblemGraphNode;
import com.schiller.veriasa.web.server.escj.EscJUtil;
import com.schiller.veriasa.web.server.logging.LogEntry;
import com.schiller.veriasa.web.server.logging.MessageAction;
import com.schiller.veriasa.web.shared.config.EscJResponseException;
import com.schiller.veriasa.web.shared.core.MethodContract;
import com.schiller.veriasa.web.shared.core.MethodSpecBuilder;
import com.schiller.veriasa.web.shared.core.Clause;
import com.schiller.veriasa.web.shared.core.Clause.Status;
import com.schiller.veriasa.web.shared.escj.ProjectResult;
import com.schiller.veriasa.web.shared.messaging.ImpossibleMessage;
import com.schiller.veriasa.web.shared.messaging.UserMessage.Vote;
import com.schiller.veriasa.web.shared.messaging.UserMessageThread;
import com.schiller.veriasa.web.shared.problems.MethodProblem;
import com.schiller.veriasa.web.shared.problems.SelectRequiresProblem;
import com.schiller.veriasa.web.shared.problems.WriteEnsuresProblem;
import com.schiller.veriasa.web.shared.problems.WriteExsuresProblem;
import com.schiller.veriasa.web.shared.problems.WriteRequiresProblem;
import com.schiller.veriasa.web.shared.solutions.HasImpossibleInfo;
import com.schiller.veriasa.web.shared.solutions.SelectRequiresSolution;
import com.schiller.veriasa.web.shared.solutions.Solution;
import com.schiller.veriasa.web.shared.solutions.WriteEnsuresSolution;
import com.schiller.veriasa.web.shared.solutions.WriteExsuresSolution;
import com.schiller.veriasa.web.shared.solutions.WriteRequiresSolution;

/**
 * Record a users solution to a subproblem, and update the other subproblems accordingly
 * @author Todd Schiller
 */
public abstract class VeriRecord {

	public static void recordSolution(User user, Solution solution) throws EscJResponseException{
		ProjectState state = user.getProject();
		
		synchronized(state){
			ProblemGraph problems = state.getProblems();
			ProblemGraphNode problem = user.getActiveProblem();
			
			boolean valid = !problems.isInvalidated(problem);
			
			MethodContract method = Util.lookupMethod(state.getActiveSpec(), (MethodProblem) problem.getProblem());
		
			boolean doUpdate = false;
		
			// dispatch to the problem solution handler
			if (solution instanceof WriteEnsuresSolution){
				doUpdate = recordSolution(user, (WriteEnsuresSolution) solution, method, valid);
			}else if(solution instanceof WriteExsuresSolution){
				doUpdate = recordSolution(user, (WriteExsuresSolution) solution, method, valid);
			}else if (solution instanceof WriteRequiresSolution){
				doUpdate = recordSolution(user, (WriteRequiresSolution) solution, method, valid);
			}else if (solution instanceof SelectRequiresSolution){
				doUpdate = recordSolution(user, (SelectRequiresSolution) solution, method, valid);
			}else{
				throw new RuntimeException("Invalid solution type received");
			}
		
			if (valid){
				// do before update since it affects current set of object invariants
				state.markVisited(problem);
				
				if (doUpdate){
					update(state);
					problems.invalidate(problem);
				}
				
				problems.updateEnabled();

				if (!problems.hasEnabled()){
					ProjectResult result = VeriServiceImpl.getResult(state.getActiveSpec());
					if (EscJUtil.hasErrors(result)){
						state.log(Level.ERROR, "Final specification does not validate " + state.getName());
						throw new RuntimeException("Error validating final specification for " + state.getName());
					}else{
						state.markSolved();
					}
				}
			}else if (doUpdate){
				update(state);
			}
		} // synchronized(state)
	}
	
	private static void update(ProjectState state) throws EscJResponseException{
		synchronized(state){
			// first calculate the object invariants, since the other updates depend on them
			// state.calculateObjectInvariants();
			
			// update all of the problems, if necessary
			ProjectResult result = VeriServiceImpl.getResult(state.getActiveSpec());
			state.log(Level.INFO, "RESULT ID:" + result.getId());
			VeriUpdate.updateSelectRequiresProblems(state, result);
			VeriUpdate.updateWriteRequiresProblems(state, result);
			VeriUpdate.updateWriteEnsuresProblems(state, result);
			VeriUpdate.updateWriteExsuresProblems(state, result);
		
			if (!EscJUtil.hasErrors(result)){
				state.markSolved();
			}
		}
	}
	
	public static boolean recordSolution(User user, WriteRequiresSolution solution, MethodContract method, boolean valid){
		ProjectState state = user.getProject();
		
		synchronized(state){
			ProblemGraph problems = state.getProblems();
		
			if (valid){
				Collection<Clause> oldRequires = filter(method.getRequires(), SpecUtil.ACCEPT_GOOD);
				List<Clause> newRequires = Lists.newArrayList(filter(solution.getStatements(), SpecUtil.ACCEPT_GOOD));
				
				problems.finish(user, user.getActiveProblem(), "Solution submitted by " + user);
				
				if (!SpecUtil.equals(oldRequires, newRequires)){
					if (SpecUtil.isPreconditionSetWeaker(Lists.newLinkedList(oldRequires), newRequires)){
						//check the ensures clause for this method
						problems.enable(WriteEnsuresProblem.class, method, "Preconditions were weakened");
						
						//check the exsures clause for this method, if it has one
						if (state.find(WriteExsuresProblem.class, method) != null){
							problems.enable(WriteExsuresProblem.class, method, "Preconditions were weakened");
						}
					}
					
					//The precondition set must be stronger, because it is not equal to what it was before
					problems.enableDependentProblems(user.getActiveProblem(), ProblemGraphNode.IS_SELREQ,  "Preconditions for " + method.getSignature() + " were strengthened");
					recordReqImpossibleInfo(user, solution, method);
				
					MethodContract modified = MethodSpecBuilder.builder(method).setRequires(newRequires).getSpec();

					state.update(modified);
					user.addSolved();
					return true;
				}else{
					recordReqImpossibleInfo(user, solution, method);
					user.addSolved();
					return false;	
				}
			}else{
				List<Clause> staleRequires = Lists.newArrayList(filter(solution.getStatements(), SpecUtil.ACCEPT_GOOD));
				List<Clause> modifiedRequires = SpecUtil.extend(
						method.getRequires(),
						Lists.transform(staleRequires, SpecUtil.RESET_PENDING));
				
				MethodContract modified = MethodSpecBuilder.builder(method).setRequires(modifiedRequires).getSpec();
			
				state.update(modified);
				user.addSolved();
				return false;
			}
		}
	}

	public static boolean recordSolution(User user, SelectRequiresSolution solution, MethodContract method, boolean valid){
		if (!valid){
			return false;
		}
		
		ProjectState state = user.getProject();
		
		synchronized(state){
			ProblemGraph problems = state.getProblems();
			ProblemGraphNode problem = user.getActiveProblem();
		
			Set<Clause> oldRequires = ((SelectRequiresProblem) problem.getProblem()).getActive();
			List<Clause> selected = Lists.transform(solution.getSelected(), new SpecUtil.ChangeStatus(Status.KNOWN_GOOD));
			
			problems.finish(user, problem, "Solution submitted by " + user);
			
			if (!SpecUtil.equals(oldRequires, selected)){
				if (SpecUtil.isPreconditionSetWeaker(oldRequires, selected)){
					//check the ensures clause for this method
					problems.enable(WriteEnsuresProblem.class, method, "Requires weakened for " + method);
					
					//check the exsures clause for this method, if it has one
					if (state.find(WriteExsuresProblem.class, method) != null){
						problems.enable(WriteExsuresProblem.class, method, "Requires weakened for " + method);
					}
				}else if (SpecUtil.isPreconditionSetStronger(oldRequires, selected)){
					problems.enableDependentProblems(problem, ProblemGraphNode.IS_SELREQ, "Requires strengthened for " + method);
				}else{
					throw new RuntimeException("Internal Error: bad precondition set comparison");
				}
				
				recordReqImpossibleInfo(user, solution, method);
			
				MethodContract modified = MethodSpecBuilder
						.builder(method)
						.setRequires(Lists.transform(selected, new SpecUtil.ChangeStatus(Status.KNOWN_GOOD)))
						.getSpec();
		
				state.update(modified);
				user.addSolved();
				return true;
			}else{
				recordReqImpossibleInfo(user, solution, method);
				user.addSolved();
				return false;
			}
		}
	}
	
	/**
	 * Record the user's solution to a write ensures problem
	 * <ul>
	 * 	<li>if the user approved object invariants, push those invariants to the other methods' preconditions</li>
	 *  <li>if the user weakened the postconditions for the method, activate all of the dependent problems</li>
	 * </ul>
	 * @param user the user
	 * @param solution the user's solution
	 * @param method the method being worked on
	 * @return <code>true</code> iff an update must be performed for all the problems
	 */
	public static boolean recordSolution(User user, WriteEnsuresSolution solution, MethodContract method, boolean valid){
		ProjectState state = user.getProject();
		
		synchronized(state){
			if (valid){
				Collection<Clause> oldEnsures = filter(method.getEnsures(), SpecUtil.ACCEPT_GOOD);
				List<Clause> newEnsures = Lists.newArrayList(filter(solution.getStatements(), SpecUtil.ACCEPT_GOOD));
				
				boolean updateRequired = false;
				
				ProjectState project = user.getProject();
				ProblemGraph problems = project.getProblems();
				
				problems.finish(user, user.getActiveProblem(), "Solution submitted by " + user);
			
				for (Clause objectInvariant : solution.getApprovedObjectInvariants()){
					ObjectInvariants.log.debug("Propogating object invariant " + objectInvariant);
					project.addObjectInvariant(Util.findType(project.getActiveSpec(), method), objectInvariant, Lists.newArrayList(method));
					updateRequired = true;
				}
				
				if (!SpecUtil.equals(oldEnsures, newEnsures)){
					if (SpecUtil.isPostconditionSetWeaker(oldEnsures, newEnsures)){
						problems.enableDependentProblems(user.getActiveProblem(), ProblemGraphNode.IS_ANY, "Ensures weakened for " + method);
					}
					recordReqImpossibleInfo(user, solution, method);
					
					MethodContract modified = MethodSpecBuilder.builder(method).setEnsures(newEnsures).getSpec();
			
					project.update(modified);
					user.addSolved();
					return true; // updateRequired
				}
				
				recordReqImpossibleInfo(user, solution, method);
				user.addSolved();
				return updateRequired;
			
			}else{
				List<Clause> staleEnsures = Lists.newArrayList(filter(solution.getStatements(), SpecUtil.ACCEPT_GOOD));
				List<Clause> modifiedEnsures = SpecUtil.extend(
						method.getEnsures(),
						Lists.transform(staleEnsures, SpecUtil.RESET_PENDING));
				
				MethodContract modified = MethodSpecBuilder.builder(method).setEnsures(modifiedEnsures).getSpec();
			
				state.update(modified);
				user.addSolved();
				return false;	
			}
		}
	}
	
	/**
	 * Record the user's solution to a write exsures problem
	 * <ul>
	 * 	<li>if the user approved object invariants, push those invariants to the other methods' preconditions</li>
	 *  <li>if the user weakened the postconditions for the method, activate all of the dependent problems</li>
	 * </ul>
	 * @param user the user
	 * @param solution the user's solution
	 * @param method the method being worked on
	 * @return <code>true</code> iff an update must be performed for all the problems
	 */
	public static boolean recordSolution(User user, WriteExsuresSolution solution, MethodContract method, boolean valid){
		ProjectState state = user.getProject();
		
		synchronized(state){
			String exception = ((WriteExsuresProblem) user.getActiveProblem().getProblem()).getException();
			
			if (valid){
				Collection<Clause> oldExsures = filter(method.getExsures().get(exception), SpecUtil.ACCEPT_GOOD);
				List<Clause> newExsures = Lists.newArrayList(filter(solution.getStatements(), SpecUtil.ACCEPT_GOOD));
				
				Map<String, List<Clause>> allNewExsures = Maps.newHashMap(method.getExsures());
				allNewExsures.put(exception, newExsures);
				
				ProjectState project = user.getProject();
				ProblemGraph problems = project.getProblems();
				
				problems.finish(user, user.getActiveProblem(), "Solution submitted by " + user);
				
				for (Clause objectInvariant : solution.getApprovedObjectInvariants()){
					ObjectInvariants.log.debug("Propogating object invariant " + objectInvariant);
					project.addObjectInvariant(Util.findType(project.getActiveSpec(), method), objectInvariant, Lists.newArrayList(method));
				}
				
				if (SpecUtil.isPostconditionSetWeaker(oldExsures, newExsures)){
					problems.enableDependentProblems(user.getActiveProblem(), ProblemGraphNode.IS_ANY, "Exsures weakened for " + method);
				}
				
				recordReqImpossibleInfo(user, solution, method);
				
				MethodContract modified = MethodSpecBuilder.builder(method).setExsures(allNewExsures).getSpec();

				project.update(modified);
				user.addSolved();
				return true;
			}else{
				List<Clause> staleExsures = Lists.newArrayList(filter(solution.getStatements(), SpecUtil.ACCEPT_GOOD));
				List<Clause> modifiedExsures = SpecUtil.extend(
						method.getExsures().get(exception),
						Lists.transform(staleExsures, SpecUtil.RESET_PENDING));
				
				MethodContract modified = MethodSpecBuilder.builder(method).setExsures(exception, modifiedExsures).getSpec();
			
				state.update(modified);
				user.addSolved();
				return false;
			}	
		}
	}
	
	public static void recordReqImpossibleInfo(User user, HasImpossibleInfo solution, MethodContract method){
		ProjectState state = user.getProject();
		
		synchronized(state){
			
			ProblemGraph problems = state.getProblems();
			ProblemGraphNode problem = user.getActiveProblem();
					
			//Add to the list of messages
			if (solution.hasInfo()){
				String methodToBlame = solution.getInfo().getMethod();
				
				//Create message
				ImpossibleMessage message = new ImpossibleMessage(
						user.getId(),
						method.getSignature(),
						solution.getInfo().getAssociatedStatement(),
						solution.getInfo().getComment(),
						Vote.NO_VOTE,
						solution.getInfo().getReason());
				state.addMessage(methodToBlame, new UserMessageThread(message));
				
				//Log message
				user.log(Level.INFO, message.toString());	
				VeriServiceImpl.tryLog(new LogEntry(
						new MessageAction(user, problem.getProblem(), message),
						state));
				
				//activate the necessary nodes
				switch(solution.getInfo().getReason()){
				case STRONG_REQ:
					user.addPreference(problem);
					
					ProblemGraphNode selectRequiresToBlame = state.find(SelectRequiresProblem.class, methodToBlame);
					user.addPreference(selectRequiresToBlame);
					
					problems.enable(problem, "User thinks preconditions for " + methodToBlame + " are too strong");
					problems.enable(selectRequiresToBlame, "User thinks preconditions are too strong");
					selectRequiresToBlame.setIgnoreSufficient(true);
					break;
				case WEAK_ENS:
					user.addPreference(problem);
					
					ProblemGraphNode writeEnsuresToBlame = state.find(WriteEnsuresProblem.class, methodToBlame);
					user.addPreference(writeEnsuresToBlame);
					
					problems.enable(problem, "User thinks ensures clauses for " + methodToBlame + "are too weak");
					problems.enable(writeEnsuresToBlame, "User says the ensures clauses are too weak");
					break;
				case WEAK_EXS:
					user.addPreference(problem);
					ProblemGraphNode writeExsuresToBlame = state.find(WriteExsuresProblem.class, methodToBlame);
					user.addPreference(writeExsuresToBlame);
					
					problems.enable(problem, "User thinks exsures clauses for " + methodToBlame + "are too weak");
					problems.enable(writeExsuresToBlame, "User says its exsures clauses are too weak");
					break;
				case NOT_LISTED:
					ProblemGraphNode writeRequiresToBlame = state.find(WriteRequiresProblem.class, method.getSignature());
					problems.enable(writeRequiresToBlame, "User says necessary precondition is not listed");
					writeRequiresToBlame.setIgnoreSufficient(true);
					user.addPreference(writeRequiresToBlame);
					break;
				case BUG:
				default:
					break;
				}
			}	
		}
	}
	
}
