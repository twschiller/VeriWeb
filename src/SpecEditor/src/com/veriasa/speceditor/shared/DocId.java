package com.veriasa.speceditor.shared;

import java.io.Serializable;

/**
 * A unique identifier for a peice of documentation
 * @author tschiller
 */
@SuppressWarnings("serial")
public class DocId implements Serializable{

	private final String name;
	
	public DocId(String name){
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
