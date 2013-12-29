package com.schiller.veriasa.web.shared.messaging;

import java.io.Serializable;

/**
 * A message from one user to another concerning a method.
 * @author Todd Schiller
 */
public class UserMessage implements Serializable{

	private static final long serialVersionUID = 1L;

	public static enum Vote { NO_VOTE, GOOD, BAD}
	
	private long userId;
	private String sourceMethod;
	private String sourceSpec;
	private String comment;
	private Vote vote;
	
	@SuppressWarnings("unused")
	private UserMessage(){
		
	}
	
	public UserMessage(long userId, String sourceMethod, String sourceSpec, String comment, Vote vote) {
		super();
		this.userId = userId;
		this.sourceSpec = sourceSpec;
		this.sourceMethod = sourceMethod;
		this.comment = comment;
		this.vote = vote;
	}

	public long getUserId() {
		return userId;
	}
	
	public String getSourceSpec(){
		return sourceSpec;
	}
	public String getSourceMethod() {
		return sourceMethod;
	}
	public String getComment() {
		return comment;
	}

	public Vote getVote() {
		return vote;
	}
	
	public void setVote(Vote vote){
		this.vote = vote;
	}
	
	@Override
	public String toString() {
		return "MSG [author=" + userId + ", src="
				+ sourceMethod + " " + comment.replaceAll("\n", " ") + "]";
	}
	
}
