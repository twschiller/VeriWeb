package com.schiller.veriasa.web.client.views.mturk;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class MTurkKeyView extends Composite {

	private static MTurkKeyViewUiBinder uiBinder = GWT
			.create(MTurkKeyViewUiBinder.class);

	interface MTurkKeyViewUiBinder extends UiBinder<Widget, MTurkKeyView> {
	}

	@UiField Label key;
	
	public MTurkKeyView() {
		initWidget(uiBinder.createAndBindUi(this));
	}
	
	public MTurkKeyView(String key) {
		initWidget(uiBinder.createAndBindUi(this));
		this.key.setText(key);
	}

}
