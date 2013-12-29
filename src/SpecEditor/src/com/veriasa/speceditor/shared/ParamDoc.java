package com.veriasa.speceditor.shared;

import java.io.Serializable;

@SuppressWarnings("serial")
public class ParamDoc implements Serializable{

	private String name;
	private String doc;
	
	@SuppressWarnings("unused")
	private ParamDoc(){
	}
	
	public ParamDoc(String name, String doc){
		this.name = name;
		this.doc = doc;
		
	}

	public String getName() {
		return name;
	}

	public String getDoc() {
		return doc;
	}
	
}
