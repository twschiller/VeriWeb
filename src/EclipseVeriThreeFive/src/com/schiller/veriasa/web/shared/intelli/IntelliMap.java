package com.schiller.veriasa.web.shared.intelli;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Class that stores a mapping from fully-qualified type strings 
 * to their corresponding nodes
 * @author tws
 */
@SuppressWarnings("serial")
public class IntelliMap implements Serializable{
	
	private HashMap<String, IntelliNode> map = new HashMap<String, IntelliNode>();

	public IntelliMap(){
		
	}
	
	public void addMapping(String typeName,IntelliNode node){
		map.put(typeName, node );
	}

	public boolean isMapped(String typeName){
		return map.containsKey(typeName);
	}
	
	public IntelliNode getIntelliNode(String typeName){
		return map.get(typeName);
	}
}
