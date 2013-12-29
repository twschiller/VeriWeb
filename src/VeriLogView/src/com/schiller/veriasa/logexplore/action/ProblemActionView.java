package com.schiller.veriasa.logexplore.action;

import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.schiller.veriasa.web.server.logging.ProblemAction;

@SuppressWarnings("serial")
public class ProblemActionView extends JPanel{
	public ProblemActionView(ProblemAction action){
		super(new GridLayout(1,1));
	
		this.add(new JLabel("Action:"));
		this.add(new JLabel(action.getAction().toString()));

	}
}
