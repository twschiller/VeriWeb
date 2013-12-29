package com.schiller.veriasa.web.client.views;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.schiller.veriasa.web.shared.executejml.ValFragment;
import com.schiller.veriasa.web.shared.executejml.ValSpanMaker;
import com.schiller.veriasa.web.shared.parsejml.JmlSpan;
import com.schiller.veriasa.web.shared.parsejml.SpanMaker;

/**
 * Utility class for highlighting contract sub-expressions
 * @author Todd Schiller
 */
public class Spanner implements ValSpanMaker, SpanMaker{
	/**
	 * CSS style for fragments
	 */
	private String defaultStyle;
	
	/**
	 * CSS style for highlighted fragments
	 */
	private String highlightedStyle;
	
	/**
	 * id counter to ensure that contract fragment id DOM elements are unique
	 */
	private static int internalUniqueId = 0;
	
	/**
	 * DOM contract id -> tool-tip content
	 */
	private static final HashMap<Integer, String> tooltips = new HashMap<Integer, String>();
	
	/**
	 * Tool-tip pop-up
	 */
	private static final PopupPanel tooltip = new PopupPanel();
	
	/**
	 * Fragment -> sub-fragments
	 */
	private static final HashMap<Object, List<?>> associationMap = new HashMap<Object, List<?>>();
	
	/**
	 * contract id -> contract fragment; inverse of {@link Spanner#fragmentIds}
	 */
	private static final HashMap<Integer, Object> fragmentById = new HashMap<Integer, Object>();
	
	/**
	 * contract fragment -> contract id; inverse of {@link Spanner#fragmentById}
	 */
	private static final HashMap<Object, Integer> fragmentIds = new HashMap<Object, Integer>();
	
	/**
	 * HTML panels registered with the mouse-event observer
	 */
	private static final List<HTMLPanel> topLevelPanels = new ArrayList<HTMLPanel>();
	
	/**
	 * cache for DOM element id -> GWT HTML panel; value set is {@link specPanels}
	 */
	private static final HashMap<Integer, HTMLPanel> panelById = new HashMap<Integer,HTMLPanel>();
	
	/**
	 * Initialize the styles, and export the Javascript callbacks via
	 * {@link exportSpecOver} and {@link exportSpecOut}
	 * @param defaultStyle the default CSS style
	 * @param highlightStyle the CSS style to use when highlighting a subexpression
	 */
	public Spanner(String defaultStyle, String highlightStyle, boolean reset){
		this.defaultStyle = defaultStyle;
		this.highlightedStyle = highlightStyle;
		
		if (reset){
			reset();
		}
		
		exportSpecOver();
		exportSpecOut();
		
		System.out.println("Constructing spanner with styles " + defaultStyle + " and " + highlightStyle);
	}
	
	public static void reset(){
		associationMap.clear();
		fragmentById.clear();
		fragmentIds.clear();
		topLevelPanels.clear();
		panelById.clear();
		tooltip.clear();
	}
	
	/**
	 * Register a panel with the mouse-event observer
	 * @param panel the panel to register
	 */
	public void registerMouseListener(HTMLPanel panel){
		topLevelPanels.add(panel);
	}
	
	private int generateUniqueId(){
		if (internalUniqueId == Integer.MAX_VALUE){
			internalUniqueId = 0;
		}
		return ++internalUniqueId;
	}
	
	@Override
	public String makeSpan(ValFragment fragment, String tooltip, List<ValFragment> associated) {
		if (associated.contains(null)){
			throw new NullPointerException("associated fragment cannot be null");
		}
		
		int uniqueId = generateUniqueId();
		
		fragmentById.put(uniqueId, fragment);
		fragmentIds.put(fragment, uniqueId);
		associationMap.put(fragment, associated);
		
		tooltips.put(uniqueId, tooltip);
		
		return "<span id=\"spec" + uniqueId + "\" "  
			+ "onMouseOver=\"specOver(" + uniqueId + ",'"+ highlightedStyle + "');\" "
			+ "onMouseOut=\"specOut(" + uniqueId + ",'" + defaultStyle + "');\""
			+ " class=\"" + defaultStyle + "\""
			+ ">" + fragment.getText() + "</span>";
	}
	
	@Override
	public String makeSpan(JmlSpan jml, List<JmlSpan> associated) {
		if (associated.contains(null)){
			throw new NullPointerException("associated fragment cannot be null");
		}
		
		int uniqueId = generateUniqueId();
		
		fragmentById.put(uniqueId, jml);
		fragmentIds.put(jml, uniqueId);
		associationMap.put(jml, associated);
		
		return "<span id=\"spec" + uniqueId + "\" "  
			+ "onMouseOver=\"specOver(" + uniqueId + ",'"+ highlightedStyle + "');\" "
			+ "onMouseOut=\"specOut(" + uniqueId + ",'" + defaultStyle + "');\""
			+ " class=\"" + defaultStyle + "\""
			+ ">" + jml.getText() + "</span>";
	}
	
	/**
	 * Returns the {@link HTMLPanel} associated with span <code>fragmentId</code>.
	 * @param fragmentId the fragment identifier
	 * @return the {@link HTMLPanel} associated with span <code>fragmentId</code>.
	 */
	private HTMLPanel lookupPanel(int fragmentId){
		if (panelById.containsKey(fragmentId)){
			return panelById.get(fragmentId);
		}
		
		for (HTMLPanel panel : topLevelPanels){
			Element fragmentElement = panel.getElementById("spec" + fragmentId);
			if (fragmentElement != null){
				panelById.put(fragmentId, panel);
				return panel;
			}
		}
		
		throw new RuntimeException("Could not locate HTMLPanel for fragment " + fragmentId);
	}
	
	/**
	 * Change the CSS style of displayed specification fragment.
	 * @param fragmentId the specification id (DOM element id is prefixed with <code>spec</code>)
	 * @param style the new CSS style
	 */
	private void setFragmentStyle(int fragmentId, String style){
		HTMLPanel panel = lookupPanel(fragmentId);
		Element element = panel.getElementById("spec" + fragmentId);
		
		if (element == null){
			throw new RuntimeException("Could not locate HTML element for fragment " + fragmentId);
		}
		
		element.setClassName(style);
	}
	
	/**
	 * Display a simple tooltip at the specified location
	 * @param content the tooltip text
	 * @param mouseX the horizontal location
	 * @param mouseY the vertical location
	 */
	private void showToolTip(String content, int mouseX, int mouseY){
		tooltip.clear();
		tooltip.add(new Label(content));
		
		tooltip.setPopupPosition(mouseX + 15, mouseY + 10);
		
		tooltip.show();
	}
	
	
	/**
	 * UI callback triggered when the user moves the mouse over a contract:
	 * (1) sets the style of the element to <code>style</code>
	 * (2) trigger the associated tooltip, if there is one
	 * @param id the id of the DOM element
	 * @param mouseX the horizontal mouse position
	 * @param mouseY the vertical mouse position
	 * @param style the new CSS style
	 */
	private void specOver(int id, int mouseX, int mouseY, String style){
		if (!fragmentById.containsKey(id)){
			// this fragment is owned by a different spanner
			return;
		}
		
		setFragmentStyle(id, style);
		
		for (Object child : associationMap.get(fragmentById.get(id))){
			setFragmentStyle(fragmentIds.get(child),  style);
		}
		
		if (tooltips.containsKey(id)){
			showToolTip(tooltips.get(id), mouseX, mouseY);
		}	
	}
	
	/**
	 * UI callback triggered when the user moves his mouse out of a contract;
	 * sets the style of the element to <code>style</code>
	 * @param id the id of the DOM element
	 * @param style the new CSS style
	 */
	private void specOut(int id, String style){
		if (!fragmentById.containsKey(id)){
			// this fragment is owned by a different spanner
			return;
		}
		
		setFragmentStyle(id, style);
		for (Object child : associationMap.get(fragmentById.get(id))){
			setFragmentStyle(fragmentIds.get(child),  style);
		}
		tooltip.hide();
	}
	
	/**
	 * Javascript to register the {@link specOver} callback
	 */
	private native void exportSpecOver() /*-{
	var _this = this;
	$wnd.specOver = function(id, style){
	
		var xloc = $wnd.mx;
		var yloc = $wnd.my;
	
		if (xloc < 0){xloc = 0;}
		if (yloc < 0){yloc = 0;}  
		
		_this.@com.schiller.veriasa.web.client.views.Spanner::specOver(IIILjava/lang/String;)(id, xloc, yloc, style);
	};
}-*/;
	
	/**
	 * Javascript to register the {@link specOut} callback
	 */
	private native void exportSpecOut() /*-{
	var _this = this;
	$wnd.specOut = function(id, style){
		_this.@com.schiller.veriasa.web.client.views.Spanner::specOut(ILjava/lang/String;)(id, style);
	};
}-*/;
}

