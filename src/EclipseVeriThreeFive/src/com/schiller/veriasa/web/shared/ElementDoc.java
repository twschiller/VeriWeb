package com.schiller.veriasa.web.shared;

import java.io.Serializable;

@SuppressWarnings("serial")
public class ElementDoc implements Serializable{
	public static enum FormatType { NONE, JAVADOC, DOTNET};
	
	private String doc;
	private FormatType docFormat;
	
	
	@SuppressWarnings("unused")
	private ElementDoc(){
		
	}
	
	public ElementDoc(String doc, FormatType docFormat){
		this.doc = doc;
		this.docFormat = docFormat;
	}

	public String getDoc() {
		return doc;
	}

	public FormatType getDocFormat() {
		return docFormat;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((doc == null) ? 0 : doc.hashCode());
		result = prime * result
				+ ((docFormat == null) ? 0 : docFormat.hashCode());
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
		ElementDoc other = (ElementDoc) obj;
		if (doc == null) {
			if (other.doc != null)
				return false;
		} else if (!doc.equals(other.doc))
			return false;
		if (docFormat != other.docFormat)
			return false;
		return true;
	}	
	
	
}
