package com.schiller.veriasa.distance.util;

/**
 * Indicates an invalid JML specification, i.e., the specification cannot be parsed
 * @author Todd Schiller
 */
public class InvalidSpecification extends Exception {
	private static final long serialVersionUID = 1L;

	public InvalidSpecification(Throwable cause){
		super(cause);
	}	
}
