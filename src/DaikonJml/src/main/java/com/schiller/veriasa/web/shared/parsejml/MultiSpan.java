package com.schiller.veriasa.web.shared.parsejml;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MultiSpan extends JmlSpan implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private List<JmlSpan> subFragments;
	
	@SuppressWarnings("unused")
	private MultiSpan(){
		super();
	}
	
	public MultiSpan(List<JmlSpan> subFragments){
		super();
		this.subFragments = new ArrayList<JmlSpan>(subFragments);
	}
	
	public MultiSpan(JmlSpan subFragments[]){
		super();
		this.subFragments = new ArrayList<JmlSpan>(Arrays.asList(subFragments));
	}

	@Override
	public String generateHtml(SpanMaker spanMaker) {
		StringBuilder sb = new StringBuilder();
		
		for (JmlSpan sub : subFragments){
			if (!sub.isBoilerPlate()){
				sb.append(sub.generateHtml(spanMaker));
			}else{
				sb.append(spanMaker.makeSpan(sub, allSubFragments()));
			}
		}
		return sb.toString();
	}	
	
	@Override
	public String getText() {
		StringBuilder sb= new StringBuilder();
		for (JmlSpan sub : subFragments){
			sb.append(sub.getText());
		}
		return sb.toString();
	}

	@Override
	public List<JmlSpan> allSubFragments() {
		ArrayList<JmlSpan> fs = new ArrayList<JmlSpan>();
		for (JmlSpan sub : subFragments){
			fs.addAll(sub.allSubFragments());
		}
		return fs;
	}

	@Override
	public boolean isBoilerPlate() {
		return false;
	}	
}
