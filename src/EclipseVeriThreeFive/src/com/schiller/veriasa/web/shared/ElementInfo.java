package com.schiller.veriasa.web.shared;

import java.io.Serializable;

@SuppressWarnings("serial")
public final class ElementInfo implements Serializable{

	public static enum LanguageType { JAVA, CSHARP };
	
	private ElementDoc doc;
	private SrcLoc loc;
	private String body;
	private LanguageType srcLanguage;
	
	@SuppressWarnings("unused")
	private ElementInfo(){
		
	}
	
	public ElementInfo(SrcLoc loc, ElementDoc doc, String body, LanguageType srcLanguage){
		this.loc = loc;
		this.doc = doc;
		this.body = body;
		this.srcLanguage = srcLanguage;
	}

	public SrcLoc getLocation() {
		return loc;
	}

	public ElementDoc getDoc() {
		return doc;
	}

	public String getBody(){
		return body;
	}

	public LanguageType getSrcLanguage() {
		return srcLanguage;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((body == null) ? 0 : body.hashCode());
		result = prime * result + ((doc == null) ? 0 : doc.hashCode());
		result = prime * result + ((loc == null) ? 0 : loc.hashCode());
		result = prime * result
				+ ((srcLanguage == null) ? 0 : srcLanguage.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ElementInfo other = (ElementInfo) obj;
		if (body == null) {
			if (other.body != null)
				return false;
		} else if (!body.equals(other.body))
			return false;
		if (doc == null) {
			if (other.doc != null)
				return false;
		} else if (!doc.equals(other.doc))
			return false;
		if (loc == null) {
			if (other.loc != null)
				return false;
		} else if (!loc.equals(other.loc))
			return false;
		if (srcLanguage != other.srcLanguage)
			return false;
		return true;
	}
	
	
}