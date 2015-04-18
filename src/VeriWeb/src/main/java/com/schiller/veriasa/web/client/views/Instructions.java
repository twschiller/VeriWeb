package com.schiller.veriasa.web.client.views;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

public class Instructions extends Composite {

	private static InstructionsUiBinder uiBinder = GWT
			.create(InstructionsUiBinder.class);

	interface InstructionsUiBinder extends UiBinder<Widget, Instructions> {
	}

	@UiField
	HTML mainText;
	
	@UiField 
	HTML paletteText;
	
	@UiField
	HTML reqText;
	
	public Instructions(SafeHtml mainHtml, boolean paletteVisible, boolean reqVisible) {
		initWidget(uiBinder.createAndBindUi(this));
		
		this.mainText.setHTML("<p><span class=\"instruct\">" + mainHtml.asString() + "</span></p>");
		this.paletteText.setVisible(paletteVisible);
		this.reqText.setVisible(reqVisible);
	}
}
