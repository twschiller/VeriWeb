package com.schiller.veriasa.logexplore.action;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

import com.schiller.veriasa.logexplore.util.SpringUtilities;
import com.schiller.veriasa.web.server.logging.MessageAction;

@SuppressWarnings("serial")
public class UserMessageView extends JPanel {
	public UserMessageView(MessageAction action){
		super(new SpringLayout());

		this.add(new JLabel("Source Spec:"));
		this.add(new JLabel(action.getMessage().getSourceSpec()));

		this.add(new JLabel("Message"));
		this.add(new JLabel(action.getMessage().getComment()));
		SpringUtilities.makeCompactGrid(this,
				2, 2, //rows, cols
				6, 6,        //initX, initY
				6, 6);       //xPad, yPad
	}
}
