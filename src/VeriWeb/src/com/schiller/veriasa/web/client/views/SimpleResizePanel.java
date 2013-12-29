package com.schiller.veriasa.web.client.views;

import com.google.gwt.user.client.ui.ProvidesResize;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.ScrollPanel;

public class SimpleResizePanel extends ScrollPanel implements ProvidesResize,RequiresResize {

	@Override
	public void onResize() {
		if (getWidget() instanceof RequiresResize){
			((RequiresResize) getWidget()).onResize();
		}
	}

}
