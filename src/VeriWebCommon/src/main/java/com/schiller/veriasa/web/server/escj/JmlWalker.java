package com.schiller.veriasa.web.server.escj;

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
import org.multijava.mjc.JBinaryExpression;
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

/**
 * Walk a JML expression. Stops if it doesn't recognize the JML
 * expression type (as opposed to throwing a not implemented exception)
 * @author Todd Schiller
 */
public abstract class JmlWalker extends JmlVisitorNI{

	private void visitUnary(JUnaryExpression expr){
		expr.expr().accept(this);
	}
	
	private void visitBinary(JBinaryExpression expr){
		expr.left().accept(this);
		expr.right().accept(this);
	}
	
	@Override
	public void visitNullLiteral(JNullLiteral expr) {
	}
	
	@Override
	public void visitJmlResultExpression(JmlResultExpression expr){
	}
	
	@Override
	public void visitJmlSpecExpression(JmlSpecExpression expr){
		expr.expression().accept(this);
	}
	
	@Override
	public void visitJmlOldExpression(JmlOldExpression x){
		x.specExpression().accept(this);
	}
	
	@Override
	public void visitNameExpression(JNameExpression expr){
		if (expr.getPrefix() != null){
			expr.getPrefix().accept(this);
		}
	}
	
	@Override
	public void visitUnaryExpression(JUnaryExpression x){
		visitUnary(x);
	}
	
	@Override
	public void visitOrdinalLiteral(JOrdinalLiteral x){
	}
	
	@Override
	public void visitMethodCallExpression(JMethodCallExpression x){
		if (x.prefix() != null){
			x.prefix().accept(this);	
		}
		
		for (JExpression arg : x.args()){
			arg.accept(this);
		}
	}
	
	@Override
	public void visitJmlRelationalExpression(JmlRelationalExpression x){
		visitBinary(x);
	}

	@Override
	public void visitMinusExpression(JMinusExpression x) {
		visitBinary(x);
	}
		
	@Override
	public void visitEqualityExpression(JEqualityExpression x) {
		visitBinary(x);
	}
	
	@Override
	public void visitMultExpression(JMultExpression x) {
		visitBinary(x);
	}
	
	@Override
	public void visitAddExpression(JAddExpression x) {
		visitBinary(x);
	}
	
	@Override
	public void visitAssignmentExpression(JAssignmentExpression x) {
		visitBinary(x);
	}

	
	@Override
	public void visitArrayAccessExpression(JArrayAccessExpression x){
		x.prefix().accept(this);
		x.accessor().accept(this);
	}
	
	@Override
	public void visitJmlSpecQuantifiedExpression(JmlSpecQuantifiedExpression x){
		if (x.predicate() != null){
			x.predicate().accept(this);
		}
		x.specExpression().accept(this);
	}
	
	@Override
	public void visitBooleanLiteral(JBooleanLiteral expr) {
	}
	
	@Override
	public void visitThisExpression(JThisExpression expr) {
	}
	
	@Override
	public void visitConditionalAndExpression(JConditionalAndExpression expr) {
		visitBinary(expr);
	}

	@Override
	public void visitConditionalOrExpression(JConditionalOrExpression expr) {
		visitBinary(expr);
	}
	
	@Override
	public void visitParenthesedExpression(JParenthesedExpression expr) {
		expr.expr().accept(this);
	}

	@Override
	public void visitJmlTypeExpression(JmlTypeExpression expr) {
	}

	@Override
	public void visitJmlTypeOfExpression(JmlTypeOfExpression expr) {
		// TODO: recurse
	}

	@Override
	public void visitJmlPredicate(JmlPredicate expr) {
		expr.specExpression().accept(this);
	}

	@Override
	public void visitJmlElemTypeExpression(JmlElemTypeExpression expr) {
	}

	@Override
	public void visitPostfixExpression(JPostfixExpression expr) {
		expr.expr().accept(this);
	}

	@Override
	public void visitPrefixExpression(JPrefixExpression expr) {
		expr.expr().accept(this);
	}

	@Override
	protected void imp(String method, Object self) {
	}
}
