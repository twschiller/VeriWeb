package com.schiller.veriasa.web.shared;

import java.io.Serializable;

@SuppressWarnings("serial")
public class NoSuchProjectException extends Exception implements Serializable{

	private String project;

	@SuppressWarnings("unused")
	private NoSuchProjectException(){
		
	}
	
	public String getProject() {
		return project;
	}

	public void setProject(String project) {
		this.project = project;
	}

	public NoSuchProjectException(String project) {
		super();
		this.project = project;
	}
	
	
	
}
