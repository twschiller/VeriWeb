package com.schiller.veriasa.web.server.logging;

import java.io.Serializable;

import com.schiller.veriasa.web.server.ProjectState;
import com.schiller.veriasa.web.shared.logging.LogAction;

public class LogEntry implements Serializable {
	private static final long serialVersionUID = 1L;

	private long timestamp;
	private LogAction action;
	private ProjectState context;
	
	@SuppressWarnings("unused")
	private LogEntry(){
		timestamp = System.currentTimeMillis();
		action = null;
		context = null;
	}
	
	public LogEntry(LogAction action, ProjectState context){
		timestamp = System.currentTimeMillis();
		this.action = action;
		this.context = context;
	}

	/**
	 * @return the timestamp
	 */
	public long getTimestamp() {
		return timestamp;
	}

	/**
	 * @return the action
	 */
	public LogAction getAction() {
		return action;
	}

	/**
	 * @return the context
	 */
	public ProjectState getContext() {
		return context;
	}
}
