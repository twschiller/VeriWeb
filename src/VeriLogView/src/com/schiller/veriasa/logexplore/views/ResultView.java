package com.schiller.veriasa.logexplore.views;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import com.schiller.veriasa.web.shared.escj.Chunk;
import com.schiller.veriasa.web.shared.escj.MethodResult;
import com.schiller.veriasa.web.shared.escj.ProjectResult;
import com.schiller.veriasa.web.shared.escj.TypeResult;

public class ResultView extends JTable{

	private static final long serialVersionUID = -2253808269098214990L;

	private List<Chunk> allWarnings = new ArrayList<Chunk>();
	
	public ResultView(ProjectResult result ){
		super();
		
		allWarnings.addAll(result.getSpecErrors());
		
		for (TypeResult typeResult : result.getTypeResults()){
			allWarnings.addAll(typeResult.getWarnings());
			for (MethodResult methodResult : typeResult.getMethodResults()){
				allWarnings.addAll(methodResult.getWarnings());
			}	
		}
		
		buildTable();
	
	}
	
	public int getNumErrors(){
		return allWarnings.size();
	}
	
	private void buildTable(){
		// 1 - msg
		// 2 - user 
		// 3 - action type
		// 4 - problem
		// 5 - problem type
		
		TableModel t = new AbstractTableModel(){
			private static final long serialVersionUID = 1L;

			@Override
			public int getColumnCount() {
				return 5;
			}

			@Override
			public int getRowCount() {
				return allWarnings.size();
			}

			@Override
			public Object getValueAt(int arg0, int arg1) {
				Chunk e = allWarnings.get(arg0);
				switch (arg1){
				case 0:
					return e.getFilePath();
				case 1:
					return e.getLine();
				case 2:		
					return e.getMessage();
				case 3:
					return e.getBadLine();
				case 4:
					if (e.getAssociatedDeclaration() != null){
						return e.getAssociatedDeclaration().getContents();
					}else{
						return null;
					}
				default:
					return null;
				}
			}
			
		};
		
		setModel(t);
		getTableHeader().getColumnModel().getColumn(0).setHeaderValue("File");
		getTableHeader().getColumnModel().getColumn(1).setHeaderValue("Line #");
		getTableHeader().getColumnModel().getColumn(2).setHeaderValue("Error");
		getTableHeader().getColumnModel().getColumn(3).setHeaderValue("Line");
		getTableHeader().getColumnModel().getColumn(4).setHeaderValue("Associated");
	}
}
