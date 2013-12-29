package com.schiller.veriasa.logexplore.action;

import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.schiller.veriasa.web.server.logging.SwitchModeAction;

@SuppressWarnings("serial")
public class SwitchModeActionView extends JPanel {

	public SwitchModeActionView(SwitchModeAction action){
		super(new GridLayout(1,1));
		
		this.add(new JLabel("Write Mode:"));
		this.add(new JLabel(action.getNewMode().toString()));
	} 


}
