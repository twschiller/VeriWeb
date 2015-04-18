package com.schiller.veriasa.web.server.logging;

import java.io.Serializable;

import com.schiller.veriasa.web.server.User;
import com.schiller.veriasa.web.shared.logging.HasProblem;
import com.schiller.veriasa.web.shared.logging.LogAction;
import com.schiller.veriasa.web.shared.problems.Problem;

public class ProblemAction extends LogAction implements Serializable, UserAction, HasProblem{
	private static final long serialVersionUID = 1L;

	public enum ActionType { Start, Done, Cancel, Solved};
	
	private User user;
	private Problem problem;
	private ActionType action;
	
	@SuppressWarnings("unused")
	private ProblemAction(){
		
	}
	public ProblemAction(User user, Problem problem, ActionType action){
		this.user = user;
		this.problem = problem;
		this.action = action;
	}
	
	@Override
	public User getUser() {
		return user;
	}
	@Override
	public Problem getProblem() {
		return problem;
	}

	public ActionType getAction(){
		return action;
	}
}
