package com.schiller.veriasa.web.client;

import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Label;

/**
 * Utility methods for generating user interface elements
 * @author Todd Schiller
 */
public final class UxUtil {

	private UxUtil(){
	}
	
	/**
	 * Create a vertical spacer
	 * @param height height in px
	 * @return a vertical spacer with height {@code height} px
	 */
	public static AbsolutePanel spacer(int height){
		return spacer(height + "px");
	}
	
	/**
	 * Create a vertical spacer
	 * @param height height (e.g., 10px or 2em)
	 * @return a vertical spacer with height {@code height}
	 */
	public static AbsolutePanel spacer(String height){
		AbsolutePanel spacer = new AbsolutePanel();
		spacer.add(new Label(" "));
		spacer.setHeight(height);
		return spacer;
	}
}
