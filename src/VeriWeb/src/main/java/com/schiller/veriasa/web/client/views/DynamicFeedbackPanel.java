package com.schiller.veriasa.web.client.views;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGrid;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGridCellRenderer;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.schiller.veriasa.web.client.UxUtil;
import com.schiller.veriasa.web.shared.executejml.VarTreeNode;
import com.schiller.veriasa.web.shared.feedback.DynamicFeedback;

/**
 * View for feedback from dynamic execution
 * @author Todd Schiller
 */
public class DynamicFeedbackPanel extends VerticalPanel{

	private Spanner sliceSpanner = new Spanner("spec-def", "spec-high", false);	
	
	/**
	 * Construct a view for the {@code feedback}
	 * @param feedback the dynamic execution feedback
	 */
	public DynamicFeedbackPanel(DynamicFeedback feedback){
		// the header
		Label header = new Label("The condition is falsified during a correct method call");
		header.setStylePrimaryName("dyn-head");
		add(header);

		// the description /  instructions
		HTML description = new HTML(
				"There are two ways to explore the values: (1) expanding nodes in the table below" +
				", and <br/>(2) by hovering your mouse over subexpressions in the condition below.");
		add(description);
		
		add(UxUtil.spacer(15));
		
		// the interactive expression
		HTMLPanel expression = new HTMLPanel(feedback.getFragment().generateHtml(sliceSpanner));
		sliceSpanner.registerMouseListener(expression);
		add(expression);
		
		add(UxUtil.spacer(15));
		
		TreeStore<ModelData> store = new TreeStore<ModelData>();
		for (VarTreeNode node : feedback.getSlice()){
			store.add(node, true);
		}

		ColumnConfig name = new ColumnConfig("name", "Name", 100);
		name.setSortable(false);
		name.setMenuDisabled(true);
		name.setRenderer(new TreeGridCellRenderer<ModelData>());
		ColumnConfig before = new ColumnConfig("before", "Before Call", 100);
		ColumnConfig after = new ColumnConfig("after", "After Call", 100);
		before.setSortable(false);
		before.setMenuDisabled(true);
		after.setSortable(false);
		after.setMenuDisabled(true);
		List<ColumnConfig> cms = new ArrayList<ColumnConfig>();
		cms.add(name);
		cms.add(before);
		cms.add(after);

		ColumnModel cm = new ColumnModel(cms);
		TreeGrid<ModelData> treeGrid = new TreeGrid<ModelData>(store, cm);
		treeGrid.setSize(400, 400);   
		treeGrid.setAutoExpandColumn("name");
		treeGrid.setBorders(true);
		treeGrid.setAutoHeight(true);
		
		add(treeGrid);
	}
	
}
