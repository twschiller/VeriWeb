package com.schiller.veriasa.web.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.schiller.veriasa.web.client.problemviews.BasicView;
import com.schiller.veriasa.web.client.problemviews.SelectRequires;
import com.schiller.veriasa.web.client.problemviews.WriteEnsures;
import com.schiller.veriasa.web.client.problemviews.WriteExsures;
import com.schiller.veriasa.web.client.problemviews.WriteRequires;
import com.schiller.veriasa.web.client.views.LoadingPage;
import com.schiller.veriasa.web.client.views.NotActiveView;
import com.schiller.veriasa.web.client.views.ServerErrorView;
import com.schiller.veriasa.web.client.views.mturk.MTurkKeyView;
import com.schiller.veriasa.web.shared.config.SharedConfig;
import com.schiller.veriasa.web.shared.core.SourceElement.LanguageType;
import com.schiller.veriasa.web.shared.feedback.Feedback;
import com.schiller.veriasa.web.shared.feedback.SelectRequiresFeedback;
import com.schiller.veriasa.web.shared.feedback.WriteEnsuresFeedback;
import com.schiller.veriasa.web.shared.feedback.WriteExsuresFeedback;
import com.schiller.veriasa.web.shared.feedback.WriteRequiresFeedback;
import com.schiller.veriasa.web.shared.messaging.UserMessage;
import com.schiller.veriasa.web.shared.messaging.UserMessage.Vote;
import com.schiller.veriasa.web.shared.messaging.UserMessageThread;
import com.schiller.veriasa.web.shared.problems.MaybeProblem;
import com.schiller.veriasa.web.shared.problems.NoProblemInfo;
import com.schiller.veriasa.web.shared.problems.Problem;
import com.schiller.veriasa.web.shared.problems.ProjectFinished;
import com.schiller.veriasa.web.shared.problems.SelectRequiresProblem;
import com.schiller.veriasa.web.shared.problems.WriteEnsuresProblem;
import com.schiller.veriasa.web.shared.problems.WriteExsuresProblem;
import com.schiller.veriasa.web.shared.problems.WriteRequiresProblem;
import com.schiller.veriasa.web.shared.solutions.SelectRequiresSolution;
import com.schiller.veriasa.web.shared.solutions.Solution;
import com.schiller.veriasa.web.shared.solutions.WriteEnsuresSolution;
import com.schiller.veriasa.web.shared.solutions.WriteExsuresSolution;
import com.schiller.veriasa.web.shared.solutions.WriteRequiresSolution;
import com.schiller.veriasa.web.shared.update.SelectRequiresUpdate;
import com.schiller.veriasa.web.shared.update.Update;
import com.schiller.veriasa.web.shared.update.WriteEnsuresUpdate;
import com.schiller.veriasa.web.shared.update.WriteExsuresUpdate;
import com.schiller.veriasa.web.shared.update.WriteRequiresUpdate;
import com.schiller.veriasa.web.shared.util.CallbackUtils;

/**
 * Main VeriWeb entry point
 * @author Todd Schiller
 */
public class VeriWeb implements EntryPoint {
	// TODO re-add MTurk functionality
	
	/**
	 * Height of the documentation / warning box (in pixels)
	 */
	public static final int DOC_HEIGHT = 400;
	
	/**
	 * Width of the documentation / warning box (in pixels)
	 */
	public static final int DOC_WIDTH = 350;
	
	public static boolean DEBUG_MODE = false;
	
	/**
	 * Create a remote service proxy to talk to the server-side Greeting service.
	 */
	private final VeriServiceAsync veriService = GWT.create(VeriService.class);

	private final LoadingPage loadingPage = new LoadingPage();

	private static final AsyncCallback<Boolean> voteCallback = CallbackUtils.ignore();
	
	/**
	 * Clear the root panel and show <code>view</code>
	 * @param view the view to show
	 */
	private void show(Composite view){
		RootLayoutPanel.get().clear();
		RootLayoutPanel.get().add(view);
	}
	
	private void showErrorPage(){
		show(new ServerErrorView());
	}
	
	private void showInactivePage(){
		show(new NotActiveView());
	}
	
	public void showFinished(){
		// TODO show the proper result page	
		show(new MTurkKeyView("12345"));
	}
	
	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		initSession();
	}
	
	/**
	 * Communicate with the server every {@link SharedConfig#STAY_ALIVE_INTERVAL} seconds
	 * to free problems when users disconnect
	 */
	public void startStayAlive(){
		Timer t =  new Timer(){
			public void run(){
				veriService.stayAlive(CallbackUtils.<Boolean>ignore());
			}
		};
		t.scheduleRepeating(SharedConfig.STAY_ALIVE_INTERVAL);
	}
	
	public void initSession(){
		RootLayoutPanel.get().clear();
		RootLayoutPanel.get().add(loadingPage);
		
		final String service = com.google.gwt.user.client.Window.Location.getParameter("srv");
		final String project = com.google.gwt.user.client.Window.Location.getParameter("proj");
		final String webId = com.google.gwt.user.client.Window.Location.getParameter("id");
		
		DEBUG_MODE = com.google.gwt.user.client.Window.Location.getParameter("debug") != null;
		
		//debugPrint("Debugging Enabled");
		
		final String mturkId = com.google.gwt.user.client.Window.Location.getParameter("workerId");
		final String assignmentId = com.google.gwt.user.client.Window.Location.getParameter("assignmentId");
		String sharedInstanceKey = com.google.gwt.user.client.Window.Location.getParameter("shareKey");
		
		if (sharedInstanceKey == null || sharedInstanceKey.equalsIgnoreCase("")){
			sharedInstanceKey = null;
		}
		
		final boolean share = Boolean.valueOf(com.google.gwt.user.client.Window.Location.getParameter("shared"));
		
		if (webId != null && mturkId != null){
			Window.alert("Ambiguous worker id");
			showErrorPage();
			return;
		}else if (mturkId != null && !service.equalsIgnoreCase("MTURK")){
			Window.alert("Unexpected Mechanical Turk worker id");
			showErrorPage();
			return;
		}
		
		loadingPage.randomTip();
		show(loadingPage);
		RootLayoutPanel.get().clear();
		
		veriService.initSession(service,project,webId == null ? mturkId : webId, assignmentId, sharedInstanceKey, share,  new AsyncCallback<MaybeProblem>(){
			@Override
			public void onFailure(Throwable caught) {
				showErrorPage();
			}
			@Override
			public void onSuccess(final MaybeProblem result) {
				if (result.hasProblem()){
					startStayAlive();
					handleProblem(result.getUserId(), result.getProblem());
				}else{
					handleInfo(result.getInfo());
				}
			}
		});
	}
	
	public void handleInfo(NoProblemInfo info){
		if (info instanceof ProjectFinished){
			showFinished();
		}else{
			showInactivePage();
		}
	}
		
	public void requestProblem(){
		loadingPage.randomTip();
		show(loadingPage);
		
		veriService.requestProblem(new AsyncCallback<MaybeProblem>(){
			@Override
			public void onFailure(Throwable caught) {
				showErrorPage();
			}
			@Override
			public void onSuccess(MaybeProblem result) {
				if (result.hasProblem()){
					handleProblem(result.getUserId(), result.getProblem());
				}else{
					handleInfo(result.getInfo());
				}
			}
		});
	}
	
	public void requestProblem(Solution solution){
		loadingPage.randomTip();
		show(loadingPage);
		
		veriService.requestProblem(solution, new AsyncCallback<MaybeProblem>(){
			@Override
			public void onFailure(Throwable caught) {
				showErrorPage();
			}
			@Override
			public void onSuccess(MaybeProblem result) {
				if (result.hasProblem()){
					handleProblem(result.getUserId(), result.getProblem());
				}else{
					handleInfo(result.getInfo());
				}
			}
		});
	}
	
	private void handleProblem(long userId, Problem problem){
		RootLayoutPanel.get().clear();
		final BasicView view = new BasicView();
		
		if (problem instanceof SelectRequiresProblem){
			SelectRequiresProblem p = (SelectRequiresProblem) problem;
			view.setMain(new SelectRequires(p, new DefaultCallback<SelectRequiresSolution, SelectRequiresUpdate,SelectRequiresFeedback>(view)), userId);
		}else if (problem instanceof WriteRequiresProblem){
			WriteRequiresProblem p = (WriteRequiresProblem) problem;
			view.setMain(new WriteRequires(p, new DefaultCallback<WriteRequiresSolution, WriteRequiresUpdate,WriteRequiresFeedback>(view)), userId);
		}else if (problem instanceof WriteExsuresProblem){
			WriteExsuresProblem p = (WriteExsuresProblem) problem;
			view.setMain(new WriteExsures(p, new ParallelCallback<WriteExsuresSolution, WriteExsuresUpdate, WriteExsuresFeedback>(view)), userId);
		}else if (problem instanceof WriteEnsuresProblem){
			WriteEnsuresProblem p = (WriteEnsuresProblem) problem;
			view.setMain(new WriteEnsures(p, new ParallelCallback<WriteEnsuresSolution, WriteEnsuresUpdate, WriteEnsuresFeedback>(view)), userId);
		}else{
			showErrorPage();
			return;
		}
		RootLayoutPanel.get().add(view);
	}
	
	/**
	 * Basic callback without support for parallel feedback
	 * @author Todd Schiller
	 * @param <S> solution type
	 * @param <U> update type
	 * @param <F> feedback type
	 */
	private class DefaultCallback<S extends Solution,U extends Update, F extends Feedback> implements SubProblemCallback<S,U,F>{
		protected final BasicView view;
		
		private DefaultCallback(BasicView view){
			this.view = view;
		}
		
		@Override
		public void onUpdate(U update, final UpdateResponseHandler<F> responseHandler) {
			veriService.requestFeedback(update, new AsyncCallback<Feedback>(){
				@Override
				public void onFailure(Throwable caught) {
					showErrorPage();
				}
				@SuppressWarnings("unchecked")
				@Override
				public void onSuccess(Feedback result) {
					responseHandler.onUpdateResponse((F) result);	
					DefaultCallback.this.view.flushWarnings();
				}
			});
		}
		@Override
		public void onFinish(S solution) {
			requestProblem(solution);
		}
		@Override
		public void onVote(UserMessageThread thread, UserMessage message, Vote vote, UserMessage response) {
			veriService.submitVote(thread, message, vote, response, voteCallback);
		}
		@Override
		public void updateCode(String src, LanguageType lang, boolean highlight) {
			view.updateCode(src, lang, highlight);
		}
		@Override
		public void onUpdate(List<U> update, UpdateResponseHandler<F> responseHandler) {
			throw new RuntimeException("Parallel feedback not enabled");
		}
	}
	
	/**
	 * Basic callback without support for parallel feedback
	 * @author Todd Schiller
	 * @param <S> solution type
	 * @param <U> update type
	 * @param <F> feedback type
	 */
	private class ParallelCallback<S extends Solution,U extends Update, F extends Feedback> extends DefaultCallback<S,U,F>{
		ParallelCallback(BasicView view) {
			super(view);
		}
		
		@Override
		public void onUpdate(List<U> update, final UpdateResponseHandler<F> responseHandler) {
			veriService.requestFeedbacks(new ArrayList<Update>(update), new AsyncCallback<List<Feedback>>(){
				@Override
				public void onFailure(Throwable caught) {
					showErrorPage();
				}
				@SuppressWarnings("unchecked")
				@Override
				public void onSuccess(List<Feedback> result) {
					responseHandler.onUpdateResponse((List<F>) result);
					ParallelCallback.this.view.flushWarnings();
				}
			});
		}
	}
}
