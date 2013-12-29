package com.schiller.veriasa.executejml;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
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
import org.multijava.mjc.Constants;
import org.multijava.mjc.JAddExpression;
import org.multijava.mjc.JArrayAccessExpression;
import org.multijava.mjc.JAssignmentExpression;
import org.multijava.mjc.JBinaryExpression;
import org.multijava.mjc.JBitwiseExpression;
import org.multijava.mjc.JBooleanLiteral;
import org.multijava.mjc.JCastExpression;
import org.multijava.mjc.JCompoundAssignmentExpression;
import org.multijava.mjc.JConditionalAndExpression;
import org.multijava.mjc.JConditionalOrExpression;
import org.multijava.mjc.JDivideExpression;
import org.multijava.mjc.JEqualityExpression;
import org.multijava.mjc.JExpression;
import org.multijava.mjc.JMethodCallExpression;
import org.multijava.mjc.JMinusExpression;
import org.multijava.mjc.JModuloExpression;
import org.multijava.mjc.JMultExpression;
import org.multijava.mjc.JNameExpression;
import org.multijava.mjc.JNullLiteral;
import org.multijava.mjc.JOrdinalLiteral;
import org.multijava.mjc.JParenthesedExpression;
import org.multijava.mjc.JPostfixExpression;
import org.multijava.mjc.JPrefixExpression;
import org.multijava.mjc.JThisExpression;
import org.multijava.mjc.JUnaryExpression;

import com.schiller.veriasa.web.shared.parsejml.JmlSpan;
import com.schiller.veriasa.web.shared.parsejml.MultiSpan;
import com.schiller.veriasa.web.shared.parsejml.SingleSpan;

public class SpanParser extends JmlVisitorNI{
	private static Logger log = Logger.getLogger(SpanParser.class);
	
	public JmlSpan fragment;
	
	private SpanParser(){	
	}
	
	public static JmlSpan build(JmlSpecExpression expr){
		return build(expr.expression());
	}
	
	public static JmlSpan build(JExpression x){
		SpanParser v = new SpanParser();
		x.accept(v);
		return v.fragment;
	}
	
	private static String qualifyName(JNameExpression n){
		String qn = n.getName();
		
		JExpression pr = n.getPrefix();
		while (pr != null){
			if (pr instanceof JThisExpression){
				qn = "this." + qn;
				break;
			}else if (pr instanceof JNameExpression){
				qn = ((JNameExpression) pr).getName() + "." + qn;
				pr = ((JNameExpression) pr).getPrefix();
			}else{
				throw new RuntimeException("Unexpected expression " + pr);
			}
		}
		return qn;
	}
	
	private void visitUnary(String op, JUnaryExpression x){
		fragment = new MultiSpan(
				new JmlSpan[]{
						new SingleSpan(op,true),
						build(x.expr()),
				});
	}
	
	private void visitBinary(String op, JBinaryExpression x){
		fragment = new MultiSpan(
				new JmlSpan[]{
						build(x.left()),
						new SingleSpan(" " + op + " ",true),
						build(x.right()),
		});
	}
	
	@Override
	public void visitNullLiteral(JNullLiteral arg0) {
		fragment = new SingleSpan("null",false);
	}
	
	@Override
	public void visitJmlResultExpression(JmlResultExpression r){
		fragment = new SingleSpan("\\result",false);
	}
	
	@Override
	public void visitJmlOldExpression(JmlOldExpression x){
		fragment = new MultiSpan(new JmlSpan[]{
				new SingleSpan("\\old(",true),
				build(x.specExpression()),
				new SingleSpan(")",true)
		});
	}
	
	@Override
	public void visitNameExpression(JNameExpression arg0){
		fragment = new SingleSpan(qualifyName(arg0),false);
	}
	
	@Override
	public void visitMethodCallExpression(JMethodCallExpression e){

		List<JmlSpan> rs = new ArrayList<JmlSpan>();		
		for (JExpression ex : e.args()){
			rs.add(build(ex));
		}
		
		List<JmlSpan> f = new ArrayList<JmlSpan>();
		
		if (e.prefix() instanceof JNameExpression){
			f.add(new SingleSpan(qualifyName((JNameExpression) e.prefix()) + "." + e.ident() + (rs.size() == 0 ? "()" : "("), true));
		}else if(e.prefix() != null){
			JmlSpan r  = build(e.prefix());
			f.add(r);
			f.add(new SingleSpan("." + e.ident() + (rs.size() == 0 ? "()" : "("), true));
		}else{
			f.add(new SingleSpan(e.ident() + (rs.size() == 0 ? "()" : "("), true));
		}
		
		if (rs.size() > 0){
			f.add(rs.get(0));
			for (int j = 1; j < rs.size(); j++){
				f.add(new SingleSpan(",",true));
				f.add(rs.get(j));
			}
			f.add(new SingleSpan(")",true));
		}
		fragment = new MultiSpan(f.toArray(new JmlSpan[]{}));	
	}
	

	@Override
	public void visitUnaryExpression(JUnaryExpression x){
		if (x.oper() == Constants.OPE_MINUS){
			if (x.expr().isLiteral()){
				fragment = new SingleSpan("-" + x.expr().toString(),false);
			}else{
				visitUnary("-",x);
			}
		}else if(x.oper() == Constants.OPE_LNOT){
			visitUnary("!",x);
		}else{
			throw new UnsupportedOperationException();
		}
		
	}
	
	@Override
	public void visitOrdinalLiteral(JOrdinalLiteral x){
		fragment = new SingleSpan(x.toString(),false);
	}
	
	@Override
	public void visitJmlRelationalExpression(JmlRelationalExpression x){
		visitBinary(x.opString(),x);
	}
	
	@Override
	public void visitMultExpression(JMultExpression arg0) {
		visitBinary("*",arg0);
	}
	
	@Override
	public void visitMinusExpression(JMinusExpression arg0) {
		visitBinary("-",arg0);
	}
	
	@Override
	public void visitModuloExpression(JModuloExpression arg0) {
		visitBinary("%",arg0);
	}
	
	@Override
	public void visitEqualityExpression(JEqualityExpression arg0) {
		if (arg0.oper() == JEqualityExpression.OPE_EQ){
			visitBinary("==",arg0);
		}else if (arg0.oper() == JEqualityExpression.OPE_NE){
			visitBinary("!=",arg0);
		}else{
			throw new RuntimeException("Unexpected equality expression " + arg0);
		}
	}
	
	@Override
	public void visitAddExpression(JAddExpression arg0) {
		visitBinary("+",arg0);
	}
	
	@Override
	public void visitAssignmentExpression(JAssignmentExpression arg0) {
		visitBinary("=",arg0);
	}

	@Override
	public void visitDivideExpression(JDivideExpression arg0) {
		visitBinary("/",arg0);
	}
	
	
	@Override
	public void visitPostfixExpression(JPostfixExpression arg0) {
		throw new UnsupportedOperationException("Postfix expressions are not supported");
	}

	@Override
	public void visitPrefixExpression(JPrefixExpression arg0) {
		throw new UnsupportedOperationException("Prefix expressions are not supported");
	}
	
	@Override
	public void visitArrayAccessExpression(JArrayAccessExpression x){
		if (x.prefix() instanceof JNameExpression){
			fragment = new MultiSpan(new JmlSpan[]{
					new SingleSpan(qualifyName((JNameExpression)x.prefix()) + "[",true),
					build(x.accessor()),
					new SingleSpan("]",true)
			});
		}else{
			fragment = new MultiSpan(new JmlSpan[]{
					build(x.prefix()),
					new SingleSpan("[",true),
					build(x.accessor()),
					new SingleSpan("]",true)
			});
		}		
	}
	
	@Override
	public void visitJmlSpecQuantifiedExpression(JmlSpecQuantifiedExpression x){
		
		if (x.isForAll()){
		
			StringBuilder sb = new StringBuilder();
			sb.append(x.quantifiedVarDecls()[0].ident());
			
			for (int i=1; i < x.quantifiedVarDecls().length; i++){
				sb.append(", ").append(x.quantifiedVarDecls()[i].ident());
			}
			
			if (x.predicate() != null){
				fragment = new MultiSpan(new JmlSpan[]{
						new SingleSpan("(\\forall int " + sb.toString() + ";",true),
						build(x.predicate().specExpression()),
						new SingleSpan(";",true),
						build(x.specExpression()),
						new SingleSpan(")",true)
				});
			}else{
				fragment = new MultiSpan(new JmlSpan[]{
						new SingleSpan("(\\forall int " + sb.toString() + ";",true),
						build(x.specExpression()),
						new SingleSpan(")",true)
				});
			}
		}else{
			throw new UnsupportedOperationException("Unknown quantified expression");
		}
	}

	@Override
	public void visitBooleanLiteral(JBooleanLiteral arg0) {
		fragment = new SingleSpan(arg0.booleanValue() ? "true" : "false", false);
	}
	
	@Override
	public void visitThisExpression(JThisExpression arg0) {
		fragment = new SingleSpan("this", false);
	}
	
	@Override
	public void visitConditionalAndExpression(JConditionalAndExpression arg0) {
		visitBinary("&&",arg0);
	}

	@Override
	public void visitConditionalOrExpression(JConditionalOrExpression arg0) {
		visitBinary("||",arg0);
	}
	
	@Override
	public void visitParenthesedExpression(JParenthesedExpression arg0) {
		JmlSpan xx = build(arg0.expr());
		
		List<JmlSpan> l = new ArrayList<JmlSpan>();
		l.add(new SingleSpan("(",true));
		l.addAll(xx.allSubFragments());
		l.add(new SingleSpan(")",true));
		
		fragment = new MultiSpan(l.toArray(new JmlSpan[]{}));
	}

	@Override
	public void visitJmlPredicate(JmlPredicate arg0) {
		arg0.specExpression().accept(this);
	}

	@Override
	public void visitJmlSpecExpression(JmlSpecExpression arg0) {
		arg0.expression().accept(this);
	}

	@Override
	public void visitJmlTypeOfExpression(JmlTypeOfExpression arg0) {
		fragment = new MultiSpan(new JmlSpan[]{
				new SingleSpan("\\typeof(",true),
				build(arg0.specExpression().expression()),
				new SingleSpan(")",true)
		});	
	}
	
	@Override
	public void visitJmlTypeExpression(JmlTypeExpression arg0) {
		fragment = new MultiSpan(new JmlSpan[]{
				new SingleSpan("\\type(",true),
				new SingleSpan("java.lang.Object[]",false),
				new SingleSpan(")",true),
		});
		log.trace("assuming type expression is \\type(java.lang.Object[])");
	}

	@Override
	public void visitJmlElemTypeExpression(JmlElemTypeExpression arg0) {
		fragment = new MultiSpan(new JmlSpan[]{
				new SingleSpan("\\elemtype(",true),
				build(arg0.specExpression().expression()),
				new SingleSpan(")",true)
		});
	}

	@Override
	public void visitBitwiseExpression(JBitwiseExpression arg0) {
		throw new UnsupportedOperationException("Bitwise expressions are not supported");
	}

	@Override
	public void visitCompoundAssignmentExpression(JCompoundAssignmentExpression arg0) {
		throw new UnsupportedOperationException("Compound assignment expressions are not supported");
	}

	@Override
	public void visitCastExpression(JCastExpression arg0) {
		throw new UnsupportedOperationException("Cast expressions are not supported");
	}	
}
