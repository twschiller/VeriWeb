package com.schiller.veriasa.web.server;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

import org.jmlspecs.checker.JmlResultExpression;
import org.jmlspecs.checker.JmlSpecExpression;
import org.multijava.mjc.JNameExpression;

import antlr.RecognitionException;
import antlr.TokenStreamException;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.schiller.veriasa.executejml.ExecuteJml;
import com.schiller.veriasa.web.server.escj.JmlWalker;
import com.schiller.veriasa.web.shared.config.JmlParseException;
import com.schiller.veriasa.web.shared.core.MethodContract;
import com.schiller.veriasa.web.shared.core.Clause;

/**
 * true iff specification doesn't contain one of the specified names
 * @author Todd Schiller
 */
public class NoParamPredicate implements Predicate<Clause>{
	private final List<String> badNames;
	private final boolean allowResult;
	private static final WeakHashMap<String, NameCollector> cache = new WeakHashMap<String, NameCollector>();
	
	/**
	 * @param method the method
	 * @param allowResult true iff \result is allowed
	 */
	public NoParamPredicate(MethodContract method, boolean allowResult) {
		this.badNames = Lists.newArrayList(method.getParameters());
		this.allowResult = allowResult;
	}
	
	@Override
	public boolean apply(Clause statement) {
		NameCollector acc = null;
		
		synchronized(cache){	
			if (cache.containsKey(statement)){
				acc = cache.get(statement);
			}else{
				try {
					acc = NameCollector.names(statement.getClause());
					cache.put(statement.getClause(), acc);
				} catch (JmlParseException e) {
					return true;
				}
			}
		}

		return (!acc.containsResult || allowResult)
			&& Collections.disjoint(badNames, acc.names);
	}
	
	
	private static class NameCollector extends JmlWalker{
		private final Set<String> names = Sets.newHashSet();
		private boolean containsResult = false;
		
		private NameCollector(){
		};
	
		public static NameCollector names(JmlSpecExpression expr){
			NameCollector acc = new NameCollector();
			expr.expression().accept(acc);
			return acc;
		}
		
		public static NameCollector names(String jmlStatement) throws JmlParseException{
			try {
				return names(ExecuteJml.tryParse(SpecUtil.clean(jmlStatement)));
			} catch (RecognitionException e) {
				throw new JmlParseException(e);
			} catch (TokenStreamException e) {
				throw new JmlParseException(e);
			}
		}

		@Override
		public void visitJmlResultExpression(JmlResultExpression resultExpr) {
			containsResult = true;
			super.visitJmlResultExpression(resultExpr);
		}

		@Override
		public void visitNameExpression(JNameExpression nameExpr) {
			names.add(nameExpr.qualifiedName());
			super.visitNameExpression(nameExpr);
		}
	}
}
