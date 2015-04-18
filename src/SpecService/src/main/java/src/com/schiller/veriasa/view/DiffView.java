package com.schiller.veriasa.view;

import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import com.schiller.veriasa.distance.MethodDistance;
import com.schiller.veriasa.distance.TypeDistance;
import com.schiller.veriasa.view.SpecTreePanel.Position;
import com.schiller.veriasa.view.SpecTreePanel.Stylizer;
import com.schiller.veriasa.web.shared.core.Clause;
import com.schiller.veriasa.web.shared.core.TypeSpecification;

/**
 * View the difference between two VeriWeb specifications
 * @author Todd Schiller
 */
public class DiffView extends JFrame {
	private static final long serialVersionUID = 2L;
	private final JSplitPane splitPane;

	public DiffView(TypeSpecification left, TypeSpecification right, final TypeDistance diff){
		 setTitle("View Diff");
		 this.setSize(800, 500);
		 
		 Stylizer leftStyle = new Stylizer(){
			@Override
			public Color method(String signature, Position position, Clause clause) {
				MethodDistance md = diff.getMethods().get(signature);
				
				if (md == null){
					return Color.BLACK;
				}
				
				String s = clause.getClause();
				switch (position){
				case REQUIRES:
					if (md.getRequires().getRemoved().contains(s)){
						return Color.RED;
					}
					break;
				case ENSURES:
					if (md.getEnsures().getRemoved().contains(s)){
						return Color.RED;
					}
					break;
				case EXSURES:
					if (md.getExsures().getRemoved().contains(s)){
						return Color.RED;
					}
					break;
				};
				if (clause.getStatus() != Clause.Status.KNOWN_GOOD){
					return Color.GRAY;
				}else{
					return Color.BLACK;
				}
			}

			@Override
			public Color type(String signature, Clause clause) {
				return (diff.getInvariants() != null && diff.getInvariants().getRemoved().contains(clause.getClause())) 
					? Color.RED : Color.BLACK;
			} 
		 };
		 
		 Stylizer rightStyle = new Stylizer(){
				@Override
				public Color method(String signature, Position clause, Clause spec) {
					MethodDistance md = diff.getMethods().get(signature);
					
					if (md == null){
						return Color.BLACK;
					}
	
					String s = spec.getClause();
					switch (clause){
					case REQUIRES:
						if (md.getRequires().getAdded().contains(s)){
							return Color.RED;
						}
						break;
					case ENSURES:
						if (md.getEnsures().getAdded().contains(s)){
							return Color.RED;
						}
						break;

					case EXSURES:
						if (md.getExsures().getAdded().contains(s)){
							return Color.RED;
						}
						break;

					};
					return Color.BLACK;
				}

				@Override
				public Color type(String signature, Clause clause) {
					return (diff.getInvariants() != null && diff.getInvariants().getAdded().contains(clause.getClause())) 
						? Color.RED : Color.BLACK;
				} 
			 };
		 
		 SpecTreePanel leftTree = new SpecTreePanel(left, leftStyle);
		 SpecTreePanel rightTree = new SpecTreePanel(right, rightStyle);
		 
		 JScrollPane ls = new JScrollPane(leftTree);
		 JScrollPane rs = new JScrollPane(rightTree);
		
		 splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, ls, rs);
		
		 getContentPane().add(splitPane);
		 splitPane.setDividerLocation(400);
		 ls.repaint();
		 rs.repaint();
	}
	
}
