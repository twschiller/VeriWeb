package com.schiller.veriasa.web.server.logging;

import com.schiller.veriasa.web.client.views.ClauseWriter;
import com.schiller.veriasa.web.client.views.ClauseWriter.WriteMode;
import com.schiller.veriasa.web.server.User;
import com.schiller.veriasa.web.shared.logging.LogAction;

public class SwitchModeAction extends LogAction implements UserAction{
	private static final long serialVersionUID = 1L;
	
	private User user;
	private ClauseWriter.WriteMode newMode;
	
	@SuppressWarnings("unused")
	private SwitchModeAction(){
		
	}
	
	public SwitchModeAction(User user, WriteMode newMode) {
		super();
		this.user = user;
		this.newMode = newMode;
	}

	/**
	 * @return the newMode
	 */
	public ClauseWriter.WriteMode getNewMode() {
		return newMode;
	}

	@Override
	public User getUser() {
		return user;
	}
}
