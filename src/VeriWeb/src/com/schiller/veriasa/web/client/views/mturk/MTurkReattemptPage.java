package com.schiller.veriasa.web.client.views.mturk;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

/**
 * Page shown when a MTurk user attempts to perform a HIT multiple times
 * @author Todd Schiller
 */
public class MTurkReattemptPage extends Composite {

	private static MTurkReattemptPageUiBinder uiBinder = GWT.create(MTurkReattemptPageUiBinder.class);

	interface MTurkReattemptPageUiBinder extends UiBinder<Widget, MTurkReattemptPage> {
	}

	public MTurkReattemptPage() {
		initWidget(uiBinder.createAndBindUi(this));
	}

}
