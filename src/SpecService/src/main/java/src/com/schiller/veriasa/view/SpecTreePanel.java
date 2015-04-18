package com.schiller.veriasa.view;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;

import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

import com.schiller.veriasa.web.shared.core.*;

/**
 * View a simple specification as a tree
 * @author Todd Schiller
 */
public class SpecTreePanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private JTree specTree = new JTree();
	
	public interface Stylizer{
		Color method(String signature, Position position, Clause clause);
		Color type(String signature, Clause spec);
	}
	
	private Stylizer style;
	
	public enum Position {REQUIRES, ENSURES, EXSURES};
	
	public SpecTreePanel(){
		super(new BorderLayout());
		add(specTree,BorderLayout.LINE_START);
		setBackground(Color.WHITE);
		specTree.setCellRenderer(render);
		style = DEFAULT;
	}
	
	public SpecTreePanel(TypeSpecification spec, Stylizer style){
		this();
		TreeModel m = new DefaultTreeModel(forType(spec));
		specTree.setModel(m);
		for (int i = 0; i < specTree.getRowCount(); i++) {
			specTree.expandRow(i);
		}
		this.style = style;
		repaint();
	}
	
	public void setSpec(ProjectSpecification spec){
		TreeModel m = new DefaultTreeModel(forProject(spec));
		specTree.setModel(m);
		
		for (int i = 0; i < specTree.getRowCount(); i++) {
			specTree.expandRow(i);
		}
		repaint();
	}
	
	
	private DefaultMutableTreeNode forProject(ProjectSpecification spec){
		DefaultMutableTreeNode top = new DefaultMutableTreeNode(spec.getName());
		for (TypeSpecification ts : spec.getTypeSpecs()){
			if (!ts.getFullyQualifiedName().contains("Check")){
				top.add(forType(ts));
			}
		}
		return top;
	}
	
	public static final Stylizer DEFAULT=  new Stylizer(){
		private Color status(Clause spec){
			switch(spec.getStatus()){
			case KNOWN_GOOD:
				return Color.decode("#348017");	
			case PENDING:
				return Color.YELLOW;
			case KNOWN_BAD:
			case SYNTAX_BAD:
				return Color.RED;
			default:
				return Color.BLACK;
			}
		}
		@Override
		public Color method(String signature, Position clause, Clause spec) {
			return status(spec);
		}
		@Override
		public Color type(String signature, Clause spec) {
			return status(spec);
		}
	};

	private final DefaultTreeCellRenderer render = new DefaultTreeCellRenderer(){
		private static final long serialVersionUID = 6332706960311157853L;

		@Override
		public Component getTreeCellRendererComponent(JTree pTree,
				Object pValue, boolean pIsSelected, boolean pIsExpanded,
				boolean pIsLeaf, int pRow, boolean pHasFocus)
		{
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)pValue;
			super.getTreeCellRendererComponent(pTree, pValue, pIsSelected,
					pIsExpanded, pIsLeaf, pRow, pHasFocus);
			
			if (node instanceof MethodSpecNode){
				MethodSpecNode s = (MethodSpecNode) node;
				this.setForeground(style.method(s.qualifiedSignature(), s.getClause(), s.getSpec()));
			}else{
				this.setForeground(Color.BLACK);
			}
			return this;
		}
	};
	
	private static class TypeSpecNode extends DefaultMutableTreeNode{
		private static final long serialVersionUID = 1L;
		private final String type;
		private final Clause spec;
		
		public TypeSpecNode(String type, Clause spec) {
			super(spec.getClause());
			this.type = type;
			this.spec = spec;
		}
		
		@SuppressWarnings("unused")
		public String getType() {
			return type;
		}

		@SuppressWarnings("unused")
		public Clause getSpec() {
			return spec;
		}
	}
	
	private static class MethodSpecNode extends DefaultMutableTreeNode implements HasQualifiedSignature{
		private static final long serialVersionUID = 1L;
		
		private final String method;
		private final Clause spec;
		private final Position clause;
		
		public MethodSpecNode(String method, Position clause, Clause spec){
			super(spec.getClause());
			this.spec = spec;
			this.method = method;
			this.clause = clause;
		}
		
		public Clause getSpec() {
			return spec;
		}
		
		@Override
		public String qualifiedSignature() {
			return method;
		}
		/**
		 * @return the clause
		 */
		public Position getClause() {
			return clause;
		}
	}
	

	private DefaultMutableTreeNode forMethod(MethodContract spec){
		DefaultMutableTreeNode top = new DefaultMutableTreeNode(spec.getSignature());
		
		DefaultMutableTreeNode reqs = new DefaultMutableTreeNode("Requires");
		for (Clause s : spec.getRequires()){
			reqs.add(new MethodSpecNode(spec.qualifiedSignature(),Position.REQUIRES,s));
		}
		DefaultMutableTreeNode ens = new DefaultMutableTreeNode("Ensures");
		for (Clause s : spec.getEnsures()){
			ens.add(new MethodSpecNode(spec.qualifiedSignature(),Position.ENSURES,s));
		}
		
		top.add(reqs);
		top.add(ens);
		
		for (String ex :spec.getExsures().keySet()){
			DefaultMutableTreeNode exs = new DefaultMutableTreeNode(ex);
			for (Clause s : spec.getExsures().get(ex)){
				exs.add(new MethodSpecNode(spec.qualifiedSignature(),Position.EXSURES,s));
			}
			top.add(exs);
		}
		return top;
	}
	
	private DefaultMutableTreeNode forType(TypeSpecification spec){
		DefaultMutableTreeNode top = new DefaultMutableTreeNode(spec.getFullyQualifiedName());
		for (Clause s : spec.getInvariants()){
			top.add(new TypeSpecNode(spec.getFullyQualifiedName(), s));
		}	
		for (MethodContract fs : spec.getMethods()){
			top.add(forMethod(fs));
		}
		return top;
	}
}
