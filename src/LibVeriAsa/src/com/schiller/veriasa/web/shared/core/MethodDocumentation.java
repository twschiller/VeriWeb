package com.schiller.veriasa.web.shared.core;

import java.io.Serializable;
import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;

/**
 * Documentation for a method
 * @author Todd Schiller
 */
public class MethodDocumentation extends ElementDocumentation implements Serializable{

	private static final long serialVersionUID = 2L;

	private String summary;
	private List<ParameterDocumentation> params;
	private String returns;
	
	private static enum ParseState { SUMMARY, PARAMS, RETURN }
	
	private MethodDocumentation(){
		super(null, FormatType.NONE);
	}
	
	public MethodDocumentation(String text, FormatType format) throws ParseException {
		super(text, format);
		
		if (format == FormatType.JAVADOC){
			parseJavaDoc(text);
		}else{
			throw new IllegalArgumentException("Method documentation format " + format.toString() + " not supported");
		}
	}

	public String getSummary() {
		return summary;
	}

	public List<ParameterDocumentation> getParams() {
		return new LinkedList<ParameterDocumentation>(params);
	}
	
	public String getReturns() {
		return returns;
	}

	private void parseJavaDoc(String javaDoc) throws ParseException{
		if (javaDoc == null){
			summary = null;
			params = new LinkedList<ParameterDocumentation>();
			returns = null;
		}else{
			//TODO: Support parameters 
			//TODO: use the javadoc api for parsing
			
			params = new LinkedList<ParameterDocumentation>();
			
			javaDoc = javaDoc.trim();
			
			javaDoc = javaDoc.substring(3, javaDoc.length() - 2);
			
			String [] lines = javaDoc.split("\n");
			
			summary = "";
			returns = "";
			
			ParseState state = ParseState.SUMMARY;
				
			StringBuilder current = new StringBuilder();
			
			for (String line : lines){
				String t = line.trim();
			
				if (t.equals("") || t.equals("/**") || t.equals("*/") || t.equals("*")){
					continue;
				}
				
				t = t.substring(1).trim(); // get rid of leading *
				
				if (t.startsWith("@param")){
					state = ParseState.PARAMS;
					summary = current.toString();
					current = new StringBuilder();
				}
				if (t.startsWith("@return")){
					if (state.equals(ParseState.SUMMARY)){
						summary = current.toString();
					}else if (state.equals(ParseState.PARAMS)){
						
					}
					current = new StringBuilder();
				}
				
				current.append(t);
			}
			
			switch(state){
			case SUMMARY:
				summary = current.toString();
				break;
			case RETURN:
				returns = current.toString();
				break;
			}	
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((params == null) ? 0 : params.hashCode());
		result = prime * result + ((returns == null) ? 0 : returns.hashCode());
		result = prime * result + ((summary == null) ? 0 : summary.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		MethodDocumentation other = (MethodDocumentation) obj;
		if (params == null) {
			if (other.params != null)
				return false;
		} else if (!params.equals(other.params))
			return false;
		if (returns == null) {
			if (other.returns != null)
				return false;
		} else if (!returns.equals(other.returns))
			return false;
		if (summary == null) {
			if (other.summary != null)
				return false;
		} else if (!summary.equals(other.summary))
			return false;
		return true;
	}



	public static class ParameterDocumentation implements Serializable{
		private static final long serialVersionUID = 2L;
		
		private String param;
		private String text;
		
		@SuppressWarnings("unused")
		private ParameterDocumentation(){
		}
		
		public ParameterDocumentation(String param, String text) {
			super();
			this.param = param;
			this.text = text;
		}

		public String getName() {
			return param;
		}

		public String getDoc() {
			return text;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((text == null) ? 0 : text.hashCode());
			result = prime * result + ((param == null) ? 0 : param.hashCode());
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
			ParameterDocumentation other = (ParameterDocumentation) obj;
			if (text == null) {
				if (other.text != null)
					return false;
			} else if (!text.equals(other.text))
				return false;
			if (param == null) {
				if (other.param != null)
					return false;
			} else if (!param.equals(other.param))
				return false;
			return true;
		}	
	}
}
