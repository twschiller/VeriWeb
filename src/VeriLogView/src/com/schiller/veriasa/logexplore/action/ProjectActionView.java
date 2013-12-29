package com.schiller.veriasa.logexplore.action;

import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.schiller.veriasa.web.server.logging.ProjectAction;

@SuppressWarnings("serial")
public class ProjectActionView extends JPanel{
	
	public ProjectActionView(ProjectAction action){
		super(new GridLayout(1,1));
	
		this.add(new JLabel(action.getProjectName()));
		this.add(new JLabel(action.getAction().toString()));

	}
}

