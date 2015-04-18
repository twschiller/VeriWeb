package com.schiller.veriasa.web.shared.core;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Mutable element definition map. Mapping from program element usage in source code to their definitions
 * in the source code, and associated JavaDoc
 * @author Todd Schiller
 */
public class DefinitionMap implements Serializable {
	private static final long serialVersionUID = 1L;

	private HashMap<String, HashMap<SourceElement, ElementDefinition>> data = 
		new HashMap<String, HashMap<SourceElement, ElementDefinition>>();
	
	public DefinitionMap(){
	}

	/**
	 * Add a mapping from <tt>source</tt> to <tt>destination</tt> in the map for <tt>compilationUnit</tt>
	 * @param compilationUnit the compilation unit
	 * @param source the source element (occurrence in the source code)
	 * @param definition the destination element (the definition)
	 */
	public void addMapping(String compilationUnit, SourceElement source, ElementDefinition definition){
		if (!data.containsKey(compilationUnit)){
			data.put(compilationUnit, new HashMap<SourceElement, ElementDefinition>());
		}
		data.get(compilationUnit).put(source, definition);
	}
	
	/**
	 * @return the set of compilation units for which the map has data
	 */
	public Set<String> getCompilationUnits(){
		return data.keySet();
	}

	/**
	 * Get an unmodifiable view of the mapping for elements in <tt>compilationUnit</tt>
	 * @param compilationUnit the compilation unit
	 * @return an unmodifiable view of the mapping for elements in <tt>compilationUnit</tt>
	 */
	public Map<SourceElement, ElementDefinition> getMap(String compilationUnit){
		if (!data.containsKey(compilationUnit)){
			return null;
		}
		return Collections.unmodifiableMap(data.get(compilationUnit));
	}
	
	public static class ElementDefinition implements Serializable{
		private static final long serialVersionUID = 2L;
		
		private String docHtml;
		private SourceLocation location;
		
		@SuppressWarnings("unused")
		private ElementDefinition(){
			
		}
		public ElementDefinition(String docHtml){
			this(null, docHtml);
		}
		
		public ElementDefinition(SourceLocation location, String docHtml){
			this.location = location;
			this.docHtml = docHtml;
		}
		
		public String getDocHtml() {
			return docHtml;
		}
		
		public SourceLocation getLocation() {
			return location;
		}
		
		public boolean hasLocation(){
			return location == null;
		}
	}
	
	public static class SourceElement implements Serializable{
		private static final long serialVersionUID = 1L;
		
		private String text;
		private SourceLocation location;
		
		@SuppressWarnings("unused")
		private SourceElement(){
			
		}
		
		public SourceElement(String text, SourceLocation location){
			this.location = location;
			this.text = text;
		}
		
		public String getText() {
			return text;
		}
		
		public SourceLocation getLocation() {
			return location;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((location == null) ? 0 : location.hashCode());
			result = prime * result + ((text == null) ? 0 : text.hashCode());
			return result;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			SourceElement other = (SourceElement) obj;
			if (location == null) {
				if (other.location != null)
					return false;
			} else if (!location.equals(other.location))
				return false;
			if (text == null) {
				if (other.text != null)
					return false;
			} else if (!text.equals(other.text))
				return false;
			return true;
		}
	}	
}
