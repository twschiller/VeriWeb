package com.schiller.veriasa.web.shared.util;

import com.google.gwt.user.client.rpc.AsyncCallback;

public abstract class CallbackUtils {

	/**
	 * A callback that ignores all results, including exceptions
	 * @author Todd Schiller
	 * @param <T> return type of the callback
	 */
	public static class Ignore<T> implements AsyncCallback<T>{
		@Override
		public void onFailure(Throwable caught) {
			// ignore
		}

		@Override
		public void onSuccess(T result) {
			// ignore
		}
	}
	
	/**
	 * Generate a callback that ignores all results, including exceptions
	 * @return a callback that ignores all results, including exceptions
	 */
	public static <T> AsyncCallback<T> ignore(){
		return new Ignore<T>();
	}
	
}
