package com.schiller.veriasa.web.server;

import java.util.Comparator;
import java.util.List;

import com.google.common.base.Predicate;

/**
 * Generic utility functions
 * @author Todd Schiller
 */
public class GenericUtil {
	
	/**
	 * Returns the index of <code>query</code> in <code>objs</code> using comparator <code>cmp</code>,
	 * or <code>-1</code> if <code>objs</code> does not contain <code>query</code>
	 * @param objs list of objects to search
	 * @param query the query
	 * @param cmp the comparator
	 * @return the index of <code>query</code> in <code>objs</code> using comparator <code>cmp</code>
	 */
	public static <T> int findIndex(List<T> objs, T query, Comparator<T> cmp){
		for (int i = 0; i < objs.size(); i++){
			if (cmp.compare(objs.get(i), query) == 0){
				return i;
			}	
		}
		return -1;
	}
	
	/**
	 * true iff subject object is == to query
	 * @param query
	 * @return true iff subject object is == to query
	 */
	public static Predicate<Object> refEq(final Object query){
		return new Predicate<Object>(){
			@Override
			public boolean apply(Object o) {
				return o == query;
			}	
		};
	}
}
