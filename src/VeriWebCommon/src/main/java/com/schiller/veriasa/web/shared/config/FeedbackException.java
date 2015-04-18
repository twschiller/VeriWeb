package com.schiller.veriasa.web.shared.config;

/**
 * Generic exception for errors occuring when generating {@link Feedback} for
 * a user {@link Update}
 * @author Todd Schiller
 */
public class FeedbackException extends Exception{
	private static final long serialVersionUID = 1L;

	public FeedbackException() {
		super();
	}

	public FeedbackException(String message, Throwable cause) {
		super(message, cause);
	}

	public FeedbackException(String message) {
		super(message);
	}

	public FeedbackException(Throwable cause) {
		super(cause);
	}
}
