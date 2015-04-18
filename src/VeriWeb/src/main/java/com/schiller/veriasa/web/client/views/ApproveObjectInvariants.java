package com.schiller.veriasa.web.client.views;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.schiller.veriasa.web.shared.core.Clause;

/**
 * Pop-up for selecting which statements to approve as object invariants
 * @author Todd Schiller
 */
public class ApproveObjectInvariants extends Composite {

	// TODO allow subexpression highlighting for invariants in the table
	
	private static ApproveObjectInvariantsUiBinder uiBinder = GWT
			.create(ApproveObjectInvariantsUiBinder.class);

	protected interface ApproveObjectInvariantsUiBinder extends
			UiBinder<Widget, ApproveObjectInvariants> {
	}

	public interface Callback{
		public void onSubmit(List<Clause> approved);
	}
	
	private final List<Clause> statements;
	private final List<CheckBox> checkboxes;
	private final Callback callback;
	
	@UiField
	protected Button okButton;

	@UiField
	protected FlexTable statementTable;
	
	public ApproveObjectInvariants(List<Clause> statements, Callback callback){
		initWidget(uiBinder.createAndBindUi(this));
		
		this.statements = new ArrayList<Clause>(statements);
		this.checkboxes = new ArrayList<CheckBox>(statements.size());
		this.callback = callback;
		
		statementTable.setCellPadding(6);
		statementTable.setCellSpacing(6);
		
		for (int row = 0; row < statements.size(); row++){
			CheckBox box = new CheckBox();
			checkboxes.add(box);
			statementTable.setWidget(row, 0, box);
			statementTable.setWidget(row, 1, new Label(statements.get(row).getClause()));
		}
	}

	@UiHandler("okButton")
	void onClick(ClickEvent e) {
		List<Clause> approved = new ArrayList<Clause>();
		
		for (int row = 0; row < statements.size(); row++){
			if (checkboxes.get(row).getValue()){
				approved.add(statements.get(row));
			}
		}
		
		callback.onSubmit(approved);
	}
}
