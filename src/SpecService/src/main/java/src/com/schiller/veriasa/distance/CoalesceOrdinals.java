package com.schiller.veriasa.distance;

import java.util.List;

import org.jmlspecs.checker.JmlVisitorNI;
import org.multijava.mjc.CStdType;
import org.multijava.mjc.Constants;
import org.multijava.mjc.JAddExpression;
import org.multijava.mjc.JExpression;
import org.multijava.mjc.JMinusExpression;
import org.multijava.mjc.JOrdinalLiteral;
import org.multijava.mjc.JParenthesedExpression;
import org.multijava.mjc.JUnaryExpression;
import org.multijava.util.compiler.TokenReference;

import com.google.common.collect.Lists;

/**
 * AST transform to combine ordinals (integers) in addition and subtraction statements
 * @author Todd Schiller
 */
public abstract class CoalesceOrdinals{

	public static JExpression coalesce(JExpression expr){
		Enumerator c = new Enumerator();
		expr.accept(c);
		
		int total = 0;
		int numOrdinal = 0;
		
		List<JExpression> no = Lists.newArrayList();
		
		for (JExpression a : c.adds){
			JExpression uu = a.unParenthesize();
			
			if (uu instanceof JOrdinalLiteral){
				total += ((JOrdinalLiteral) uu).numberValue().intValue();
				numOrdinal++;
			}else if (uu instanceof JUnaryExpression &&
					((JUnaryExpression) uu).expr().unParenthesize() instanceof JOrdinalLiteral){
				
				total -= ((JOrdinalLiteral) ((JUnaryExpression) uu).expr().unParenthesize()).numberValue().intValue();
				
			}else{
				no.add(a);
			}
		}
	
		TokenReference tok = expr.getTokenReference();
		
		if (numOrdinal == 0){
			return expr;
		}else if (no.size() == 0){
			assert total >= 0;
			return new JOrdinalLiteral(tok,total,CStdType.Integer);
		}else if (no.size() == 1){
			if (total == 0){
				// DROP THE ZERO TERM
				return no.get(0);
			}else{
				JOrdinalLiteral raw = new JOrdinalLiteral(tok,total,CStdType.Integer);
				
				JExpression oo = total > 0 ? raw
					: new JParenthesedExpression(tok, 
							new JUnaryExpression(tok, Constants.OPE_MINUS, raw));
				return new JAddExpression(tok, no.get(0), oo);
			}
			
		}else{
			throw new RuntimeException("cannot coalesce multiple non-ordinals");
		}
	}
	
	public static boolean isBasicAdd(JExpression x){
		if (x.unParenthesize() instanceof JAddExpression){
			return ((JAddExpression) x.unParenthesize()).right().unParenthesize() instanceof JOrdinalLiteral;
		}else{
			return false;
		}
	}
	
	private static class Enumerator extends JmlVisitorNI{
		List<JExpression> adds = Lists.newArrayList();
		
		@Override
		public void visitAddExpression(JAddExpression arg0) {
			for (JExpression e : Lists.newArrayList(arg0.left(), arg0.right())){
				if (e.unParenthesize() instanceof JAddExpression){
					e.unParenthesize().accept(this);
				}else{
					adds.add(e);
				}
			}
		}
		
		@Override
		public void visitMinusExpression(JMinusExpression arg0) {
			throw new RuntimeException("can only coalesce addition");
		}
		
	}

}
