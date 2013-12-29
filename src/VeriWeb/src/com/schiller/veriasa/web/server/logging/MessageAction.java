package com.schiller.veriasa.web.server.logging;

import com.schiller.veriasa.web.server.User;
import com.schiller.veriasa.web.shared.logging.HasProblem;
import com.schiller.veriasa.web.shared.logging.LogAction;
import com.schiller.veriasa.web.shared.messaging.UserMessage;
import com.schiller.veriasa.web.shared.problems.Problem;

public class MessageAction extends LogAction implements HasProblem, UserAction{
	private static final long serialVersionUID = 2L;
	
	private UserMessage message;
	private User user;
	private Problem problem;
	
	@SuppressWarnings("unused")
	private MessageAction(){
		
	}
	
	public MessageAction(User user, Problem problem, UserMessage message){
		this.user = user;
		this.problem = problem;
		this.message = message;
	}
	
	public UserMessage getMessage(){
		return message;
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
