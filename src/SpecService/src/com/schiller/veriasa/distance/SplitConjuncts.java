package com.schiller.veriasa.distance;

import java.util.Set;

import org.jmlspecs.checker.JmlRelationalExpression;
import org.jmlspecs.checker.JmlSpecExpression;
import org.jmlspecs.checker.JmlSpecQuantifiedExpression;
import org.jmlspecs.checker.JmlVisitorNI;
import org.multijava.mjc.JConditionalAndExpression;
import org.multijava.mjc.JExpression;

import com.google.common.collect.Sets;

/**
 * Split top-level conjuncts
 * @author Todd Schiller
 */
public class SplitConjuncts extends JmlVisitorNI {

	private Set<JExpression> exprs = Sets.newHashSet();
	
	public static Set<JExpression> split(JExpression expr){
		SplitConjuncts s = new SplitConjuncts();
		try{
			expr.accept(s);
			return Sets.newHashSet(s.exprs);
		}catch(Exception e){
			return Sets.newHashSet(expr);
		}
	}

	@Override
	public void visitConditionalAndExpression(JConditionalAndExpression expr) {
		exprs.addAll(split(expr.left()));
		exprs.addAll(split(expr.right()));
	}

	@Override
	public void visitJmlRelationalExpression(JmlRelationalExpression expr) {
		if (expr.isImplication()){
			for (JExpression e : split(expr.right())){
				exprs.add(new JmlRelationalExpression(expr.getTokenReference(),expr.oper(),expr.left(),e));
			}
		}else if(expr.isBackwardImplication()){
			for (JExpression e : split(expr.left())){
				exprs.add(new JmlRelationalExpression(expr.getTokenReference(),expr.oper(),expr.right(),e));
			}
		}else{
			throw new RuntimeException();
		}
	}

	@Override
	public void visitJmlSpecQuantifiedExpression(JmlSpecQuantifiedExpression expr) {
		if (expr.isForAll()){
			for (JExpression e : split(expr.specExpression().expression())){
				exprs.add(new JmlSpecQuantifiedExpression(
						expr.getTokenReference(), 
						expr.oper(),
						expr.quantifiedVarDecls(), 
						expr.predicate(), 
						new JmlSpecExpression(e)));
			}
		}else{
			throw new RuntimeException();
		}
	}
}
