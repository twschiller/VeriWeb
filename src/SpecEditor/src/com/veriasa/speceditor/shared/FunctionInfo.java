package com.veriasa.speceditor.shared;

import java.io.Serializable;

@SuppressWarnings("serial")
public final class FunctionInfo implements Serializable{

	private FunctionSig sig;
	private FunctionDoc doc;
	private String body;
	
	@SuppressWarnings("unused")
	private FunctionInfo(){
		
	}
	
	public FunctionInfo(FunctionSig sig, FunctionDoc doc, String body ){
		this.sig = sig;
		this.doc = doc;
		this.body = body;
	}

	public FunctionDoc getDoc() {
		return doc;
	}

	public FunctionSig getSig(){
		return sig;
	}

	public String getBody(){
		return body;
	}
}
