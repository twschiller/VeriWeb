package com.veriasa.speceditor.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.veriasa.speceditor.shared.FunctionDoc;
import com.veriasa.speceditor.shared.SpecProblem;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("greet")
public interface GreetingService extends RemoteService {
	String greetServer(String name) throws IllegalArgumentException;

	FunctionDoc requestDoc(int id);
	SpecProblem requestProblem();
}
