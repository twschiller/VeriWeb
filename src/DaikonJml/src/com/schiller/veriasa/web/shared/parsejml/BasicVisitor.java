package com.schiller.veriasa.web.shared.parsejml;

import org.jmlspecs.checker.JmlElemTypeExpression;
import org.jmlspecs.checker.JmlOldExpression;
import org.jmlspecs.checker.JmlPredicate;
import org.jmlspecs.checker.JmlRelationalExpression;
import org.jmlspecs.checker.JmlResultExpression;
import org.jmlspecs.checker.JmlSpecExpression;
import org.jmlspecs.checker.JmlSpecQuantifiedExpression;
import org.jmlspecs.checker.JmlTypeExpression;
import org.jmlspecs.checker.JmlTypeOfExpression;
import org.jmlspecs.checker.JmlVisitorNI;
import org.multijava.mjc.JAddExpression;
import org.multijava.mjc.JArrayAccessExpression;
import org.multijava.mjc.JAssignmentExpression;
import org.multijava.mjc.JBooleanLiteral;
import org.multijava.mjc.JConditionalAndExpression;
import org.multijava.mjc.JConditionalOrExpression;
import org.multijava.mjc.JEqualityExpression;
import org.multijava.mjc.JExpression;
import org.multijava.mjc.JMethodCallExpression;
import org.multijava.mjc.JMinusExpression;
import org.multijava.mjc.JMultExpression;
import org.multijava.mjc.JNameExpression;
import org.multijava.mjc.JNullLiteral;
import org.multijava.mjc.JOrdinalLiteral;
import org.multijava.mjc.JParenthesedExpression;
import org.multijava.mjc.JPostfixExpression;
import org.multijava.mjc.JPrefixExpression;
import org.multijava.mjc.JThisExpression;
import org.multijava.mjc.JUnaryExpression;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

public class BasicVisitor extends JmlVisitorNI {
	
	protected JExpression expr;
	private final BasicVisitorFactory factory;
	
	public interface BasicVisitorFactory{
		BasicVisitor build();
	}
	
	protected BasicVisitor(BasicVisitorFactory factory){
		this.factory = factory;
	}
	
	public JExpression getExpression(){
		return expr;
	}
	
	protected final Function<JExpression, JExpression> vizF = new Function<JExpression,JExpression>(){
		@Override
		public JExpression apply(JExpression arg0) {
			return viz(arg0);
		}
	};
	
	protected JExpression viz(JExpression x){
		if (x == null){
			return null;
		}else{
			BasicVisitor v = this.factory.build();
			x.accept(v);
			return v.getExpression();
		}
	}
	
	@Override
	public void visitNullLiteral(JNullLiteral arg0) {
		expr = arg0;
	}
	
	@Override
	public void visitJmlResultExpression(JmlResultExpression r){
		expr = r;
	}
	
	@Override
	public void visitJmlSpecExpression(JmlSpecExpression r){
		r.expression().accept(this);
	}
	
	@Override
	public void visitJmlOldExpression(JmlOldExpression x){
		expr = new JmlOldExpression(x.getTokenReference(),new JmlSpecExpression(viz(x.specExpression())), null);
	}
	
	@Override
	public void visitNameExpression(JNameExpression arg0){
		expr = arg0.getPrefix() != null 
			? new JNameExpression(arg0.getTokenReference(),viz(arg0.getPrefix()),arg0.getName()) 
			: new JNameExpression(arg0.getTokenReference(),arg0.getName()) ;
	}
	

	@Override
	public void visitUnaryExpression(JUnaryExpression x){
		expr = new JUnaryExpression(x.getTokenReference(),x.oper(), viz(x.expr()));	
	}
	
	@Override
	public void visitOrdinalLiteral(JOrdinalLiteral x){
		expr = x;
	}
	
	@Override
	public void visitMethodCallExpression(JMethodCallExpression x){
		
		expr = new JMethodCallExpression(
				x.getTokenReference(),
				viz(x.prefix()),
				x.ident(),
				Collections2.transform(Lists.newArrayList(x.args()), vizF).toArray(new JExpression[]{}),
				false);
	}
	
	@Override
	public void visitJmlRelationalExpression(JmlRelationalExpression x){
		expr = new JmlRelationalExpression(x.getTokenReference(), x.oper(), viz(x.left()),viz(x.right()));
	}

	@Override
	public void visitMinusExpression(JMinusExpression x) {
		expr = new JMinusExpression(x.getTokenReference(), viz(x.left()),viz(x.right()));
	}
		
	@Override
	public void visitEqualityExpression(JEqualityExpression x) {
		expr = new JEqualityExpression(x.getTokenReference(), x.oper(), viz(x.left()),viz(x.right()));
	}
	
	@Override
	public void visitMultExpression(JMultExpression x) {
		expr = new JMultExpression(x.getTokenReference(), viz(x.left()), viz(x.right()));
	}
	
	@Override
	public void visitAddExpression(JAddExpression x) {
		expr = new JAddExpression(x.getTokenReference(), viz(x.left()), viz(x.right()));
	}
	
	@Override
	public void visitAssignmentExpression(JAssignmentExpression x) {
		expr = new JAssignmentExpression(x.getTokenReference(), viz(x.left()),viz(x.right()));
	}

	
	@Override
	public void visitArrayAccessExpression(JArrayAccessExpression x){
		expr = new JArrayAccessExpression(x.getTokenReference(), viz(x.prefix()), viz(x.accessor()));
	}
	
	@Override
	public void visitJmlSpecQuantifiedExpression(JmlSpecQuantifiedExpression x){
		expr = new JmlSpecQuantifiedExpression(
				x.getTokenReference(), 
				x.oper(), 
				x.quantifiedVarDecls(),
				x.predicate() != null ? new JmlPredicate(new JmlSpecExpression(viz(x.predicate()))) : null,
						new JmlSpecExpression(viz(x.specExpression())));
	}
	
	@Override
	public void visitBooleanLiteral(JBooleanLiteral arg0) {
		expr = arg0;
	}
	@Override
	public void visitThisExpression(JThisExpression arg0) {
		expr = arg0;
	}
	
	@Override
	public void visitConditionalAndExpression(JConditionalAndExpression arg0) {
		expr = new JConditionalAndExpression(arg0.getTokenReference(),viz(arg0.left()), viz(arg0.right()));	
	}

	@Override
	public void visitConditionalOrExpression(JConditionalOrExpression arg0) {
		expr = new JConditionalOrExpression(arg0.getTokenReference(),viz(arg0.left()), viz(arg0.right()));	
	}
	
	@Override
	public void visitParenthesedExpression(JParenthesedExpression arg0) {
		expr = new JParenthesedExpression(arg0.getTokenReference(), viz(arg0.expr()));
	}

	@Override
	public void visitJmlTypeExpression(JmlTypeExpression arg0) {
		expr = arg0;
	}

	@Override
	public void visitJmlTypeOfExpression(JmlTypeOfExpression arg0) {
		//TODO: recurse
		expr = arg0;
	}


	@Override
	public void visitJmlPredicate(JmlPredicate arg0) {
		expr = viz(arg0.specExpression());
	}

	@Override
	public void visitJmlElemTypeExpression(JmlElemTypeExpression arg0) {
		expr = arg0;
	}

	
	@Override
	public void visitPostfixExpression(JPostfixExpression arg0) {
		expr = new JPostfixExpression(arg0.getTokenReference(), arg0.oper(), viz(arg0.expr()));
	}


	@Override
	public void visitPrefixExpression(JPrefixExpression arg0) {
		expr = new JPrefixExpression(arg0.getTokenReference(), arg0.oper(), viz(arg0.expr()));
	}
}
