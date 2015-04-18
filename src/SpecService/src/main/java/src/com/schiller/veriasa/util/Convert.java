package com.schiller.veriasa.util;

import java.util.List;

import org.jmlspecs.checker.JmlInvariant;
import org.multijava.mjc.JExpression;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.schiller.veriasa.executejml.SpanParser;
import com.schiller.veriasa.web.shared.core.Clause;
import com.schiller.veriasa.web.shared.core.Clause.Status;
import com.schiller.veriasa.web.shared.parsejml.JmlSpan;
import com.schiller.veriasa.web.shared.parsejml.SpanMaker;

public abstract class Convert {
	
	/**
	 * Convert a Java expression to a String
	 * @param expr
	 * @return
	 */
	public static String exprToString(JExpression expr){
		JmlSpan ss = SpanParser.build(expr);
		String res = ss.generateHtml(new SpanMaker(){
			@Override
			public String makeSpan(JmlSpan f, List<JmlSpan> associated) {
				return f.getText();
			}
		});
		return res;
	}
	
	public static List<Clause> convert(JmlInvariant[] old){
		return Lists.newArrayList(
				Iterables.transform(Lists.newArrayList(old), new Function<JmlInvariant,Clause>(){
					@Override
					public Clause apply(JmlInvariant arg0) {
						String i = Convert.exprToString(arg0.predicate());
						return new Clause(i,"TARGET", Status.KNOWN_GOOD);
					}
				}));
	}
	public static List<Clause> convert(List<String> old){
		return Lists.newArrayList(
				Iterables.transform(old, new Function<String,Clause>(){
					@Override
					public Clause apply(String arg0) {
						
						return new Clause(arg0,"SOLUTION", Status.KNOWN_GOOD);
					}
				}));
	}

}
