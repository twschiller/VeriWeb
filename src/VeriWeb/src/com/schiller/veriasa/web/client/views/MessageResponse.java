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
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;

public class MessageResponse extends Composite {

	private static MessageResponseUiBinder uiBinder = GWT.create(MessageResponseUiBinder.class);

	interface MessageResponseUiBinder extends UiBinder<Widget, MessageResponse> {
	}
	
	@UiField
	TextArea comment;
	
	@UiField 
	Button okButton;
	
	@UiField
	Button cancelButton;

	private static final int MIN_COMMENT_LENGTH = 5;
	
	public interface DialogHandler{
		void onOk(SafeHtml response);
		void onCancel();
	}
	
	public MessageResponse() {
		initWidget(uiBinder.createAndBindUi(this));
		
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
	
	public void focus(){
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
