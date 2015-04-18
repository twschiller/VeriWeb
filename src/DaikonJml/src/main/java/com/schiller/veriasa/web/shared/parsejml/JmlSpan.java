package com.schiller.veriasa.web.shared.parsejml;

import java.io.Serializable;
import java.util.List;

public abstract class JmlSpan implements Serializable{
	private static final long serialVersionUID = 1L;
	
	public JmlSpan(){	
	}
	
	public abstract boolean isBoilerPlate();
	public abstract String getText();
	public abstract String generateHtml(SpanMaker spanMaker);
	public abstract List<JmlSpan> allSubFragments();
}
