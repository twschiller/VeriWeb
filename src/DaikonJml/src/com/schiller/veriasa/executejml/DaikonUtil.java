package com.schiller.veriasa.executejml;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.schiller.veriasa.web.shared.executejml.VarTreeNode;

import daikon.PptTopLevel;
import daikon.ProglangType;
import daikon.ValueTuple;
import daikon.VarInfo;

/**
 * Utility methods for dealing with Daikon output
 * @author Todd Schiller
 */
public class DaikonUtil {

	/**
	 * Returns the variable information associated with <tt>daikonName</tt>, or
	 * returns <tt>null</tt> iff the variable is not in the trace
	 * @param ppt program point information 
	 * @param daikonName the Daikon-formatted name of the variable
	 * @return the variable information associated with <tt>daikonName</tt>
	 */
	public static VarInfo findVar(PptTopLevel ppt, String daikonName){
		for (VarInfo vi : ppt.var_infos){
			if (vi.name().equals(daikonName)){
				return vi;
			}
		}
		return null;
	}

	/**
	 * Returns the VeriWeb-formatted reference string for a value. Null values
	 * are given by <tt>"null"</tt>; references have the form <tt>"ref@..."</tt>
	 * @param refVal the reference value
	 * @return the VeriWeb-formatted reference string for a value
	 */
	private static String makeReferenceString(Object refVal){
		if (refVal == null || refVal.toString().equals("0")){
			return "null";
		}else{
			return "ref@" + refVal.toString();
		}
	}
	
	/**
	 * Returns the clean value string for variable <tt>var</tt> with value <tt>value</tt>
	 * @param var the variable record
	 * @param value the value
	 * @return the clean value string for variable <tt>var</tt> with value <tt>value</tt>
	 */
	private static String clean(VarInfo var, Object value){
		if (var.isPointer()){
			return makeReferenceString(value);
		}else if (var.type.equals(ProglangType.BOOLEAN)){
			return value.toString().equals(0) ? "false" : "true";
		}else{
			return value.toString();
		}
	}
	
	/**
	 * Returns the variable record for the instance variable (i.e., <tt>this</tt>) 
	 * at a program point
	 * @param ppt the program point
	 * @return the variable record for the instance variable, or <tt>null</tt>
	 */
	private static VarInfo thisVar(PptTopLevel ppt){
		for (VarInfo vi : ppt.var_infos){
			if (vi.name().equals("this")){
				return vi;
			}
		}
		return null;
	}
	
	/**
	 * Returns the variable record for the method return value, or <tt>null</tt>
	 * @param ppt the program point
	 * @return the variable record for the method return value, or <tt>null</tt>
	 */
	private static VarInfo resultVar(PptTopLevel ppt){
		for (VarInfo vi : ppt.var_infos){
			if (vi.name().equals("return")){
				return vi;
			}
		}
		return null;
	}
	
	/**
	 * <tt>true</tt> iff <tt>ppt</tt> is the exit program point for a constructor
	 * @param ppt the program point
	 * @return <tt>true</tt> iff <tt>ppt</tt> is the exit program point for a constructor
	 */
	private static boolean isCtorExit(PptTopLevel ppt){
		int b = ppt.name().indexOf('(');
		String ps[] = ppt.name().substring(0, b).split("\\.");
		int l = ps.length;
		
		return ppt.is_exit() && ps[l-1].equals(ps[l-2]);
	}
	
	public static List<VarTreeNode> buildVarTree(PptTopLevel ppt, ValueTuple vt){
		ArrayList<VarTreeNode> roots = new ArrayList<VarTreeNode>();
		
		HashMap<VarInfo,VarTreeNode> inserted = new HashMap<VarInfo, VarTreeNode>();
		
		VarInfo th = thisVar(ppt);
		VarInfo result = resultVar(ppt);
		
		//create root nodes
		for (VarInfo vi : ppt.var_infos){
			if ( ((vi.isParam() && !vi.equals(th) && vi.enclosing_var == null) 
					|| (vi.enclosing_var != null && vi.enclosing_var.equals(th)))){
				VarTreeNode n  = buildNode(vi,ppt,vt);
				roots.add(n);
				inserted.put(vi, n);
			}
		}
		
		if (result != null){
			VarTreeNode n  = buildNode(result,ppt,vt);
			roots.add(n);
			inserted.put(result, n);
		}
		
		//create other nodes
		boolean converged = true;
		do{
			converged = true;
			for (VarInfo vi : ppt.var_infos){
				if (inserted.containsKey(vi.enclosing_var) && !inserted.containsKey(vi) && !vi.isDerived()
						&& !vi.name().contains("getClass()") && !vi.name().startsWith("orig(")){
					VarTreeNode n  = buildNode(vi,ppt,vt);
					inserted.get(vi.get_enclosing_var()).addChild(n);
					inserted.put(vi, n);
					converged = false;
				}
			}
		}while (!converged);
		
		return roots;
	}
	

	public static VarTreeNode buildNode(VarInfo vi, PptTopLevel ppt, ValueTuple vt){
		VarTreeNode result = null;
		
		VarInfo maybeOrig = findVar(ppt, "orig(" + vi.name() + ")");
		
		String name = vi.name();
		
		if (vi.isArray() && name.contains("[..]")){
			if (vi.name().equals("return")){
				throw new RuntimeException("result cannot be array");
			}
			result = (maybeOrig != null) ? buildArrayNode(maybeOrig,vi,ppt,vt) : buildArrayNode(vi,null,ppt,vt);
		}else{
			if (maybeOrig != null){
				String newValue = clean(vi, vt.getValue(vi));
				String origValue = clean(maybeOrig, vt.getValue(maybeOrig));
				result = new VarTreeNode(name, origValue, newValue);
			}else if(vi.name().equals("return")){
				String returnValue = clean(vi, vt.getValue(vi));
				result = new VarTreeNode(name, null, returnValue);
			}else{
				String value = clean(vi, vt.getValue(vi));
				result = isCtorExit(ppt) ? new VarTreeNode(name, null, value) : new VarTreeNode(name,value,null);
			}
		}
		
		return result;
	}
	
	private static VarTreeNode buildArrayNode(VarInfo viBefore, VarInfo viAfter,  PptTopLevel ppt, ValueTuple vt){
		Object before = vt.getValue(viBefore);
		int lenBefore = Array.getLength(before);
		
		VarTreeNode result = null;
		
		if (viAfter == null){
			result = isCtorExit(ppt) 
					? new VarTreeNode(viBefore.name(), null, "length " + lenBefore) 
					: new VarTreeNode(viBefore.name(), "length " + lenBefore, null);
			
			for (int i = 0; i < lenBefore; i++){
				VarTreeNode c;
				
				String val = viBefore.type.equals(ProglangType.rep_parse("java.lang.Object[]")) ? makeReferenceString(Array.get(before, i)) : Array.get(before, i).toString();
				
				String name = viBefore.enclosing_var.name() + "[" + i + "]";
				
				c = isCtorExit(ppt) ?  new VarTreeNode(name,null,val) : new VarTreeNode(name,val,null);
				
				result.addChild(c);
			}
			
		}else{
			Object after = vt.getValue(viAfter);
			int lenAfter = Array.getLength(after);
			
			int maxLen = Math.max(lenBefore, lenAfter);
			result = new VarTreeNode(viAfter.name(), "length " + lenBefore, "length " + lenAfter);
			
			for (int i = 0; i < maxLen; i++){
				VarTreeNode c;
				if (viBefore.type.equals(ProglangType.rep_parse("java.lang.Object[]"))){
					c = new VarTreeNode(viAfter.enclosing_var.name() + "[" + i + "]", 
							i < lenBefore ? makeReferenceString(Array.get(before, i)) : null,
							i < lenAfter ? makeReferenceString(Array.get(after,i)) : null);
				}else{
					c = new VarTreeNode(viAfter.enclosing_var.name() + "[" + i + "]", 
							i < lenBefore ? Array.get(before, i).toString() : null,
							i < lenAfter ? Array.get(after, i).toString() : null);
				}
				result.addChild(c);
			}
			
		}
		
		return result;
	}
	
	
}
