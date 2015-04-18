package com.schiller.veriasa.web.client;

import java.util.List;

import com.schiller.veriasa.web.shared.core.SourceElement.LanguageType;
import com.schiller.veriasa.web.shared.feedback.Feedback;
import com.schiller.veriasa.web.shared.messaging.UserMessage;
import com.schiller.veriasa.web.shared.messaging.UserMessageThread;
import com.schiller.veriasa.web.shared.messaging.UserMessage.Vote;
import com.schiller.veriasa.web.shared.solutions.Solution;
import com.schiller.veriasa.web.shared.update.Update;

/**
 * Callbacks for VeriWeb client / server communication
 * @author Todd Schiller
 * @param <S> the problem solution type
 * @param <U> the client update type
 * @param <F> the server feedback type
 */
public interface SubProblemCallback<S extends Solution,U extends Update, F extends Feedback>{
	
	public interface UpdateResponseHandler<F extends Feedback>{
		public void onUpdateResponse(F response);
		public void onUpdateResponse(List<F> responses);
	}
	
	public void onUpdate(U update, UpdateResponseHandler<F> responseHandler);
	public void onUpdate(List<U> update, UpdateResponseHandler<F> responseHandler);
	public void onFinish(S solution);
	public void onVote(UserMessageThread thread, UserMessage message, Vote vote, UserMessage response);
	public void updateCode(String src, LanguageType lang, boolean highlight);
}
