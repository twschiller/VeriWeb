package com.schiller.veriasa.web.shared.problems;

import com.schiller.veriasa.web.shared.core.MethodContract;
import com.schiller.veriasa.web.shared.core.HasQualifiedSignature;

public abstract class MethodProblem extends Problem implements HasQualifiedSignature{
	private static final long serialVersionUID = 1L;
	
	private MethodContract method;
	private String annotatedBody;
	
	@SuppressWarnings("unused")
	private MethodProblem(){
		
	}
	
	public MethodProblem(MethodContract method, String annotatedBody){
		this.method = method;
		this.annotatedBody = annotatedBody;
	}

	public MethodContract getFunction() {
		return method;
	}

	public String getAnnotatedBody() {
		return annotatedBody;
	}

	public void setAnnotatedBody(String annotatedBody) {
		this.annotatedBody = annotatedBody;
	}

	@Override
	public String qualifiedSignature(){
		return method.qualifiedSignature();
	}
}
