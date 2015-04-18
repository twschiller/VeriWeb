package com.schiller.veriasa.web.client.views;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

/**
 * Loading page that displays a random tip to the user
 * @author Todd Schiller
 */
public class LoadingPage extends Composite {

	@UiField HTML oldTip;
	@UiField HTML resultTip;
	@UiField HTML hoverDoc;
	@UiField HTML implies;
	@UiField HTML forall;
	
	
	
	private static LoadingPageUiBinder uiBinder = GWT
			.create(LoadingPageUiBinder.class);

	interface LoadingPageUiBinder extends UiBinder<Widget, LoadingPage> {
	}

	public LoadingPage() {
		initWidget(uiBinder.createAndBindUi(this));
		randomTip();
	}
	
	/**
	 * Change the random tip
	 */
	public void randomTip(){
		HTML tips[] = new HTML [] {oldTip, resultTip, hoverDoc, implies, forall};
		
		for (HTML tip : tips){
			tip.setVisible(false);
		}
		tips[(int)(Math.random() * tips.length)].setVisible(true);
	}
}
