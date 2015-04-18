package com.schiller.veriasa.web.server.logging;

import com.schiller.veriasa.web.server.User;
import com.schiller.veriasa.web.shared.logging.HasProblem;
import com.schiller.veriasa.web.shared.logging.LogAction;
import com.schiller.veriasa.web.shared.messaging.UserMessage;
import com.schiller.veriasa.web.shared.messaging.UserMessage.Vote;
import com.schiller.veriasa.web.shared.problems.Problem;

public class VoteAction extends LogAction implements HasProblem, UserAction{
	private static final long serialVersionUID = 2L;

	private Vote vote;
	private UserMessage message;
	private User user;
	private Problem problem;
	
	@SuppressWarnings("unused")
	private VoteAction(){
		
	}
	
	public VoteAction(User user, Problem problem, UserMessage message, Vote vote){
		this.user = user;
		this.problem = problem;
		this.message = message;
		this.vote = vote;
	}
	
	public UserMessage getMessage(){
		return message;
	}
	
	/**
	 * @return the vote
	 */
	public Vote getVote() {
		return vote;
	}

	@Override
	public User getUser() {
		return user;
	}

	@Override
	public Problem getProblem() {
		return problem;
	}
}
