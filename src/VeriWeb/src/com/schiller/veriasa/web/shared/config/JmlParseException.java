package com.schiller.veriasa.web.shared.config;

/**
 * Generic checked exception thrown when a JML clause cannot be parsed
 * @author Todd Schiller
 */
public class JmlParseException extends Exception{
	private static final long serialVersionUID = 1L;
	
	@SuppressWarnings("unused")
	private JmlParseException(){
		super();
	}
	
	public JmlParseException(Throwable t){
		super(t);
	}
}

