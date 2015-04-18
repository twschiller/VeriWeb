package com.schiller.veriasa.web.shared.config;

/**
 * Generic check exception for VeriWeb server errors
 * @author Todd Schiller
 */
public class UnknownServerException extends Exception{
	private static final long serialVersionUID = 1L;

	public UnknownServerException() {
		super();
	}

	public UnknownServerException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnknownServerException(String message) {
		super(message);
	}

	public UnknownServerException(Throwable cause) {
		super(cause);
	}
}
