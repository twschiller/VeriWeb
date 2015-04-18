package com.schiller.veriasa.web.shared.messaging;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.schiller.veriasa.web.shared.messaging.UserMessage.Vote;

/**
 * A thread of user messages. Each message is a response to the previous message.
 * @author Todd Schiller
 */
public class UserMessageThread implements Serializable {

	private static final long serialVersionUID = 1L;

	private LinkedList<UserMessage> messages = new LinkedList<UserMessage>();
	
	@SuppressWarnings("unused")
	private UserMessageThread(){}
	
	/**
	 * Construct a message thread consisting of a single message <code>first</code>
	 * @param first the message
	 */
	public UserMessageThread(UserMessage first){
		messages.add(first);
	}
	
	/**
	 * returns the first message in the thread
	 * @return the first message in the thread
	 */
	public UserMessage getFirst(){
		return messages.get(0);
	}
	
	/**
	 * add <code>message</code> to the end of the message thread
	 * @param message the message to add
	 * @return the message thread
	 */
	public UserMessageThread add(UserMessage message){
		messages.add(message);
		return this;
	}
	
	/**
	 * returns an unmodifiable view of the messages in the thread
	 * @return an unmodifiable view of the messages in the thread
	 */
	public List<UserMessage> getMessages(){
		return Collections.unmodifiableList(messages);
	}
	
	/**
	 * true iff the last message in the thread is <code>Vote.GOOD</code>
	 * @return true iff the last message in the thread is <code>Vote.GOOD</code>
	 */
	public boolean isResolved(){
		return messages.getLast().getVote() == Vote.GOOD;
	}
}
