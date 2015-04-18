package com.schiller.veriasa.web.shared.executejml;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A {@link ValFragment} composed of multiple children.
 * @author Todd Schiller
 */
public class MultiFragment extends ValFragment implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private List<ValFragment> subFragments;
	private String value;
	
	@SuppressWarnings("unused")
	private MultiFragment(){
	}
	
	public MultiFragment(List<ValFragment> subFragments, String value){
		this.subFragments = new ArrayList<ValFragment>(subFragments);
		this.value = value;
	}
	
	public MultiFragment(ValFragment subFragments[], String value){
		this.subFragments = new ArrayList<ValFragment>(Arrays.asList(subFragments));
		this.value = value;
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public String generateHtml(ValSpanMaker spanMaker) {
		StringBuilder result = new StringBuilder();
	
		for (ValFragment sub : subFragments){
			if (sub.hasValue()){
				result.append(sub.generateHtml(spanMaker));
			}else{
				result.append(spanMaker.makeSpan(sub, value, allSubFragments()));
			}
		}
		return result.toString();
	}

	@Override
	public boolean hasValue() {
		return true;
	}

	@Override
	public String getText() {
		StringBuilder result = new StringBuilder();
		for (ValFragment subFragment : subFragments){
			result.append(subFragment.getText());
		}
		return result.toString();
	}

	@Override
	public List<ValFragment> allSubFragments() {
		ArrayList<ValFragment> result = new ArrayList<ValFragment>();
		for (ValFragment subFragment : subFragments){
			result.addAll(subFragment.allSubFragments());
		}
		return result;
	}	
}
