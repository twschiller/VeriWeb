package com.schiller.veriasa.web.client.views;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SimpleHtmlSanitizer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;
import com.schiller.veriasa.web.shared.solutions.ImpossibleInfo;

public class IssueView extends Composite {

	private static final int MIN_COMMENT_LENGTH = 5;

	
	private static IssueViewUiBinder uiBinder = GWT
			.create(IssueViewUiBinder.class);
	
	public interface DialogHandler{
		void onOk(SafeHtml response);
		void onCancel();
	}
	
	interface IssueViewUiBinder extends UiBinder<Widget, IssueView> {
	}
	
	@UiField
	TextArea comment;
	
	@UiField 
	Button okButton;
	
	@UiField
	Button cancelButton;
	
	@UiField 
	HTML missingMsg;
	
	@UiField 
	HTML strongMsg;
	
	@UiField 
	HTML weakMsg;
	
	
	public void setMsg(ImpossibleInfo.Reason type){
		switch (type){
		case NOT_LISTED: 
			missingMsg.setVisible(true);
			strongMsg.setVisible(false);
			weakMsg.setVisible(false);
			break;
		case STRONG_REQ: 
			strongMsg.setVisible(true);
			missingMsg.setVisible(false);
			weakMsg.setVisible(false);
			break;
		case WEAK_EXS:
		case WEAK_ENS: 
			weakMsg.setVisible(true);
			missingMsg.setVisible(false);
			strongMsg.setVisible(false);
			break;
		}
	}
	
	public void focus(){
		comment.setFocus(true);
	}
	
	public IssueView(ImpossibleInfo.Reason type) {
		initWidget(uiBinder.createAndBindUi(this));
		
		setMsg(type);
		comment.addKeyPressHandler(new KeyPressHandler(){
			@Override
			public void onKeyPress(KeyPressEvent event) {
				markOk();
			}
		});
		
		comment.addValueChangeHandler(new ValueChangeHandler<String>(){
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				markOk();
			}
		});
		
		comment.setFocus(true);
	}
	
	private DialogHandler handler = null;
	
	public void setHandler(DialogHandler dialogHandler) {
		this.handler = dialogHandler;
	}
	
	void markOk(){
		okButton.setEnabled(comment.getValue().trim().length() >= MIN_COMMENT_LENGTH);
	}

	@UiHandler("cancelButton")
	void onCancel(ClickEvent e) {
		if (handler != null){
			handler.onCancel();
		}
	}
	
	@UiHandler("okButton")
	void onOk(ClickEvent e) {
		if (handler != null){
			handler.onOk(SimpleHtmlSanitizer.sanitizeHtml(comment.getText().trim()));
		}
	}
}
