package com.schiller.veriasa.web.client;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.schiller.veriasa.web.client.views.ClauseWriter;
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
 * The async counterpart of <code>GreetingService</code>.
 */
public interface VeriServiceAsync {
	void initSession(String service, String project, String workerId, String assignmentId, String sharedInstanceKey, boolean share, AsyncCallback<MaybeProblem> callback );
	
	void requestProblem(AsyncCallback<MaybeProblem> callback );
	
	void requestProblem(Solution solution, AsyncCallback<MaybeProblem> callback );
	
	void requestDoc(int id, AsyncCallback<Documentation> callback);
	void requestDoc(int id, Integer warningId, AsyncCallback<Documentation> callback);
	void requestWarning(int id, AsyncCallback<String> callback);
	
	void requestParams(AsyncCallback<List<String>> callback);
	
	void requestSignatures(AsyncCallback<List<String>> callback);
	void requestMessages(AsyncCallback<List<UserMessageThread>> callback);

	void submitVote(UserMessageThread thread, UserMessage message, Vote vote, UserMessage response, AsyncCallback<Boolean> callback );
	
	void requestLocals(AsyncCallback<List<InvElement>> callback);
	void requestFields(String exp, AsyncCallback<List<InvElement>> callback);
	
	void requestFeedbacks(List<Update> update, AsyncCallback<List<Feedback>> callback);
	void requestFeedback(Update solution, AsyncCallback<Feedback> callback);
	
	void requestAssignmentId(AsyncCallback<String> callback);
	
	void stringToElt(String spec, AsyncCallback<InvElement> callback);
	
	void stayAlive(AsyncCallback<Boolean> callback);
	
	void writeModeChanged(ClauseWriter.WriteMode mode, AsyncCallback<Boolean> callback);

	void specToSpan(String s, AsyncCallback<JmlSpan> callback);
}
