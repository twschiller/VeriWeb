package com.schiller.veriasa.logexplore;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import com.schiller.veriasa.logexplore.util.ViewUtil;
import com.schiller.veriasa.web.server.User;
import com.schiller.veriasa.web.server.logging.LogEntry;
import com.schiller.veriasa.web.server.logging.UserAction;
import com.schiller.veriasa.web.shared.logging.HasProblem;
import com.schiller.veriasa.web.shared.problems.MethodProblem;


@SuppressWarnings("serial")
public class ActionTable extends JTable{

	private List<LogEntry> entries;
	
	public static interface SelectionHook{
		void select(LogEntry entry);
	}
	
	public ActionTable(List<LogEntry> entries, SelectionHook hook){
		this.entries = new ArrayList<LogEntry>(entries);
		this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		if (hook != null){
			this.getSelectionModel().addListSelectionListener(new SharedListSelectionHandler(hook));
		}
		buildTable();
	}
	

	private class SharedListSelectionHandler implements ListSelectionListener {
		SelectionHook hook;
		
		public SharedListSelectionHandler(SelectionHook hook){
			this.hook = hook;
		}
		
		public void valueChanged(ListSelectionEvent e) {
			ListSelectionModel lsm = (ListSelectionModel) e.getSource();

			if (lsm.isSelectionEmpty()) {

			} else {
				for (int r = lsm.getMinSelectionIndex(); r <= lsm.getMaxSelectionIndex(); r++){
					if (lsm.isSelectedIndex(r)){
						hook.select(entries.get(r));
					}		
				}
			}
		}

	}
	
	public void update(List<LogEntry> entries){
		this.entries = new ArrayList<LogEntry>(entries);
		buildTable();
	}
	
	
	private void buildTable(){
		// 1 - timestamp
		// 2 - user 
		// 3 - action type
		// 4 - problem
		// 5 - problem type
		
		TableModel t = new AbstractTableModel(){

			@Override
			public int getColumnCount() {
				return 5;
			}

			@Override
			public int getRowCount() {
				return entries.size();
			}

			@Override
			public Object getValueAt(int arg0, int arg1) {
				LogEntry e = entries.get(arg0);
				switch (arg1){
				case 0:
					
					return ViewUtil.formatTimestamp(e.getTimestamp());
					//return DateFormat.getDateInstance(DateFormat.FULL,Locale.ENGLISH).format());
				case 1:
					if (e.getAction() instanceof UserAction){
						User u = ((UserAction) e.getAction()).getUser();
						return ViewUtil.userId(u);
					}else{
						return null;
					}
				
				case 2:
					if (e.getAction() instanceof HasProblem){
						HasProblem hp = (HasProblem) e.getAction();
						MethodProblem fp = (MethodProblem) hp.getProblem();
						
						return fp.getFunction().getSignature();
					}else{
						return null;
					}
				case 3:
					if (e.getAction() instanceof HasProblem){
						HasProblem hp = (HasProblem) e.getAction();
						MethodProblem fp = (MethodProblem) hp.getProblem();
						
						return fp.getClass().getSimpleName();
					}else{
						return null;
					}
				case 4:
					return e.getAction().getClass().getSimpleName();
				default:
					return null;
				}
			}
			
		};
		
		setModel(t);
		getTableHeader().getColumnModel().getColumn(0).setHeaderValue("Timestamp");
		getTableHeader().getColumnModel().getColumn(1).setHeaderValue("User");
		getTableHeader().getColumnModel().getColumn(2).setHeaderValue("Problem");
		getTableHeader().getColumnModel().getColumn(3).setHeaderValue("Problem Type");
		getTableHeader().getColumnModel().getColumn(4).setHeaderValue("Action Type");
	}
}
