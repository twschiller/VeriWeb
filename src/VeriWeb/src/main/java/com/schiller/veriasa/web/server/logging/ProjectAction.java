package com.schiller.veriasa.web.server.logging;

import com.schiller.veriasa.web.shared.logging.LogAction;

public class ProjectAction extends LogAction {
	private static final long serialVersionUID = 2L;

	public enum ActionType {Start, Solved}
	
	private ActionType action;
	private String projectName;
	
	@SuppressWarnings("unused")
	private ProjectAction(){
		
	}
	
	public ProjectAction(String projectName, ActionType action){
		this.action = action;
		this.projectName = projectName;
	}

	/**
	 * @return the type
	 */
	public ActionType getAction() {
		return action;
	}

	/**
	 * @return the project name
	 */
	public String getProjectName() {
		return projectName;
	}
}
