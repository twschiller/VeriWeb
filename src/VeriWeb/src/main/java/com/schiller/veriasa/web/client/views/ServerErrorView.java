package com.schiller.veriasa.web.client.views;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class ServerErrorView extends Composite {

	private static ServerErrorViewUiBinder uiBinder = GWT
			.create(ServerErrorViewUiBinder.class);

	interface ServerErrorViewUiBinder extends UiBinder<Widget, ServerErrorView> {
	}

	public ServerErrorView() {
		initWidget(uiBinder.createAndBindUi(this));
	}

}
