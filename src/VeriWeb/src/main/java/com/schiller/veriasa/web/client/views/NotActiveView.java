package com.schiller.veriasa.web.client.views;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class NotActiveView extends Composite {

	private static NotActiveViewUiBinder uiBinder = GWT
			.create(NotActiveViewUiBinder.class);

	interface NotActiveViewUiBinder extends UiBinder<Widget, NotActiveView> {
	}

	public NotActiveView() {
		initWidget(uiBinder.createAndBindUi(this));
	}

}
