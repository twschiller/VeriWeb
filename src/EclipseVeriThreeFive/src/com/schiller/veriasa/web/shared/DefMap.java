package com.schiller.veriasa.web.shared;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("serial")
public class DefMap implements Serializable {

	private HashMap<String, HashMap<SrcElement, DestElement>> map = 
		new HashMap<String, HashMap<SrcElement, DestElement>>();
	
	public DefMap(){
		
	}
	
	public Set<String> getCompilationUnits(){
		return map.keySet();
	}

	public Map<SrcElement, DestElement> getMap(String compilationUnit){
		if (!map.containsKey(compilationUnit)){
			return null;
		}
		return Collections.unmodifiableMap(map.get(compilationUnit));
	}

	public void addMapping(String cu, SrcElement src, DestElement dest){
		if (!map.containsKey(cu)){
			map.put(cu, new HashMap<SrcElement, DestElement>());
		}
		map.get(cu).put(src, dest);
	}
	
	public static class DestElement implements Serializable{
		private String docHtml;
		private SrcLoc srcLoc;
		
		@SuppressWarnings("unused")
		private DestElement(){
			
		}
		public DestElement(String docHtml){
			this.srcLoc = null;
			this.docHtml = docHtml;
		}
		public DestElement(SrcLoc srcLoc, String docHtml){
			this.srcLoc = srcLoc;
			this.docHtml = docHtml;
		}
		public String getDocHtml() {
			return docHtml;
		}
		public SrcLoc getSrcLoc() {
			return srcLoc;
		}
		public boolean hasSrcLoc(){
			return srcLoc == null;
		}
	}
	
	public static class SrcElement implements Serializable{
		private String text;
		private SrcLoc loc;
		
		@SuppressWarnings("unused")
		private SrcElement(){
			
		}
		public SrcElement(String text, SrcLoc loc){
			this.loc = loc;
			this.text = text;
		}
		public String getText() {
			return text;
		}
		public SrcLoc getLoc() {
			return loc;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((loc == null) ? 0 : loc.hashCode());
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
			SrcElement other = (SrcElement) obj;
			if (loc == null) {
				if (other.loc != null)
					return false;
			} else if (!loc.equals(other.loc))
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
