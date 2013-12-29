package com.veriasa.speceditor.shared;

import java.io.Serializable;

@SuppressWarnings("serial")
public class FunctionSig implements Serializable{

	
	private String text;
	private String name;
	private String returnType;
	private String [] args;
	private String [] argTypes;
	
	@SuppressWarnings("unused")
	private FunctionSig(){
		
	}
	
	
	public FunctionSig(String text, String name, String returnType, String [] args, String [] argTypes){
		this.text = text;
		this.name = name;
		this.returnType = returnType;
		this.args = args;
		this.argTypes = argTypes;
	}

	public static String textFromBody(String body){
		return body.substring(0, body.indexOf("{")).trim().replaceAll("\n", " ");
	}
	
	public String getName() {
		return name;
	}

	public String getReturnType() {
		return returnType;
	}

	public String[] getArgs() {
		return args;
	}

	public String[] getArgTypes() {
		return argTypes;
	}
	 
	public String getText(){
		return text;
	}
	
}
