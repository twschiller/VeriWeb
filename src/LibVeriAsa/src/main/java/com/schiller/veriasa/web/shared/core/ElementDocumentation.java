package com.schiller.veriasa.web.shared.core;

import java.io.Serializable;

/**
 * Documentation for a program element
 * @author Todd Schiller
 */
public class ElementDocumentation implements Serializable{
	private static final long serialVersionUID = 2L;

	public static enum FormatType {NONE, JAVADOC, DOTNET};
	
	private String text;
	private FormatType format;
	
	@SuppressWarnings("unused")
	private ElementDocumentation(){	
	}
	
	/**
	 * @param text the documentation
	 * @param format the format the documentation is stored in
	 */
	public ElementDocumentation(String text, FormatType format){
		this.text = text;
		this.format = format;
	}

	/**
	 * @return the documentation in format {@link ElementDocumentation#getFormat()}
	 */
	public String getText() {
		return text;
	}

	/**
	 * @return the format of the documentation
	 */
	public FormatType getFormat() {
		return format;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((text == null) ? 0 : text.hashCode());
		result = prime * result
				+ ((format == null) ? 0 : format.hashCode());
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
		ElementDocumentation other = (ElementDocumentation) obj;
		if (text == null) {
			if (other.text != null)
				return false;
		} else if (!text.equals(other.text))
			return false;
		if (format != other.format)
			return false;
		return true;
	}	
}
