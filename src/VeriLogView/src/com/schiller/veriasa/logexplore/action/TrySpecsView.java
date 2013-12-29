package com.schiller.veriasa.logexplore.action;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

import com.schiller.veriasa.logexplore.util.SpringUtilities;
import com.schiller.veriasa.logexplore.util.ViewUtil;
import com.schiller.veriasa.web.server.logging.TrySpecsAction;

@SuppressWarnings("serial")
public class TrySpecsView extends JPanel {

	public TrySpecsView(TrySpecsAction action){
		super(new SpringLayout());
		
		this.add(new JLabel("Time:"));
		this.add(new JLabel(ViewUtil.formatTimestamp(action.timestamp)));
		
		for (int i=0; i < action.getSpecs().size(); i++){
			this.add(new JLabel(action.getSpecs().get(i).getProvenance()));
			this.add(new JLabel(action.getSpecs().get(i).getClause()));
			
		}
		
		SpringUtilities.makeCompactGrid(this,
				action.getSpecs().size() + 1, 2, //rows, cols
                6, 6,        //initX, initY
                6, 6);       //xPad, yPad
	}
	
}
