package com.schiller.veriasa.logexplore;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractListModel;
import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.schiller.veriasa.logexplore.util.ViewUtil;
import com.schiller.veriasa.web.server.logging.LogEntry;

@SuppressWarnings("serial")
public class UserTable extends JList{

	public static interface SelectHook{
		void select(String user);
	}
	
	List<String> slice;
	
	public UserTable(SelectHook hook){
		if (hook != null){
		    this.getSelectionModel().addListSelectionListener(new SharedListSelectionHandler(hook));
		}
		this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}
	

	public void populate(List<LogEntry> entries, boolean requireId){
		Set<String> users = new HashSet<String>();
		
		for (LogEntry e : entries){
			if (ViewUtil.userId(e.getAction()) != null){
				if (requireId && !ViewUtil.hasId(e.getAction())){
					continue;
					
				}
				
				users.add(ViewUtil.userId(e.getAction()));
			}
		}
		
		slice = new ArrayList<String>(users);
		
		ListModel m = new AbstractListModel(){
			@Override
			public Object getElementAt(int arg0) {
				return slice.get(arg0);
			}

			@Override
			public int getSize() {
				return slice.size();
			}
		};
		this.setModel(m);
	}
	
	
	
	private class SharedListSelectionHandler implements ListSelectionListener {
		SelectHook hook;
		
		public SharedListSelectionHandler(SelectHook hook){
			this.hook = hook;
		}
		
		public void valueChanged(ListSelectionEvent e) {
			ListSelectionModel lsm = (ListSelectionModel) e.getSource();

			if (lsm.isSelectionEmpty()) {

			} else {
				for (int r = lsm.getMinSelectionIndex(); r <= lsm.getMaxSelectionIndex(); r++){
					if (lsm.isSelectedIndex(r)){
						hook.select(slice.get(r));
					}		
				}
			}
		}
	}
}
