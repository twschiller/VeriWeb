package com.schiller.veriasa.web.shared.config;

import java.io.Serializable;

/**
 * Exception thrown when the user requests an invalid project
 * @author Todd Schiller
 */
public class NoSuchProjectException extends Exception implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private String project;

	@SuppressWarnings("unused")
	private NoSuchProjectException(){
	}
	
	/**
	 * @param project the requested project
	 */
	public NoSuchProjectException(String project) {
		this.project = project;
	}
	
	/**
	 * @return the requested project
	 */
	public String getProject() {
		return project;
	}	
}
