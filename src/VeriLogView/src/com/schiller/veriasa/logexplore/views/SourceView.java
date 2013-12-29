package com.schiller.veriasa.logexplore.views;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;

import com.schiller.veriasa.web.shared.escj.AnnotatedFile;

@SuppressWarnings("serial")
public class SourceView extends JTextArea{

	public SourceView(AnnotatedFile af) {
		
		
		this.setText(af.getAnnotatedBody());
	
		
		final JPopupMenu contextMenu = new JPopupMenu("Source");
		JMenuItem item = new JMenuItem("Select All");
		item.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				SourceView.this.selectAll();
				
			}
		});
		contextMenu.add(item);
		
		
		JMenuItem cpy = new JMenuItem("Copy");
		cpy.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				SourceView.this.copy();
			}
		});
		contextMenu.add(cpy);
		
	
		
		this.setComponentPopupMenu(contextMenu);
	}
	
	
	

}
