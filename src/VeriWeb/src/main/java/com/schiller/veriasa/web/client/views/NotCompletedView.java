package com.schiller.veriasa.web.client.views;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.safehtml.shared.SimpleHtmlSanitizer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.schiller.veriasa.web.shared.solutions.ImpossibleInfo;
import com.schiller.veriasa.web.shared.solutions.ImpossibleInfo.Reason;

public class NotCompletedView extends Composite {

	private static final int MIN_COMMENT_LENGTH = 5;
	
	@UiField
	TextArea comment;
	
	@UiField 
	Button okButton;
	
	@UiField
	Button cancelButton;
	
	@UiField
	ListBox dropDown;
	
	@UiField
	VerticalPanel selectMethod;
	
	@UiField RadioButton choiceNotListed;
	@UiField RadioButton choiceReq;
	@UiField RadioButton choiceEns;
	@UiField RadioButton choiceExs;
	@UiField RadioButton choiceBug;
	
	private RadioButton [] needsMethod;
	private RadioButton [] noMethod;
	
	private String currentMethod;
	
	public interface DialogHandler{
		void onOk(ImpossibleInfo response);
		void onCancel();
	}
	
	public void setNotListedVisible(boolean visible){
		choiceNotListed.setVisible(visible);
	}
	
	public void setChoices(List<String> choices){
		dropDown.clear();
		
		for (String s : choices){
			dropDown.addItem(s);
		}
	
		for (RadioButton b : needsMethod){
			b.setVisible(!choices.isEmpty());
			b.setEnabled(!choices.isEmpty());
		}	
	}
	
	public void setHandler(DialogHandler handler) {
		this.handler = handler;
	}

	private DialogHandler handler = null;
	
	private static NotCompletedViewUiBinder uiBinder = GWT
			.create(NotCompletedViewUiBinder.class);

	interface NotCompletedViewUiBinder extends
			UiBinder<Widget, NotCompletedView> {
	}
	
	public NotCompletedView(String currentMethod) {
		initWidget(uiBinder.createAndBindUi(this));
		
		this.currentMethod = currentMethod;
		
		needsMethod = new RadioButton [] {choiceReq,choiceEns,choiceExs};
		noMethod = new RadioButton [] {choiceNotListed,choiceBug};
		
		for (RadioButton b : needsMethod){
			b.addValueChangeHandler(new ValueChangeHandler<Boolean>(){
				@Override
				public void onValueChange(ValueChangeEvent<Boolean> event) {
					dropDown.setEnabled(event.getValue() && dropDown.getItemCount() > 0);
					selectMethod.setVisible(event.getValue());
					markOk();
				}
			});
		}
		for (RadioButton b : noMethod){
			b.addValueChangeHandler(new ValueChangeHandler<Boolean>(){
				@Override
				public void onValueChange(ValueChangeEvent<Boolean> event) {
					dropDown.setEnabled(!event.getValue() && dropDown.getItemCount() > 0);
					selectMethod.setVisible(!event.getValue());
					markOk();
				}
			});
		}
		
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
		choiceNotListed.setValue(true);
	}
	
	@UiHandler("okButton")
	void onOk(ClickEvent e) {
		if (handler != null){
			if (choiceBug.getValue()){
				handler.onOk(new ImpossibleInfo(Reason.BUG,currentMethod,comment.getText().trim()));
			}else if(choiceNotListed.getValue()){
				handler.onOk(new ImpossibleInfo(Reason.NOT_LISTED,currentMethod,comment.getText().trim()));
			}else{
				Reason r = choiceReq.getValue() ? Reason.STRONG_REQ :
								choiceEns.getValue() ? Reason.WEAK_ENS :
								Reason.WEAK_EXS;
				
				handler.onOk(new ImpossibleInfo(r,
						dropDown.getItemText(dropDown.getSelectedIndex()),
						SimpleHtmlSanitizer.sanitizeHtml(comment.getText().trim()).asString()));
			}
		}
	}
	

	void markOk(){
		
		boolean ok = true;
		
		String val = comment.getValue().trim();
		
		if (val.length() < MIN_COMMENT_LENGTH){
			ok = false;
		}
		
		okButton.setEnabled(ok);
	}
	
	@UiHandler("cancelButton")
	void onCancel(ClickEvent e) {
		if (handler != null){
			handler.onCancel();
		}
	}
}
