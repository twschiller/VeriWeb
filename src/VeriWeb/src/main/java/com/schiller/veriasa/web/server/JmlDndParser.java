package com.schiller.veriasa.web.server;

import java.util.ArrayList;
import java.util.List;

import org.jmlspecs.checker.JmlOldExpression;
import org.jmlspecs.checker.JmlRelationalExpression;
import org.jmlspecs.checker.JmlResultExpression;
import org.jmlspecs.checker.JmlSpecExpression;
import org.jmlspecs.checker.JmlSpecQuantifiedExpression;
import org.jmlspecs.checker.JmlVisitorNI;
import org.multijava.mjc.Constants;
import org.multijava.mjc.JAddExpression;
import org.multijava.mjc.JArrayAccessExpression;
import org.multijava.mjc.JArrayLengthExpression;
import org.multijava.mjc.JAssignmentExpression;
import org.multijava.mjc.JBinaryExpression;
import org.multijava.mjc.JBooleanLiteral;
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
import org.multijava.mjc.JThisExpression;
import org.multijava.mjc.JUnaryExpression;

import antlr.RecognitionException;
import antlr.TokenStreamException;

import com.schiller.veriasa.executejml.ExecuteJml;
import com.schiller.veriasa.web.shared.config.JmlParseException;
import com.schiller.veriasa.web.shared.dnd.InvElement;
import com.schiller.veriasa.web.shared.dnd.InvElement.RefType;
import com.schiller.veriasa.web.shared.dnd.InvFixed;
import com.schiller.veriasa.web.shared.dnd.InvRef;

/**
 * Convert JML expressions to drag-and-drop UI elements
 * @author Todd Schiller
 */
public class JmlDndParser {
	
	public static InvElement specToFragment(String jmlStatement) throws JmlParseException{
		try {
			return JmlEltVisitor.buildElt(ExecuteJml.tryParse(SpecUtil.clean(jmlStatement)));
		} catch (RecognitionException e) {
			throw new JmlParseException(e);
		} catch (TokenStreamException e) {
			throw new JmlParseException(e);
		}
	}
	
	private static class JmlEltVisitor extends JmlVisitorNI{
		private InvElement elt;
		
		private JmlEltVisitor(){};
		
		public static InvElement buildElt(JmlSpecExpression expr){
			JmlEltVisitor v = new JmlEltVisitor();
			expr.expression().accept(v);
			return v.elt;
		}
		
		public static InvRef buildRef(JExpression x){
			JmlEltVisitor v = new JmlEltVisitor();
			x.accept(v);
			return new InvRef(v.elt);
		}
		
		private void visitUnary(String op, JUnaryExpression x){
			elt = new InvElement(new InvRef[]{
					new InvRef(new InvFixed(op, RefType.BoilerPlate)),
					buildRef(x.expr()),
			}, RefType.Expression);
		}
		
		private void visitBinary(String op, JBinaryExpression x){
			elt = new InvElement(new InvRef[]{
					buildRef(x.left()),
					new InvRef(new InvFixed(op, RefType.BoilerPlate)),
					buildRef(x.right()),
			}, RefType.Expression);
		}
		
		@Override
		public void visitNullLiteral(JNullLiteral arg0) {
			elt = new InvFixed("null", RefType.Expression);
		}
		
		@Override
		public void visitJmlOldExpression(JmlOldExpression x){
			elt = new InvElement(new InvRef[]{
					new InvRef(new InvFixed("\\old(", RefType.BoilerPlate)),
					buildRef(x.specExpression().expression()),
					new InvRef(new InvFixed(")", RefType.BoilerPlate)),
			}, RefType.Expression);
		}
		
		@Override
		public void visitMethodCallExpression(JMethodCallExpression e){
			List<InvRef> rs = new ArrayList<InvRef>();
			
			for (JExpression ex : e.args()){
				rs.add(buildRef(ex));
			}
			
			List<InvRef> f = new ArrayList<InvRef>();
			
			if(e.prefix() != null){
				InvRef r  = buildRef(e.prefix());
				f.add(r);
				f.add(new InvRef(new InvFixed("." + e.sourceName().getName() + (rs.size() == 0 ? "()" : "("), RefType.BoilerPlate)));
			}else{
				f.add(new InvRef(new InvFixed(e.sourceName().getName() + (rs.size() == 0 ? "()" : "("), RefType.BoilerPlate)));
			}
			
			if (rs.size() > 0){
				f.add(rs.get(0));
				for (int j = 1; j < rs.size(); j++){
					f.add(new InvRef(new InvFixed(",", RefType.BoilerPlate)));
					f.add(rs.get(j));
				}
				f.add(new InvRef(new InvFixed(")", RefType.BoilerPlate)));
			}
			
			elt = new InvElement(f.toArray(new InvRef[]{}), RefType.Expression);
			
		}
		
		@Override
		public void visitJmlResultExpression(JmlResultExpression r){
			elt = new InvFixed("\\result", RefType.Expression);
		}
		
		@Override
		public void visitUnaryExpression(JUnaryExpression x){
			if (x.oper() == Constants.OPE_MINUS){
				if (x.expr().isOrdinalLiteral()){
					elt = new InvFixed(
							"-" + ((JOrdinalLiteral) x.expr()).numberValue().toString()
							, RefType.Expression);
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
			elt = new InvFixed(x.numberValue().toString(), RefType.Expression);
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
		public void visitNameExpression(JNameExpression arg0){
			elt = new InvFixed(arg0.qualifiedName(), RefType.Expression);
		}
		
		@Override
		public void visitArrayAccessExpression(JArrayAccessExpression x){
			InvElement xx = buildRef(x.prefix()).getValue();
			
			if (xx.getSubElements().isEmpty()){
				elt = new InvElement(new InvRef[]{
						new InvRef(xx),
						new InvRef(new InvFixed("[",RefType.BoilerPlate)), 
						buildRef(x.accessor()),
						new InvRef(new InvFixed("]",RefType.BoilerPlate)),
				}, RefType.Expression);
			}else{
				List<InvRef> qq = new ArrayList<InvRef>();
				qq.addAll(xx.getSubElements());
				qq.add(new InvRef(new InvFixed("[",RefType.BoilerPlate)));
				qq.add(buildRef(x.accessor()));
				qq.add(new InvRef(new InvFixed("]",RefType.BoilerPlate)));
				
				elt = new InvElement(qq.toArray(new InvRef[]{}), RefType.Expression);
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
					elt = new InvElement(new InvRef[]{
							new InvRef(new InvFixed("(\\forall int "  +sb.toString() + ";",RefType.BoilerPlate)), 
							buildRef(x.predicate().specExpression().expression()),
							new InvRef(new InvFixed(";",RefType.BoilerPlate)),
							buildRef(x.specExpression().expression()),
							new InvRef(new InvFixed(")",RefType.BoilerPlate)),
					}, RefType.Expression);
				}else{
					elt = new InvElement(new InvRef[]{
							new InvRef(new InvFixed("(\\forall int "  +sb.toString() + ";",RefType.BoilerPlate)), 
							buildRef(x.specExpression().expression()),
							new InvRef(new InvFixed(")",RefType.BoilerPlate)),
					}, RefType.Expression);
				}
			}else{
				throw new UnsupportedOperationException("Unknown quantified expression");
			}
		}
		
		@Override
		public void visitBooleanLiteral(JBooleanLiteral arg0) {
			elt = new InvFixed(arg0.booleanValue() ? "true" : "false", RefType.Expression);
		}
		@Override
		public void visitThisExpression(JThisExpression arg0) {
			elt = new InvFixed("this", RefType.Expression);
		}
		
		@Override
		public void visitConditionalAndExpression(JConditionalAndExpression arg0) {
			visitBinary("&&", arg0);
		}

		@Override
		public void visitConditionalOrExpression(JConditionalOrExpression arg0) {
			visitBinary("||", arg0);
		}
		
		@Override
		public void visitParenthesedExpression(JParenthesedExpression arg0) {
			elt = new InvElement(new InvRef[]{
					new InvRef(new InvFixed("(", RefType.BoilerPlate)),
					buildRef(arg0.expr()),
					new InvRef(new InvFixed(")", RefType.BoilerPlate)),
			}, RefType.Expression);
		}

		@Override
		public void visitArrayLengthExpression(JArrayLengthExpression arg0) {
			elt = new InvElement(new InvRef[]{
					buildRef(arg0.prefix()),
					new InvRef(new InvFixed(".length", RefType.BoilerPlate)),
			}, RefType.Expression);
		}
	}
}
