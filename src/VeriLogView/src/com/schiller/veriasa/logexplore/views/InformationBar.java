package com.schiller.veriasa.logexplore.views;

import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.schiller.veriasa.logexplore.util.ViewUtil;
import com.schiller.veriasa.web.server.logging.LogEntry;
import com.schiller.veriasa.web.shared.logging.HasProblem;
import com.schiller.veriasa.web.shared.problems.MethodProblem;

@SuppressWarnings("serial")
public class InformationBar extends JPanel{
	public InformationBar(){
		setLayout(new FlowLayout());
	}
	
	public void populate(LogEntry entry){
		this.removeAll();
		repaint();
		add(new JLabel("Time:"));
		add(new JLabel(ViewUtil.formatTimestamp(entry.getTimestamp())));
		
		add(new JLabel("  |  User:"));
		add(new JLabel(ViewUtil.userId(entry.getAction())));
		
		if (entry.getAction() instanceof HasProblem){
			HasProblem hp = (HasProblem) entry.getAction();
			MethodProblem fp = (MethodProblem) hp.getProblem();
			add(new JLabel("  |  " + fp.getClass().getSimpleName()));
			
			add(new JLabel("  |  "));
			add(new JLabel(fp.getFunction().getSignature()));
		}
		validate();
	}
}
