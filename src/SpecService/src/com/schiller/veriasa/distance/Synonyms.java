package com.schiller.veriasa.distance;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class Synonyms {

	static HashMap<String,Set<String>> core = Maps.newHashMap();
	
	static HashMap<Set<String>,Set<Set<String>>> ext = Maps.newHashMap();
	
	public static boolean same(String a, String b){
		 return core.containsKey(a) && core.get(a).contains(b);
	}
	
	public static boolean same(final String a, Collection<String> b){
		
		if (b.contains(a)){
			return true;
		}
		
		if (!core.containsKey(a)){
			return false;
		}
		
		return Iterables.any(b, new Predicate<String>(){
			@Override
			public boolean apply(String arg0) {
				return same(a,arg0);
			}
		});
	}
	
	public static boolean same(final Set<String> a, Set<String> b){
		if (Sets.powerSet(b).contains(a)){
			return true;
		}
		if (!ext.containsKey(a)){
			return false;
		}
		
		for (Set<String> test : Sets.powerSet(b)){
			if (ext.get(a).contains(test)){
				return true;
			}
		}
		return false;
	}
	
	static void add(Set<String> a, Set<String> b){
		if (!ext.containsKey(a)){
			ext.put(a, Sets.<Set<String>>newHashSet());
		}
		if (!ext.containsKey(b)){
			ext.put(b, Sets.<Set<String>>newHashSet());
		}
		
		ext.get(a).add(b);
		ext.get(b).add(a);
	}

	static void add(String a, String b){
		if (!core.containsKey(a)){
			core.put(a, Sets.<String>newHashSet());
		}
		if (!core.containsKey(b)){
			core.put(b, Sets.<String>newHashSet());
		}
		
		core.get(a).add(b);
		core.get(b).add(a);
	}
	
	static{
		add("this.topOfStack + 1  <  this.theArray.length", "this.topOfStack  <  this.theArray.length - 1");
		add("(\\forall int i, j;this.topOfStack + 1  <  i - 1 && i  <  this.theArray.length;this.theArray[i] == null)",
			"(\\forall int i;(this.topOfStack + 1  <  i - 1 && i  <  this.theArray.length) ==> (this.theArray[i] == null))");
		add("(\\forall int i, j;(0  <  i - 1 && i  <  this.topOfStack - 1) ==> (this.theArray[i] != null))",
				"(\\forall int i;(0  <  i - 1 && i  <  this.topOfStack - 1) ==> (this.theArray[i] != null))");
		add("(\\forall int i, j;(this.topOfStack + 1  <  i - 1 && i  <  this.theArray.length) ==> (this.theArray[i] == null))",
			"(\\forall int i;(this.topOfStack + 1  <  i - 1 && i  <  this.theArray.length) ==> (this.theArray[i] == null))");
		
		
		add(Sets.newHashSet("this.theArray.length - 1 == this.topOfStack ==> \\result == true","this.theArray.length - 1 != this.topOfStack ==> \\result == false"),
				Sets.newHashSet("(this.theArray.length - 1 == this.topOfStack) == (\\result == true)"));
	}
}
