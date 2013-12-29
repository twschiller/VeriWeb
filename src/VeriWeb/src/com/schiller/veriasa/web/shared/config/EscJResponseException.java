package com.schiller.veriasa.web.shared.config;

/**
 * Error occurring when reading output from ESC/Java2
 * @author Todd Schiller
 */
public class EscJResponseException extends Exception {

	private static final long serialVersionUID = 1L;

	public EscJResponseException() {
		super();
	}

	public EscJResponseException(String message, Throwable cause) {
		super(message, cause);
	}

	public EscJResponseException(String message) {
		super(message);
	}

	public EscJResponseException(Throwable cause) {
		super(cause);
	}
}
