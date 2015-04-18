package com.schiller.veriasa.web.shared.config;

/**
 * A user's web session is corrupted
 * @author Todd Schiller
 */
public class BadSessionException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public BadSessionException() {
		super();
	}

	public BadSessionException(String message, Throwable cause) {
		super(message, cause);
	}

	public BadSessionException(String message) {
		super(message);
	}

	public BadSessionException(Throwable cause) {
		super(cause);
	}
}
