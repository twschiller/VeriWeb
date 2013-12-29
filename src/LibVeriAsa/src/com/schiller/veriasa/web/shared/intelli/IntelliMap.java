package com.schiller.veriasa.web.shared.intelli;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Type information mapping (i.e., recursive field and method information)
 * @author Todd Schiller
 */
public class IntelliMap implements Serializable{
	private static final long serialVersionUID = 2L;
	
	/**
	 * Fully Qualified Type -> Type Information
	 */
	private HashMap<String, IntelliNode> typeData = new HashMap<String, IntelliNode>();

	/**
	 * Create an empty mapping
	 */
	public IntelliMap(){
	}
	
	/**
	 * Add information for <tt>typeName</tt> to the mapping
	 * @param typeName the fully qualified type name
	 * @param node type information
	 */
	public void addType(String typeName, IntelliNode node){
		typeData.put(typeName, node);
	}

	/**
	 * <tt>true</tt> iff <tt>typeName</tt> is in the mapping
	 * @param typeName the fully qualified type name
	 * @return <tt>true</tt> iff <tt>typeName</tt> is in the mapping
	 */
	public boolean isMapped(String typeName){
		return typeData.containsKey(typeName);
	}
	
	/**
	 * Returns the information for <tt>typeName</tt>
	 * @param typeName the fully qualified type name
	 * @return the information for <tt>typeName</tt>
	 */
	public IntelliNode getIntelliNode(String typeName){
		return typeData.get(typeName);
	}
}
