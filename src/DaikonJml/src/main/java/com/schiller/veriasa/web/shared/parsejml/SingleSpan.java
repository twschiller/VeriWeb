package com.schiller.veriasa.web.shared.parsejml;

import java.util.ArrayList;
import java.util.List;

public class SingleSpan extends JmlSpan{
	private static final long serialVersionUID = 1L;
	private String text;
	private boolean boilerPlate;
	
	@SuppressWarnings("unused")
	private SingleSpan(){
		super();
	}
	
	public SingleSpan(String text, boolean boilerPlate) {
		super();
		this.text = text;
		this.boilerPlate = boilerPlate;
	}
	
	@Override
	public String generateHtml(SpanMaker spanMaker) {
		return spanMaker.makeSpan(this, new ArrayList<JmlSpan>());
	}
	
	@Override
	public String getText() {
		return text;
	}

	@Override
	public List<JmlSpan> allSubFragments() {
		List<JmlSpan> xx = new ArrayList<JmlSpan>();
		xx.add(this);
		return xx;
	}

	@Override
	public boolean isBoilerPlate() {
		return boilerPlate;
	}
	
	
}
