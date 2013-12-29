package com.schiller.veriasa.web.client.views.mturk;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.NamedFrame;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class MTurkFeedbackForm extends Composite {

	private static final String SANDBOX_URL = "http://workersandbox.mturk.com/mturk/externalSubmit";
	private static final String MTURK_URL = "http://www.mturk.com/mturk/externalSubmit";
	
	final FormPanel form = new FormPanel(new NamedFrame("_self"));
	final VerticalPanel panel = new VerticalPanel();
	
	public interface CancelCallback{
		void onCancel();
	}
	
	public void addFreeResponse(String name, String prompt){
		Label l = new Label(prompt);
		l.setStylePrimaryName("feedback-question");
		panel.add(l);
		
		TextArea a = new TextArea();
		a.setSize("350px", "100px");
		
		a.setName(name);
		panel.add(a);
	}
	
	public void addShortResponse(String name, String prompt, int length){
		
		Label l = new Label(prompt);
		panel.add(l);
		
		TextBox a = new TextBox();
		a.setMaxLength(length);
		a.setName(name);
		panel.add(a);
	}
	
	public static void addBreak(Panel x, int size){
		Label l = new Label(" ");
		l.setHeight(size + "px");
		l.setWidth(size+"px");
		x.add(l);
	}
	
	public MTurkFeedbackForm(String header,String msg, String names[], String values[], final CancelCallback cancel) {
		initWidget(form);
		form.setEncoding(FormPanel.ENCODING_URLENCODED);
		form.setMethod(FormPanel.METHOD_GET);
		
		panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		panel.setWidth("400px");
		
		if (header  != null){
			//Add header
			Label hd = new Label(header);
			hd.setStylePrimaryName("feedback-header");
			panel.add(hd);
			panel.setCellHorizontalAlignment(hd, HasHorizontalAlignment.ALIGN_CENTER);
		}
		
	
		addBreak(panel,10);
		
		//Add msg
		Label m = new Label(msg);
		m.setStylePrimaryName("feedback-msg");
		panel.add(m);
		panel.setCellHorizontalAlignment(m, HasHorizontalAlignment.ALIGN_LEFT);
		
		addBreak(panel,10);
		
		
		if (names !=null && values != null){
			for (int i = 0; i < names.length; i++){
				Hidden h = new Hidden(names[i],values[i]);
				panel.add(h);
			}
		}
		
		Hidden mturkId = new Hidden("workerId", com.google.gwt.user.client.Window.Location.getParameter("workerId"));
		Hidden proj = new Hidden("proj", com.google.gwt.user.client.Window.Location.getParameter("proj"));
		Hidden assign = new Hidden("assignmentId", com.google.gwt.user.client.Window.Location.getParameter("assignmentId"));
		panel.add(mturkId); panel.add(proj); panel.add(assign);
		
		addFreeResponse("missing", "Was there any information that you needed that was not provided? If so, what?");
		addBreak(panel,10);
		addFreeResponse("confusing", "Was there anything that you did not understand? If so, what?");
		addBreak(panel,10);
		addShortResponse("expected", "To perform this HIT again, how much would you need to be paid PER QUESTION?",8);
		addBreak(panel,10);
		
		HorizontalPanel btns = new HorizontalPanel();
		
		if (cancel != null){
			Button cBtn = new Button("Cancel");
			cBtn.addClickHandler(new ClickHandler(){
				@Override
				public void onClick(ClickEvent event) {
					cancel.onCancel();
				}
			});
			btns.add(cBtn);
			addBreak(btns,10);
		}
		
		Button submit = new Button("Submit HIT");
		btns.add(submit);
		
		submit.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				form.submit();
			}
		});
		
		panel.add(btns);

		form.add(panel);
		
		String submitTo = com.google.gwt.user.client.Window.Location.getParameter("turkSubmitTo");
		form.setAction(submitTo.contains("sandbox") ? SANDBOX_URL : MTURK_URL);			
	}
}
