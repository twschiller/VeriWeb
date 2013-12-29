package com.veriasa.speceditor.shared;

import java.io.Serializable;
import java.util.List;



@SuppressWarnings("serial")
public class FunctionDoc implements Serializable{

	private String summary;
	private String remarks;
	private List<ParamDoc>  params;
	private String returnValue;

	
	@SuppressWarnings("unused")
	private FunctionDoc(){
	}
	
	
	public FunctionDoc(String summary, String remarks, List<ParamDoc> params, String returnValue ){
		this.summary = summary;
		this.remarks = remarks;
		this.params = params;
		this.returnValue = returnValue;
	}

	public String getSummary() {
		return summary;
	}
	public String getRemarks() {
		return remarks;
	}
	public List<ParamDoc> getParams() {
		return params;
	}
	public String getReturnValue() {
		return returnValue;
	}
}
