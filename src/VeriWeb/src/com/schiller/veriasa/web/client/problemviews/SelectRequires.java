package com.schiller.veriasa.web.client.problemviews;

import java.util.LinkedList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.schiller.veriasa.web.client.SubProblemCallback;
import com.schiller.veriasa.web.client.SubProblemCallback.UpdateResponseHandler;
import com.schiller.veriasa.web.client.views.Instructions;
import com.schiller.veriasa.web.client.views.InformationView;
import com.schiller.veriasa.web.client.views.InformationView.DocumentationHandler;
import com.schiller.veriasa.web.client.views.IssueView;
import com.schiller.veriasa.web.client.views.IssueView.DialogHandler;
import com.schiller.veriasa.web.client.views.MessageCenter.VoteHandler;
import com.schiller.veriasa.web.client.views.ClauseSelector;
import com.schiller.veriasa.web.client.views.ClauseSelector.SelectorEventHandler;
import com.schiller.veriasa.web.shared.core.SourceElement;
import com.schiller.veriasa.web.shared.core.Clause;
import com.schiller.veriasa.web.shared.feedback.SelectRequiresFeedback;
import com.schiller.veriasa.web.shared.messaging.UserMessage;
import com.schiller.veriasa.web.shared.messaging.UserMessage.Vote;
import com.schiller.veriasa.web.shared.messaging.UserMessageThread;
import com.schiller.veriasa.web.shared.problems.SelectRequiresProblem;
import com.schiller.veriasa.web.shared.solutions.ImpossibleInfo;
import com.schiller.veriasa.web.shared.solutions.ImpossibleInfo.Reason;
import com.schiller.veriasa.web.shared.solutions.SelectRequiresSolution;
import com.schiller.veriasa.web.shared.update.SelectRequiresUpdate;

public class SelectRequires extends Composite implements IView{

	private final static boolean HIGHLIGHT = false;
	
	private static SelectRequiresUiBinder uiBinder = GWT
			.create(SelectRequiresUiBinder.class);

	interface SelectRequiresUiBinder extends UiBinder<Widget, SelectRequires> {
	}

	private final PopupPanel notCompleted = new PopupPanel();
	
	private SubProblemCallback<SelectRequiresSolution, SelectRequiresUpdate, SelectRequiresFeedback> callback;
	
	private boolean sufficient = false;
	private boolean thinking = false;
	private List<Clause> tryNext = null;
	
	@UiField 
	Button notListed;
	
	@UiField 
	Button doneButton;
	
	@UiField
	HTMLPanel loadingBar;

	@UiField
	ClauseSelector specs;
	
	@UiField
	Label bottomAnchor;
	
	private SourceElement methodInfo;
	
	private String currentMethod;
	
	private void startThinking(){
		thinking = true;
		loadingBar.setVisible(true);
		doneButton.setEnabled(false);
		notListed.setEnabled(false);
	}
	private void endThinking(){
		thinking = false;
		loadingBar.setVisible(false);
		notListed.setEnabled(!sufficient);
		doneButton.setEnabled(sufficient);
	}
	private void pauseInput(){
		specs.setCanClick(false);
		notListed.setEnabled(false);
	}
	private void resumeInput(){
		notListed.setEnabled(true);
		specs.setCanClick(true);
		
	}
	
	private void doTry(){
		startThinking();
		
		callback.onUpdate(
				new SelectRequiresUpdate(tryNext), 
				new UpdateResponseHandler<SelectRequiresFeedback>(){

					@Override
					public void onUpdateResponse(SelectRequiresFeedback response) {
						callback.updateCode(response.getAnnotatedBody(), 
								methodInfo.getSrcLanguage(),
								HIGHLIGHT);
						
						sufficient = response.isSufficient();
						
						if (tryNext != null){
							doTry();
						}else{
							endThinking();	
						}
					}

					@Override
					public void onUpdateResponse(
							List<SelectRequiresFeedback> responses) {
						throw new RuntimeException();
					}
		});
		tryNext = null;
	}
	
	
	public SelectRequires(SelectRequiresProblem problem, 
			SubProblemCallback<SelectRequiresSolution,SelectRequiresUpdate,SelectRequiresFeedback> callback) {
		
		initWidget(uiBinder.createAndBindUi(this));
		
		this.currentMethod = problem.getFunction().getSignature();
		this.callback = callback;
		
		loadingBar.setVisible(false);
		
		sufficient = problem.isSufficient();
	
		notListed.setEnabled(!sufficient);
		doneButton.setEnabled(sufficient);
		
		methodInfo = problem.getFunction().getInfo();
		
		callback.updateCode(problem.getAnnotatedBody(), problem.getFunction().getInfo().getSrcLanguage(), HIGHLIGHT);
	
		specs.setValues(problem.getChoices(), problem.getActive());
		specs.setSelectorEventHandler(new SelectorEventHandler(){
			@Override
			public void activeSetChanged(List<Clause> active) {
				tryNext = new LinkedList<Clause>(active);
				
				if (!thinking){
					doTry();
				}	
			}
		});
	}

	void showIssueDialog(final String method, final Reason reason){
		showIssueDialog(method, reason,null);
	}
	
	void showIssueDialog(final String method, final Reason reason, final Clause spec){
		pauseInput();
		
		notCompleted.clear();
		IssueView form = new IssueView(reason);	
		notCompleted.add(form);
		
		form.setHandler(new DialogHandler(){
			@Override
			public void onOk(SafeHtml comment) {
				callback.onFinish(new SelectRequiresSolution(specs.getSelected(), 
						new ImpossibleInfo(reason, method, spec, comment.asString())));
				notCompleted.hide();
			}
			@Override
			public void onCancel() {
				notCompleted.hide();
				resumeInput();
			}
		});
		notCompleted.center();
		notCompleted.show();
		form.focus();
	}
	
	
	@UiHandler("doneButton")
	void onDone(ClickEvent e) {
		callback.onFinish(new SelectRequiresSolution(specs.getSelected()));
	}
	
	@UiHandler("notListed")
	void onGiveUp(ClickEvent e) {
		showIssueDialog(currentMethod, Reason.NOT_LISTED);
	}
	
	@Override
	public DocumentationHandler getDocHandler() {
		return new DocumentationHandler(){
			@Override
			public void addEnsures(String method) {
				showIssueDialog(method, Reason.WEAK_ENS);
			}
			@Override
			public void addExsures(String method, String ex) {
				showIssueDialog(method, Reason.WEAK_EXS);
			}
			@Override
			public void removeRequires(String method, Clause spec) {
				showIssueDialog(method, Reason.STRONG_REQ, spec);
			}
			@Override
			public void copyStatement(Clause spec) {
				throw new RuntimeException("Copy not implemented");
			}
			@Override
			public boolean canCopy() {
				return false;
			}
		};
	}
	
	@Override
	public SafeHtml getInstructionHtml() {
		return SafeHtmlUtils.fromTrustedString("Select the expressions that <b>MUST</b> be true for the function to not throw an "+ 
					"unexpected <b>runtime</b> exception such as a <code>NullPointerException</code>.");
	}
	
	@Override
	public Instructions getInstructions() {
		return new Instructions(getInstructionHtml(), false, true);
	}
	
	@Override
	public List<Clause> getAssumedRequires() {
		return null;
	}

	@Override
	public VoteHandler getVoteHandler() {
		return new VoteHandler(){
			@Override
			public void recordVote(UserMessageThread thread, UserMessage message, Vote vote, UserMessage response) {
				SelectRequires.this.callback.onVote(thread, message, vote, response);
			}
		};
	}
	
	@Override
	public void setInformationView(InformationView d) {
		//don't need to do anything
	}
}
