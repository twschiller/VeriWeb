package com.schiller.veriasa.web.server.logging;

import com.schiller.veriasa.web.server.User;
import com.schiller.veriasa.web.shared.logging.LogAction;

public class SessionAction extends LogAction implements UserAction {
	private static final long serialVersionUID = 1L;

	public enum ActionType {Start, Restart}
	
	private User user;
	private ActionType action;
	
	@SuppressWarnings("unused")
	private SessionAction(){
		
	}
	
	public SessionAction(User user, ActionType action) {
		super();
		this.user = user;
		this.action = action;
	}

	/**
	 * @return the action
	 */
	public ActionType getAction() {
		return action;
	}

	@Override
	public User getUser() {
		return user;
	}

}
