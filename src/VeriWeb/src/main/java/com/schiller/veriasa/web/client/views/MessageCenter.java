package com.schiller.veriasa.web.client.views;

import java.util.List;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.schiller.veriasa.web.client.views.MessageResponse.DialogHandler;
import com.schiller.veriasa.web.shared.messaging.ImpossibleMessage;
import com.schiller.veriasa.web.shared.messaging.UserMessage;
import com.schiller.veriasa.web.shared.messaging.UserMessage.Vote;
import com.schiller.veriasa.web.shared.messaging.UserMessageThread;

/**
 * Display user messages for a problem
 * @author Todd Schiller
 */
public class MessageCenter extends Composite {

	public static final int MAX_HEIGHT = 150;
	
	public static final String MSGBOX_STYLE = "comment-box";
	public static final String SRC_STYLE = "comment-src";
	public static final String MSG_STYLE = "comment-txt";
	
	public static interface VoteHandler{
		void recordVote(UserMessageThread thread, UserMessage message, Vote vote, UserMessage response);
	}

	private HorizontalPanel root = new HorizontalPanel();
	private VerticalPanel threadView;

	private final long userId;
	
	/**
	 * Popup displayed when user downvotes a message
	 */
	private final PopupPanel responseDialog = new PopupPanel();
	
	/**
	 * http://code.google.com/webtoolkit/doc/latest/DevGuideUiCustomCells.html
	 * @author Todd Schiller
	 */
	private static class ThreadCell extends AbstractCell<UserMessageThread>{
		@Override
		public void render(Context context, UserMessageThread value, SafeHtmlBuilder sb) {
			if (value == null){
				return;
			}
			sb.appendHtmlConstant("<div class=\"thread-name\">");
			
			String s = value.getFirst().getSourceMethod();
			sb.appendEscaped(s.substring(0, s.indexOf('(')));
			sb.appendHtmlConstant("</div>");
		}
	}
	
	/**
	 * List of message threads displayed on left side of message center
	 */
	private CellList<UserMessageThread> threads;
	
	private VoteHandler voteHandler;

	public void setVoteHandler(VoteHandler voteHandler) {
		this.voteHandler = voteHandler;
	}

	public MessageCenter(long userId) {
		this.userId = userId;
		initWidget(root);
		
	}
	
	public void setMessages(List<UserMessageThread> threads){
		if (threads.size() == 1){
			root.clear();
			threadView = new VerticalPanel();
			root.add(threadView);
			showThread(threads.get(0));
		}else if (threads.size() > 1){
			root.clear();
			threadView = new VerticalPanel();
			
			VerticalPanel left = new VerticalPanel();
			left.addStyleName("msg-center-left");
			left.setWidth("150px");
			left.add(new Label("Messages from method:"));
			
			ThreadCell threadCell = new ThreadCell();
			this.threads = new CellList<UserMessageThread>(threadCell);
			this.threads.setWidth("150px");
			
			final SingleSelectionModel<UserMessageThread> selectionModel = new SingleSelectionModel<UserMessageThread>();
			this.threads.setSelectionModel(selectionModel);
			selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
				public void onSelectionChange(SelectionChangeEvent event) {
					showThread(selectionModel.getSelectedObject());
				}
			});
			
			this.threads.setRowData(threads);
			
			left.add(this.threads);
			root.add(left);
			root.add(this.threadView);
			
			selectionModel.setSelected(threads.get(0), true);
		}else{
			throw new IllegalArgumentException("Cannot call setMessages without messages");
		}
	}
	
	/**
	 * Record the vote and invoke the vote callback
	 * @param thread the message thread
	 * @param message the message
	 * @param vote the user's vote
	 */
	private void recordVote(final UserMessageThread thread, final UserMessage message, final Vote vote){
		if (vote == Vote.GOOD){
			message.setVote(vote);
			showThread(thread);
			voteHandler.recordVote(thread, message, vote, null);
		}else if (vote == Vote.BAD){
			responseDialog.clear();
			
			MessageResponse r = new MessageResponse();
			r.setHandler(new DialogHandler(){
				@Override
				public void onOk(SafeHtml response) {
					UserMessage x = new UserMessage(
							userId, 
							message.getSourceMethod(), 
							message.getSourceSpec(), 
							response.asString(), 
							Vote.NO_VOTE);
					
					message.setVote(vote);
					showThread(thread);
					voteHandler.recordVote(thread, message, vote, x);
					responseDialog.hide();
				}
				@Override
				public void onCancel() {
					responseDialog.hide();
				}
			});
			
			responseDialog.add(r);
			responseDialog.center();
			responseDialog.show();
			r.focus();
			
		}else{
			throw new RuntimeException("Unexpected vote " + vote);	
		}
	}
	
	/**
	 * Show a message thread on the right side of the message center
	 * @param thread the message thread
	 */
	private void showThread(final UserMessageThread thread){
		threadView.clear();
		threadView.addStyleName("msg-center");
		threadStatement(thread, threadView);
	
		VerticalPanel messages = new VerticalPanel();
		messages.setWidth("100%");
		
		for (final UserMessage t : thread.getMessages()){
			VerticalPanel message = new VerticalPanel();
			
			HTML cmt = new HTML(t.getComment());
			cmt.setStylePrimaryName(MSG_STYLE);
			message.add(cmt);
			message.addStyleName(MSGBOX_STYLE);
	
			if (t.getVote() == Vote.NO_VOTE){
				final HorizontalPanel vote = new HorizontalPanel();
				vote.add(new Label("Was this comment helpful?"));
				Image upVote = new Image("img/green_check.gif");
				upVote.addStyleName("img-btn");
			
				upVote.addClickHandler(new ClickHandler(){
					@Override
					public void onClick(ClickEvent event) {
						recordVote(thread, t, Vote.GOOD);
					}
				});
				
				Image downVote = new Image("img/redx.gif");
				downVote.addStyleName("img-btn");
				downVote.addClickHandler(new ClickHandler(){
					@Override
					public void onClick(ClickEvent event) {
						recordVote(thread, t, Vote.BAD);
					}
				});
				
				vote.add(upVote);
				vote.add(downVote);

				message.add(vote);
			}
			
			messages.add(message);
		}
		threadView.add(messages);
	}
	
	private static void threadStatement(UserMessageThread thread, VerticalPanel panel){
		UserMessage first = thread.getFirst();
		
		Label method = new Label("An attempt to verify " + first.getSourceMethod() + " failed:");
		
		if (first instanceof ImpossibleMessage){
			ImpossibleMessage m = (ImpossibleMessage) first;
			switch (m.getReason()){
			case NOT_LISTED:
				method = new Label("A user indicated that a pre-condition was missing when selecting pre-conditions for " + m.getSourceMethod() + ":");
				break;
			case STRONG_REQ:
				method = new Label("A user indicated the following pre-condition was too strong when working with method " + m.getSourceMethod() + ":");
				break;
			case WEAK_ENS:
			case WEAK_EXS:
				method = new Label("A user indicated that this method's post conditions were too weak when working with method " + m.getSourceMethod() + ":");
				break;
			}
		}
		
		method.setStylePrimaryName(SRC_STYLE);
		panel.add(method);
		
		if (first.getSourceSpec() != null){
			Label spec = new Label("Invariant " + first.getSourceSpec());
			spec.setStylePrimaryName(MSG_STYLE);
			panel.add(spec);
		}
	}
}
