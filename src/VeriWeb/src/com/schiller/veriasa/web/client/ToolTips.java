package com.schiller.veriasa.web.client;

import com.google.gwt.event.dom.client.HasMouseOutHandlers;
import com.google.gwt.event.dom.client.HasMouseOverHandlers;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Utility functions for attaching tooltips to UI elements
 * @author Todd Schiller
 */
public abstract class ToolTips {

	public static final int OFFSET_X = 5;
	public static final int OFFSET_Y = 10;
	
	/**
	 * Associate a tooltip with a widget, displaying the tooltip on mouse over
	 * @param widget the widget to associate the tooltip with
	 * @param info the tooltip content
	 * @param autoHide {@code true} if the popup should be automatically hidden when the user clicks outside of it or the history token changes.
	 * @param hideOnMouseOut {@code true} iff the tooltip should disappear when the mouse moves outside of it
	 */
	public static <T extends Widget & HasMouseOverHandlers & HasMouseOutHandlers> 
	void addTooltip(final T widget, final Widget info, boolean autoHide, boolean hideOnMouseOut){
		final PopupPanel infoPopup = new PopupPanel(autoHide);
		
		widget.addMouseOverHandler(new MouseOverHandler(){
			@Override
			public void onMouseOver(MouseOverEvent event) {
				infoPopup.clear();
				infoPopup.add(info);

				infoPopup.setPopupPosition(event.getClientX() + OFFSET_X, event.getClientY() + OFFSET_Y);

				if (!infoPopup.isShowing()){
					infoPopup.show();
				}
			}
		});

		if (hideOnMouseOut){
			widget.addMouseOutHandler(new MouseOutHandler(){
				@Override
				public void onMouseOut(MouseOutEvent event) {
					infoPopup.hide();
				}
			});
		}
	}
	
}
