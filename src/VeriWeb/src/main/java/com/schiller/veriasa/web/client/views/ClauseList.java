package com.schiller.veriasa.web.client.views;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.schiller.veriasa.web.shared.core.Clause;

public class ClauseList extends Composite{

	private final FlexTable specTable = new FlexTable();
	private final VerticalPanel mainPanel = new VerticalPanel();
	
	private List<Clause> specs = new ArrayList<Clause>();
	
	public void updateTable(){
		specTable.removeAllRows();
		
		int rr = 0;
	
		for (Clause s : specs){
			
			specTable.setWidget(rr, 0, new Label(s.getClause()));
			specTable.getFlexCellFormatter().addStyleName(rr,0, "provided-spec");
			rr++;
		}
		specTable.setWidth("100%");
	}

	public void setValues(List<Clause> vs){
		specs = new ArrayList<Clause>(vs);
		updateTable();
	}
	
	public ClauseList(){
		
		mainPanel.add(specTable);
		initWidget(mainPanel);
		updateTable();
	}
}
