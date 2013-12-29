package com.schiller.veriasa.web.server.logging;

import java.util.List;

import com.schiller.veriasa.web.server.User;
import com.schiller.veriasa.web.shared.core.Clause;
import com.schiller.veriasa.web.shared.logging.HasProblem;
import com.schiller.veriasa.web.shared.logging.LogAction;
import com.schiller.veriasa.web.shared.problems.Problem;


public class TrySpecsAction extends LogAction implements HasProblem, UserAction{

	private static final long serialVersionUID = 1L;

	private List<Clause> specs;
	private User user;
	private Problem problem;
	
	@SuppressWarnings("unused")
	private TrySpecsAction(){
		
	}
	
	public TrySpecsAction(User user, Problem problem, List<Clause> specs){
		this.user = user;
		this.problem = problem;
		this.specs = specs;
	}
	
	@Override
	public User getUser() {
		return user;
	}

	@Override
	public Problem getProblem() {
		return problem;
	}

	/**
	 * @return the specs
	 */
	public List<Clause> getSpecs() {
		return specs;
	}
}
