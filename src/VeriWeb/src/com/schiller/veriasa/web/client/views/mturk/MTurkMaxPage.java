package com.schiller.veriasa.web.client.views.mturk;


import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.schiller.veriasa.web.shared.mturk.MTurkProgress;

public class MTurkMaxPage extends Composite {

	private static MTurkMaxPageUiBinder uiBinder = GWT
			.create(MTurkMaxPageUiBinder.class);

	interface MTurkMaxPageUiBinder extends UiBinder<Widget, MTurkMaxPage> {
	}

	@UiField
	SimplePanel formPart;
	
	public MTurkMaxPage(MTurkProgress progress) {
		initWidget(uiBinder.createAndBindUi(this));
		
		String msg = "You have earned $" + (progress.getNumSolved() * progress.getRate()) 
			+ ". Complete the following survey to submit the HIT; a BONUS of " +
			"$" + progress.getRate() + " will be awarded for answering all of the questions.";
		
		String names [] = new String [] { "previewSolved", "numSolved", "rate", "earnings" };
		String values [] = new String [] { String.valueOf(progress.getNumPreviewSolved()), 
				String.valueOf(progress.getNumSolved()),
				String.valueOf(progress.getRate()),
				String.valueOf(progress.getEarned())};
		
		
		MTurkFeedbackForm f = new MTurkFeedbackForm(null, msg, names, values, null);
		
		formPart.add(f);
			
	}

}
