package com.schiller.veriasa.web.shared.executejml;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * An expression subfragment with no children (subfragments)
 * @author Todd Schiller
 */
public class SingleFragment extends ValFragment implements Serializable{
	private static final long serialVersionUID = 1L;
	
	public String text;
	public String value;
		
	@SuppressWarnings("unused")
	private SingleFragment(){
	}
	
	/**
	 * Create a single expression fragment with an associated value
	 * @param text the textual representation of the expression fragment
	 * @param value the value of the expression fragment
	 */
	public SingleFragment(String text, String value) {
		super();
		this.text = text;
		this.value = value;
	}
	
	/**
	 * Create a single expression fragment without an associated value
	 * @param text the textual representation of the expression fragment
	 */
	public SingleFragment(String text) {
		this(text, null);
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public boolean hasValue() {
		return value != null;
	}

	@Override
	public String generateHtml(ValSpanMaker spanMaker) {
		return spanMaker.makeSpan(this, value, new ArrayList<ValFragment>());
	}
	
	@Override
	public String getText() {
		return text;
	}

	@Override
	public List<ValFragment> allSubFragments() {
		List<ValFragment> result = new ArrayList<ValFragment>();
		result.add(this);
		return result;
	}
}
