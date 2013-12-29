package com.veriasa.speceditor.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.veriasa.speceditor.shared.FunctionDoc;
import com.veriasa.speceditor.shared.SpecProblem;

/**
 * The async counterpart of <code>GreetingService</code>.
 */
public interface GreetingServiceAsync {
	void greetServer(String input, AsyncCallback<String> callback)
			throws IllegalArgumentException;

	void requestDoc(int id, AsyncCallback<FunctionDoc> callback);

	void requestProblem(AsyncCallback<SpecProblem> callback );
	
}
