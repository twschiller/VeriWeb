package com.schiller.veriasa.web.shared.problems;

import java.io.Serializable;

/**
 * Documentation
 * @author Todd Schiller
 */
public class Documentation implements Serializable{
	private static final long serialVersionUID = 1L;

	private String docHtml;

	@SuppressWarnings("unused")
	private Documentation(){
		docHtml = "";
	}
	
	/**
	 * @param docHtml HTML-formatted documentation
	 */
	public Documentation(String docHtml){
		this.docHtml = docHtml;
	}

	/**
	 * @return the documentation in HTML format
	 */
	public String getDocHtml() {
		return docHtml;
	}
}
