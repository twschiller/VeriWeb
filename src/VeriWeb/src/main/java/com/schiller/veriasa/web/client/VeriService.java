package com.schiller.veriasa.web.client;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.schiller.veriasa.web.client.views.ClauseWriter;
import com.schiller.veriasa.web.shared.config.FeedbackException;
import com.schiller.veriasa.web.shared.config.JmlParseException;
import com.schiller.veriasa.web.shared.config.NoSuchProjectException;
import com.schiller.veriasa.web.shared.config.UnknownServerException;
import com.schiller.veriasa.web.shared.dnd.InvElement;
import com.schiller.veriasa.web.shared.feedback.Feedback;
import com.schiller.veriasa.web.shared.messaging.UserMessage;
import com.schiller.veriasa.web.shared.messaging.UserMessage.Vote;
import com.schiller.veriasa.web.shared.messaging.UserMessageThread;
import com.schiller.veriasa.web.shared.parsejml.JmlSpan;
import com.schiller.veriasa.web.shared.problems.Documentation;
import com.schiller.veriasa.web.shared.problems.MaybeProblem;
import com.schiller.veriasa.web.shared.solutions.Solution;
import com.schiller.veriasa.web.shared.update.Update;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("veri")
public interface VeriService extends RemoteService {
	MaybeProblem requestProblem();
	
	MaybeProblem requestProblem(Solution solution) throws UnknownServerException;
	
	MaybeProblem initSession(String service, String project, String workerId, String assignmentId, String sharedInstanceKey, boolean share) throws NoSuchProjectException;
	
	String requestAssignmentId();
	
	Documentation requestDoc(int id);
	Documentation requestDoc(int id, Integer warningId);
	String requestWarning(int id);
	
	List<InvElement> requestLocals();
	List<InvElement> requestFields(String exp);
	
	InvElement stringToElt(String s) throws JmlParseException;
	
	List<String> requestParams();
	
	List<String> requestSignatures();
	
	List<UserMessageThread> requestMessages();
	
	boolean submitVote(UserMessageThread thread, UserMessage message, Vote vote, UserMessage response );
	
	Feedback requestFeedback(Update update) throws FeedbackException;
	List<Feedback> requestFeedbacks(List<Update> update) throws FeedbackException;
	
	boolean writeModeChanged(ClauseWriter.WriteMode mode);

	boolean stayAlive();
	
	JmlSpan specToSpan(String s) throws JmlParseException;
	
}
