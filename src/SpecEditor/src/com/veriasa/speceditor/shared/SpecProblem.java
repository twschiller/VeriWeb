package com.veriasa.speceditor.shared;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings("serial")
public class SpecProblem implements Serializable{

	private FunctionInfo info;
	private List<String> inferredPres;
	private List<String> inferredPosts;
	private Set<String> activePres;
	private Set<String> activePosts;

	@SuppressWarnings("unused")
	private SpecProblem(){
		
	}
	
	public SpecProblem(FunctionInfo info, List<String> pres, List<String> posts){
		this.info = info;
		
		inferredPres = pres;
		inferredPosts = posts;
		
		activePres = new HashSet<String>();
		activePosts = new HashSet<String>();
	}

	public FunctionInfo getInfo() {
		return info;
	}

	public void setInfo(FunctionInfo info) {
		this.info = info;
	}

	public List<String> getInferredPres() {
		return inferredPres;
	}

	public void setInferredPres(List<String> inferredPres) {
		this.inferredPres = inferredPres;
	}

	public List<String> getInferredPosts() {
		return inferredPosts;
	}

	public void setInferredPosts(List<String> inferredPosts) {
		this.inferredPosts = inferredPosts;
	}

	public Set<String> getActivePres() {
		return activePres;
	}

	public void setActivePres(Set<String> activePres) {
		this.activePres = activePres;
	}

	public Set<String> getActivePosts() {
		return activePosts;
	}

	public void setActivePosts(Set<String> activePosts) {
		this.activePosts = activePosts;
	}
	
	
}
