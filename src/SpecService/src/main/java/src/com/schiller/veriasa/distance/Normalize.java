package com.schiller.veriasa.distance;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.jmlspecs.checker.JmlOldExpression;
import org.jmlspecs.checker.JmlRelationalExpression;
import org.jmlspecs.checker.JmlSpecExpression;
import org.jmlspecs.checker.JmlSpecQuantifiedExpression;
import org.multijava.mjc.CStdType;
import org.multijava.mjc.Constants;
import org.multijava.mjc.JAddExpression;
import org.multijava.mjc.JBooleanLiteral;
import org.multijava.mjc.JEqualityExpression;
import org.multijava.mjc.JExpression;
import org.multijava.mjc.JLiteral;
import org.multijava.mjc.JMinusExpression;
import org.multijava.mjc.JNameExpression;
import org.multijava.mjc.JOrdinalLiteral;
import org.multijava.mjc.JParenthesedExpression;
import org.multijava.mjc.JPostfixExpression;
import org.multijava.mjc.JPrefixExpression;
import org.multijava.mjc.JThisExpression;
import org.multijava.mjc.JUnaryExpression;
import org.multijava.util.compiler.TokenReference;

import antlr.RecognitionException;
import antlr.TokenStreamException;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.schiller.veriasa.executejml.ExecuteJml;
import com.schiller.veriasa.util.Convert;
import com.schiller.veriasa.web.shared.core.MethodContract;
import com.schiller.veriasa.web.shared.core.Clause;
import com.schiller.veriasa.web.shared.core.TypeSpecification;
import com.schiller.veriasa.web.shared.parsejml.BasicVisitor;

/**
 * Textually normalize JML contracts
 * @author Todd Schiller
 */
public class Normalize extends BasicVisitor{
	
	/**
	 * Known instance (member) variables. Used to insert "this"
	 */
	private static final Set<String> MEMBER_VARS = Sets.newHashSet(
			"theArray", //StackAr, QueueAr 
			"topOfStack",//StackAr
			"s",//DisjSets
			"currentSize", //QueueAr
			"front",//QueueAr
			"back");//QueueAr
	
	/**
	 * Cache
	 */
	private static HashMap<String,String> cache = Maps.newHashMap();
	
	private Normalize(){
		super(new BasicVisitorFactory(){
			@Override
			public BasicVisitor build() {
				return new Normalize();
			}
		});
	}
	
	/**
	 * Trims <code>str</code>, appending a semi-colon if <code>str</code>
	 * does not end with a semi-colon
	 * @param str the original string
	 * @return string with semi-colon
	 */
	public static String withSemi(String str){
		return str.trim().endsWith(";") ? str.trim() : str.trim() + ";";
	}	
	
	public static JExpression normalize(JExpression expr){
		Normalize n = new Normalize();
		expr.unParenthesize().accept(n);
		assert n.getExpression() != null;
		return n.getExpression();
	}
	
	private static JParenthesedExpression paren(JExpression e){
		return new JParenthesedExpression(e.getTokenReference(), e.unParenthesize());
	}
	
	public static String normalize(String s){
		s = withSemi(s);
	
		if (cache.containsKey(s)){
			return cache.get(s);
		}else{
			JExpression expr;
			try {
				expr = Normalize.normalize(ExecuteJml.tryParse(s));
			} catch (RecognitionException e) {
				throw new IllegalArgumentException(s);
			} catch (TokenStreamException e) {
				throw new IllegalArgumentException(s);
			}
			String res = Convert.exprToString(expr);
			
			assert res != null;
			cache.put(s,res);
			return res;
		}
	}
	
	public static TypeSpecification normalize(TypeSpecification t){
		List<Clause> invs = Lists.newArrayList(Iterables.transform(t.getInvariants(),NORM));
		
		List<MethodContract> methods = Lists.newArrayList();
		
		for (MethodContract m : t.getMethods()){
			HashMap<String,List<Clause>> exs = Maps.newHashMap();
			
			if (!m.getExsures().isEmpty()){
				exs.put("RuntimeException", Lists.newArrayList(Iterables.transform(m.getExsures().get("RuntimeException"), NORM)));
			}
			
			methods.add(new MethodContract(
					m.getSignature(),
					m.getParameters(),
					m.isPublic(),
					m.getInfo(),
					m.getModifies(),
					Lists.newArrayList(Iterables.transform(m.getRequires(), NORM)),
					Lists.newArrayList(Iterables.transform(m.getEnsures(), NORM)),
					exs));
		}
		return new TypeSpecification(t.getFullyQualifiedName(),t.getLocation(),invs,t.getFields(),methods);
	}

	private static final Function<Clause,Clause> NORM = new Function<Clause,Clause>(){
		@Override
		public Clause apply(Clause arg0) {
			return new Clause(normalize(withSemi(arg0.getClause())),
					arg0.getProvenance(),
					arg0.getStatus());
		}
	};
	
	@Override
	public void visitNameExpression(JNameExpression arg0){
		if (MEMBER_VARS.contains(arg0.getName()) && arg0.getPrefix() == null){
			//add "this" prefix to known member variables
			expr = new JNameExpression(arg0.getTokenReference(), new JThisExpression(arg0.getTokenReference()), arg0.getName());
		}else{
			if (arg0.getPrefix() != null){
				JExpression n = normalize(arg0.getPrefix());
				
				expr = new JNameExpression(arg0.getTokenReference(),n,arg0.getName()) ;
			}else{
				expr = new JNameExpression(arg0.getTokenReference(),arg0.getName()) ;
			}
		}	
	}
	
	
	@Override
	public void visitJmlSpecQuantifiedExpression(JmlSpecQuantifiedExpression x){
		if (x.isForAll()){
			if (x.hasPredicate()){
				expr = new JmlSpecQuantifiedExpression(
						x.getTokenReference(), 
						x.oper(), 
						x.quantifiedVarDecls(),
						null,
						new JmlSpecExpression(new JmlRelationalExpression(x.getTokenReference(), org.jmlspecs.checker.Constants.OPE_IMPLIES,
								paren(normalize(x.predicate())),
								paren(normalize(x.specExpression())))));
			}else{
				super.visitJmlSpecQuantifiedExpression(x);
			}
		}else{
			throw new RuntimeException();
		}
	}
		
	@Override
	public void visitUnaryExpression(JUnaryExpression x){
		JExpression inner = normalize(x.expr());
		
		if (x.oper() == Constants.OPE_MINUS && inner instanceof JOrdinalLiteral){
			expr = new JOrdinalLiteral(x.getTokenReference(),
					-1 * ((JOrdinalLiteral) inner).numberValue().intValue(),
					CStdType.Integer);
		}else{
			expr = new JUnaryExpression(x.getTokenReference(),x.oper(), normalize(x.expr()));	
		}
	}
	
	@Override
	public void visitEqualityExpression(JEqualityExpression x) {
		JExpression l = viz(x.left());
		JExpression r = viz(x.right());
		
		if (l instanceof JBooleanLiteral && r instanceof JBooleanLiteral){
			//coalesce booleans
			expr = new JBooleanLiteral(x.getTokenReference(), 
					x.oper() == Constants.OPE_EQ ?
							l.getBooleanLiteral().booleanValue() && l.getBooleanLiteral().booleanValue()
							: !(l.getBooleanLiteral().booleanValue() && l.getBooleanLiteral().booleanValue())
			);
		}else if (r instanceof JBooleanLiteral){
			boolean reg = r.getBooleanLiteral().booleanValue() == (x.oper() == Constants.OPE_EQ);
			expr = reg ? l
					: new JUnaryExpression(x.getTokenReference(), Constants.OPE_LNOT, l);
		}else if (l instanceof JBooleanLiteral){
			boolean reg = l.getBooleanLiteral().booleanValue() == (x.oper() == Constants.OPE_EQ);
			expr = reg ? r
					: new JUnaryExpression(x.getTokenReference(), Constants.OPE_LNOT, r);
			
		}else if (Convert.exprToString(l).hashCode() < Convert.exprToString(r).hashCode()){
			expr = paren(new JEqualityExpression(x.getTokenReference(),x.oper(),l,r));
		}else{
			expr = paren(new JEqualityExpression(x.getTokenReference(),x.oper(),r,l));
		}	
	}
	
	@Override
	public void visitJmlOldExpression(JmlOldExpression x){
		expr = new JmlOldExpression(x.getTokenReference(),new JmlSpecExpression(normalize(x.specExpression())), null);
	}
	
	@Override
	public void visitMinusExpression(JMinusExpression x) {
		JExpression lhs = normalize(x.left());
		JExpression rhs = normalize(x.right());
		
		expr = paren(normalize(new JAddExpression(x.getTokenReference(), 
				lhs, 
				normalize(new JUnaryExpression(x.getTokenReference(),Constants.OPE_MINUS, rhs)))));	
	}
	@Override
	public void visitAddExpression(JAddExpression x) {
		JExpression lhs = normalize(x.left());
		JExpression rhs = normalize(x.right());
		
		expr = paren(new JAddExpression(x.getTokenReference(), lhs, rhs));
	}

	public static JmlRelationalExpression balance(JmlRelationalExpression x){
		JExpression lhs = normalize(x.left());
		JExpression rhs = normalize(x.right());
		
		TokenReference tok = x.getTokenReference();
		int op = x.oper();
		
		JOrdinalLiteral lo = new JOrdinalLiteral(tok,0,CStdType.Integer);
		JOrdinalLiteral ro = new JOrdinalLiteral(tok,0,CStdType.Integer);
		
		JExpression restL = new JOrdinalLiteral(tok,0,CStdType.Integer);
		JExpression restR = new JOrdinalLiteral(tok,0,CStdType.Integer);
		
		if (lhs.unParenthesize() instanceof JOrdinalLiteral){
			lo = (JOrdinalLiteral) lhs.unParenthesize();
		}else if (CoalesceOrdinals.isBasicAdd(lhs)){
			restL = ((JAddExpression) lhs.unParenthesize()).left();
			lo = (JOrdinalLiteral) ((JAddExpression) lhs.unParenthesize()).right().unParenthesize();
		}else{
			restL = lhs.unParenthesize();
		}
		
		if (rhs.unParenthesize() instanceof JOrdinalLiteral){
			ro = (JOrdinalLiteral) rhs.unParenthesize();
		}else if (CoalesceOrdinals.isBasicAdd(x.right())){
			restR = ((JAddExpression) rhs.unParenthesize()).left();
			ro = (JOrdinalLiteral) ((JAddExpression) rhs.unParenthesize()).right().unParenthesize();
		}else{
			restR =rhs.unParenthesize();
		}
	
		int leftVal = lo.numberValue().intValue() - ro.numberValue().intValue();
		int rightVal = ro.numberValue().intValue() - lo.numberValue().intValue();
		
		if (leftVal == rightVal && leftVal == 0){
			return new JmlRelationalExpression(tok,op,restL,restR);
		}else if (leftVal > 0){
			return new JmlRelationalExpression(tok,op,
					CoalesceOrdinals.coalesce(new JAddExpression(tok,restL,new JOrdinalLiteral(tok,leftVal,CStdType.Integer))),
					restR);
		}else if (rightVal > 0){
			return new JmlRelationalExpression(tok,op,
					restL,
					CoalesceOrdinals.coalesce(new JAddExpression(tok,restR,new JOrdinalLiteral(tok,rightVal,CStdType.Integer))));
		}else{
			throw new RuntimeException("Unexpected Values");
		}
	}
	
	

	@Override
	public void visitJmlRelationalExpression(JmlRelationalExpression x){
		JExpression lhs = normalize(x.left());
		JExpression rhs = normalize(x.right());
		
		switch(x.oper()){
		case Constants.OPE_LT:
			x = balance(x);
			expr = paren(new JmlRelationalExpression(x.getTokenReference(), Constants.OPE_LT, x.left(), x.right()));
			return;
		case Constants.OPE_GT:
			//SWAP
			//FLIP AND NORMALIZE
			expr = paren(normalize(new JmlRelationalExpression(x.getTokenReference(), Constants.OPE_LT, rhs,lhs)));
			return;
		case Constants.OPE_LE:
			//ADD 1 to RHS and normalize again
			JLiteral uno = new JOrdinalLiteral(x.getTokenReference(),1,CStdType.Integer);
			expr = paren(normalize(new JmlRelationalExpression(x.getTokenReference(), Constants.OPE_LT, lhs,
					normalize(new JAddExpression(x.getTokenReference(),rhs, uno)))));
			return;
		case Constants.OPE_GE:
			//CONVERT TO STRICT (assume int)
			//FLIP AND NORMALIZE
			expr = paren( normalize(new JmlRelationalExpression(x.getTokenReference(), Constants.OPE_LE, rhs, lhs)));
			return;
		}
		
		if (x.isEquivalence() || x.isNonEquivalence()){
			if (Convert.exprToString(lhs).hashCode() < Convert.exprToString(rhs).hashCode()){
				expr = paren(new JEqualityExpression(
						x.getTokenReference(),
						x.isEquivalence() ? Constants.OPE_EQ : Constants.OPE_NE,
						lhs,
						rhs));
			}else{
				expr = paren(new JEqualityExpression(
						x.getTokenReference(),
						x.isEquivalence() ? Constants.OPE_EQ : Constants.OPE_NE,
						rhs,
						lhs));
			}
		}else if (x.isBackwardImplication()){
			expr = paren(new JmlRelationalExpression(
					x.getTokenReference(), 
					org.jmlspecs.checker.Constants.OPE_IMPLIES, 
					rhs,
					lhs)); 
		}else{
			expr = paren(new JmlRelationalExpression(x.getTokenReference(), org.jmlspecs.checker.Constants.OPE_IMPLIES, lhs,rhs));
		}
		
	}

	@Override
	public void visitParenthesedExpression(JParenthesedExpression arg0) {
		expr = normalize(arg0);
	}

	
	@Override
	public void visitOrdinalLiteral(JOrdinalLiteral x) {
		expr = x;
	}


	@Override
	public void visitPrefixExpression(JPrefixExpression arg0) {
		throw new RuntimeException("Cannot normalize postfix expressions");
	}

	
	@Override
	public void visitPostfixExpression(JPostfixExpression x){
		throw new RuntimeException("Cannot normalize postfix expressions");
	}
}
