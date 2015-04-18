package com.schiller.veriasa.web.shared.executejml;

import java.io.Serializable;
import java.util.List;

/**
 * An expression (or subexpression) and its value, if it has one
 * @author Todd Schiller
 */
public abstract class ValFragment implements Serializable{
	private static final long serialVersionUID = 1L;
	
	/**
	 * @return <code>true</code> if the fragment has an associated value
	 */
	public abstract boolean hasValue();
	
	/**
	 * @return the fragment's value, or <code>null</code> if the fragment has no
	 * associated value
	 */
	public abstract String getValue();
	
	/**
	 * @return textual representation of the expression fragment
	 */
	public abstract String getText();
	
	/**
	 * HTML representation of the fragment, built using <code>spanMaker</code>
	 * @param spanMaker the HTML builder
	 * @return HTML representation of the fragment, built using <code>spanMaker</code>
	 */
	public abstract String generateHtml(ValSpanMaker spanMaker);

	/**
	 * @return the fragments that comprise this fragment
	 */
	public abstract List<ValFragment> allSubFragments();
}
