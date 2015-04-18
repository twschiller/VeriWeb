package com.schiller.veriasa.web.shared.core;

import java.io.Serializable;

/**
 * A program element (e.g., method call, variable usage, etc.) in a source file
 * @author Todd Schiller
 */
public final class SourceElement implements Serializable{
	private static final long serialVersionUID = 2L;

	public static enum LanguageType { JAVA, CSHARP };
	
	private ElementDocumentation doc;
	private SourceLocation loc;
	private String body;
	private LanguageType srcLanguage;
	
	@SuppressWarnings("unused")
	private SourceElement(){	
	}
	
	public SourceElement(SourceLocation location, ElementDocumentation documentation, String body, LanguageType language){
		this.loc = location;
		this.doc = documentation;
		this.body = body;
		this.srcLanguage = language;
	}

	public SourceLocation getLocation() {
		return loc;
	}

	public ElementDocumentation getDoc() {
		return doc;
	}

	public String getBody(){
		return body;
	}

	public LanguageType getSrcLanguage() {
		return srcLanguage;
	}

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

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SourceElement other = (SourceElement) obj;
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