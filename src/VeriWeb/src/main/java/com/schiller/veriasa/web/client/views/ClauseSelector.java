package com.schiller.veriasa.web.client.views;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.schiller.veriasa.web.shared.core.Clause;

public class ClauseSelector extends Composite {
	public interface SelectorEventHandler{
		public void activeSetChanged(List<Clause> active);
	}
	
	private final FlexTable statementTable = new FlexTable();
	private final VerticalPanel main = new VerticalPanel();
	
	private List<Clause> statements = new ArrayList<Clause>();
	private List<Boolean> states = new ArrayList<Boolean>(); 
	
	private SelectorEventHandler handler = null;
	
	private boolean canClick = true;
	
	public ClauseSelector(){
		main.add(statementTable);
		initWidget(main);
		updateTable();
	}

	public void updateTable(){
		statementTable.removeAllRows();
		
		for (int row = 0; row < statements.size(); row++){
			Clause statement = statements.get(row);
			
			final CheckBox checkbox = new CheckBox();
			checkbox.setEnabled(canClick);
			
			Label statementText = new Label(statement.getClause());
			
			statementTable.setWidget(row, 0, checkbox);
			statementTable.setWidget(row, 1, statementText);
			
			statementTable.getFlexCellFormatter().setStyleName(row, 1, 
					states.get(row) ? "pre-active" : "pre-inactive");
			checkbox.setValue(states.get(row));
			
			final int finalRow = row; 
			checkbox.addValueChangeHandler(new ValueChangeHandler<Boolean>(){
				@Override
				public void onValueChange(ValueChangeEvent<Boolean> event) {
					statementTable.getFlexCellFormatter().setStyleName(finalRow, 1, 
							event.getValue() ? "pre-active" : "pre-inactive");
					
					states.set(finalRow, event.getValue());
					
					if (handler != null){
						handler.activeSetChanged(getSelected());
					}
				}
			});
			
			if (canClick){
				statementText.addClickHandler(new ClickHandler(){
					@Override
					public void onClick(ClickEvent event) {
						checkbox.setValue(!checkbox.getValue(), true);
					}
				});
			}
		}
	
		statementTable.setWidth("100%");
	}
	
	public List<Clause> getSelected(){
		List<Clause> selected = new LinkedList<Clause>();
		for (int i = 0 ; i < states.size(); i++){
			if (states.get(i)){
				selected.add(statements.get(i));
			}
		}
		return selected;
	}
		
	public void setValues(List<Clause> choices, Set<Clause> active){
		statements = new ArrayList<Clause>(choices);
		states = new ArrayList<Boolean>(choices.size());
		
		for (int i = 0; i < choices.size(); i++){
			states.add(active.contains(statements.get(i)));
		}
		
		updateTable();
	}
	
	public void setCanClick(boolean canClick){
		this.canClick = canClick;
		updateTable();
	}
	
	public void setSelectorEventHandler(SelectorEventHandler handler) {
		this.handler = handler;
	}
}
