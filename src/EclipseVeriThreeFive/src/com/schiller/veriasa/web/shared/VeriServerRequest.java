package com.schiller.veriasa.web.shared;

import java.io.Serializable;

@SuppressWarnings("serial")
public class VeriServerRequest implements Serializable{

	private ProjectSpec project;
	
	@SuppressWarnings("unused")
	private VeriServerRequest(){
		
	}

	public VeriServerRequest(ProjectSpec project) {
		super();
		this.project = project;
	}

	public ProjectSpec getProject() {
		return project;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((project == null) ? 0 : project.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		VeriServerRequest other = (VeriServerRequest) obj;
		if (project == null) {
			if (other.project != null)
				return false;
		} else if (!project.equals(other.project))
			return false;
		return true;
	}
}
