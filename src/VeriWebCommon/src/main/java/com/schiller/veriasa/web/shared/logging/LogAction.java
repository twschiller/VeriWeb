package com.schiller.veriasa.web.shared.logging;

import java.io.Serializable;

public abstract class LogAction implements Serializable{
	private static final long serialVersionUID = 1L;

	public long timestamp;

	/**
	 * Create a log actions, using the current system time as the timestamp
	 */
	public LogAction(){
		timestamp = System.currentTimeMillis();
	}
}
