package com.schiller.veriasa.web.client.views;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class MessageAlertDialog extends DialogBox{

	private final VerticalPanel panel = new VerticalPanel();
	private final FlowPanel buttons = new FlowPanel();
	
	public interface GoToCallback{
		void go();
	}
	
	public MessageAlertDialog(final GoToCallback callback){
		super(true /*auto-hide*/);
		
		setText("User-supplied Information Available");
		
		HTMLPanel content = new HTMLPanel(
				"<p>A user (possibly you) provided additional information for this problem.</p><br/>"
				+"<p>You can view this information now by clicking the View Information button below, or at any time by clicking the " +
				"toolbar link.</p>");
		
		content.setStylePrimaryName("message-alert-content");
		
		panel.add(content);
		
		Button ignore = new Button("Ignore");
		ignore.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				MessageAlertDialog.this.hide();
			}
		});
		
		ignore.setStylePrimaryName("message-alert-button");
		buttons.add(ignore);
		
		Button view = new Button("View Information");
		view.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				MessageAlertDialog.this.hide();
				callback.go();
			}
		});
		view.setStylePrimaryName("message-alert-button");
		buttons.add(view);
		
		panel.add(buttons);
		
		panel.setStylePrimaryName("message-alert-panel");
		
		setWidget(panel);
	}
	
}
