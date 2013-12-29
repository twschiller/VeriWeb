package com.veriasa.speceditor.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.VerticalPanel;

public class SpecSelector extends Composite {

	private final FlexTable preTable = new FlexTable();
	private final VerticalPanel vp = new VerticalPanel();
	
	private List<String> cs = new ArrayList<String>();
	
	private static final int NUM_COLS = 1;
	
	public void updateTable(){
		preTable.removeAllRows();
		int rr = 0;
		int cc = 0;
		
		for (String s : cs){
			if (cc == NUM_COLS){
				rr++;
				cc = 0;
			}
		
			CheckBox cb = new CheckBox(s);
			
			preTable.setWidget(rr, cc, cb);
			preTable.getFlexCellFormatter().setStyleName(rr,cc, "pre-inactive");
			
			final int rf = rr; final int cf = cc;
			
			cb.addValueChangeHandler(new ValueChangeHandler<Boolean>(){
				@Override
				public void onValueChange(ValueChangeEvent<Boolean> event) {
					preTable.getFlexCellFormatter().setStyleName(rf, cf, 
							event.getValue() ? "pre-active" : "pre-inactive");
				}
			});
		
			cc++;
		}

		
		preTable.setWidth("100%");
		
	}
	
	public void setValues(List<String> vs){
		cs = vs;
		updateTable();
	}
	
	public SpecSelector(){
		
		vp.add(preTable);
		initWidget(vp);
		
		cs.addAll(Arrays.asList("theStack != null", "top >= 0", "bottom >= 0",
	      "theStack != null"));
		
		updateTable();
	}
	
}
