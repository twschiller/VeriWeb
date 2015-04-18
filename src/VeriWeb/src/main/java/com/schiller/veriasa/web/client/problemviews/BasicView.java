package com.schiller.veriasa.web.client.problemviews;

import java.util.List;


import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.schiller.veriasa.web.client.VeriService;
import com.schiller.veriasa.web.client.VeriServiceAsync;
import com.schiller.veriasa.web.client.views.CodeView;
import com.schiller.veriasa.web.client.views.InformationView;
import com.schiller.veriasa.web.client.views.InformationView.Display;
import com.schiller.veriasa.web.client.views.MessageAlertDialog;
import com.schiller.veriasa.web.client.views.MessageCenter;
import com.schiller.veriasa.web.client.views.MessageCenter.VoteHandler;
import com.schiller.veriasa.web.client.views.mturk.MTurkFeedbackForm;
import com.schiller.veriasa.web.client.views.SimpleResizePanel;
import com.schiller.veriasa.web.client.views.ClauseList;
import com.schiller.veriasa.web.shared.core.SourceElement.LanguageType;
import com.schiller.veriasa.web.shared.messaging.UserMessage;
import com.schiller.veriasa.web.shared.messaging.UserMessage.Vote;
import com.schiller.veriasa.web.shared.messaging.UserMessageThread;
import com.schiller.veriasa.web.shared.mturk.MTurkProgress;

public class BasicView extends Composite {

	private final VeriServiceAsync veriService = GWT.create(VeriService.class);
	
	private static BasicViewUiBinder uiBinder = GWT.create(BasicViewUiBinder.class);

	interface BasicViewUiBinder extends UiBinder<Widget, BasicView> {
	}

	@UiField 
	protected SplitLayoutPanel layout;
	
	@UiField
	protected SimpleResizePanel main;
	
	@UiField
	protected InformationView info;
	
	@UiField
	protected CodeView codeView;
	
	@UiField
	protected Label showInstr;
	
	@UiField
	protected Label showMsgs;
	
	@UiField
	protected SimplePanel infoBar;
	
	@UiField
	protected Label mturkEarned;
	
	@UiField
	protected Label mturkSubmit;
	
	@UiField
	protected HorizontalPanel barPanel;
	
	@UiField
	protected ClauseList assumedPreconditions;
	
	@UiField
	protected Label assumedPreconditionsHeader;
	
	protected MessageCenter messageView;
	
	private IView view;
	
	public BasicView() {
		initWidget(uiBinder.createAndBindUi(this));
		layout.setWidgetMinSize(main, 400);	
	}
	
	public void setMain(final IView view, long userId){
		main.setWidget(view.asWidget());
		
		messageView = new MessageCenter(userId);
		
		veriService.requestMessages(new AsyncCallback<List<UserMessageThread>>(){
			@Override
			public void onFailure(Throwable caught) {
			}
			@Override
			public void onSuccess(List<UserMessageThread> result) {
				setMessages(result);
			}
		});
		
		messageView.setVoteHandler(new VoteHandler(){
			@Override
			public void recordVote(UserMessageThread thread, UserMessage message, Vote vote, UserMessage response) {
				view.getVoteHandler().recordVote(thread, message, vote, response);
				
				veriService.requestMessages(new AsyncCallback<List<UserMessageThread>>(){
					@Override
					public void onFailure(Throwable caught) {
					}
					@Override
					public void onSuccess(List<UserMessageThread> result) {
						setMessages(result);
					}
				});
			}
		});
		
		info.setHandler(view.getDocHandler());
		info.show(view.getInstructions());
		codeView.setDocPanel(info);
		
		showInstr.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				info.show(view.getInstructions());
			}
		});
		
		showMsgs.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				info.show(messageView);
			}
		});
		
		if (view.getAssumedRequires() == null){
			assumedPreconditions.setVisible(false);
			assumedPreconditionsHeader.setVisible(false);
		}else{
			assumedPreconditionsHeader.setVisible(true);
			
			if (view.getAssumedRequires().isEmpty()){
				assumedPreconditionsHeader.setText("No Known Preconditions");
			}else{
				assumedPreconditionsHeader.setText("Known Preconditions:");
			}
			
			assumedPreconditions.setValues(view.getAssumedRequires());
			assumedPreconditions.setVisible(!view.getAssumedRequires().isEmpty());
		}
			
		view.setInformationView(info);
		
		this.view = view;
	}

	public void showSubmitForm(MTurkProgress progress){
		final PopupPanel sp = new PopupPanel();
		
		String hd = "HIT Feedback";
		
		String msg = "You have earned $" + (progress.getNumSolved() * progress.getRate()) 
			+ ". Complete the following survey to submit the HIT; a BONUS of " +
			"$" + progress.getRate() + " will be awarded for answering all of the questions.";
		
		String names [] = new String [] { "previewSolved", "numSolved", "rate", "earnings" };
		String values [] = new String [] { String.valueOf(progress.getNumPreviewSolved()), 
				String.valueOf(progress.getNumSolved()),
				String.valueOf(progress.getRate()),
				String.valueOf(progress.getEarned())};
		
		
		MTurkFeedbackForm f = new MTurkFeedbackForm(hd, msg, names, values, new MTurkFeedbackForm.CancelCallback() {
			@Override
			public void onCancel() {
				sp.setVisible(false);
				mturkSubmit.setVisible(true);	
			}
		});
		
		sp.add(f);
		
		sp.center();
		sp.show();
	}
	
	/**
	 * Update the source code displayed in the code view
	 * @param src the source code to display
	 * @param language the source codes language (for highlighting)
	 * @param highlight true iff the code view should perform syntax highlighting
	 */
	public void updateCode(String src, LanguageType language, boolean highlight){
		codeView.setCode(src, language, highlight);
	}
	
	/**
	 * Update the messages displayed in the message center; update the message
	 * notification in the notification bar accordingly.
	 * @param threads the message threads to display
	 */
	public void setMessages(List<UserMessageThread> threads){
		if (threads.isEmpty()){
			showMsgs.setVisible(false);
		}else{
			messageView.setMessages(threads);
			
			new MessageAlertDialog(new MessageAlertDialog.GoToCallback() {
				@Override
				public void go() {
					info.show(messageView);
				}
			}).center();
			
			showMsgs.setText("There are messages");
			showMsgs.addStyleName("info-bar-linkbb");
			showMsgs.setVisible(true);
		}
	}
	
	/**
	 * If the warnings are currently being displayed, show the instructions
	 * instead.
	 */
	public void flushWarnings(){
		if (info.getDisplayType() == Display.WARNINGS){
			info.show(view.getInstructions());
		}
	}
}
