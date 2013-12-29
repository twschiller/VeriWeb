package com.schiller.veriasa.web.shared;

import java.io.Serializable;
import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("serial")
public class FunctionDoc extends ElementDoc implements Serializable{

	private String summary;
	private List<ParamDoc> params;
	private String returns;
	
	
	public String getSummary() {
		return summary;
	}


	public List<ParamDoc> getParams() {
		return new LinkedList<ParamDoc>(params);
	}


	public String getReturns() {
		return returns;
	}


	private FunctionDoc(){
		super("",FormatType.NONE);
		summary = "";
		params = new LinkedList<ParamDoc>();
		returns = "";
	}
	
	public FunctionDoc(String doc, FormatType docFormat) throws ParseException {
		super(doc, docFormat);
		
		switch (docFormat){
		case JAVADOC:
			parseJavadoc(doc);
			break;
		default:
			throw new RuntimeException("Function doc format " + docFormat.toString() + " not supported");
		}
		
	}

	
	private static enum ParseState { SUMMARY, PARAMS, RETURN}
	
	private void parseJavadoc(String doc) throws ParseException{
		if (doc == null){
			summary = null;
			params = new LinkedList<ParamDoc>();
			returns = null;
		}else{
			//TODO: Support parameters 
			//TODO: use the javadoc api for parsing
			
			params = new LinkedList<ParamDoc>();
			
			doc = doc.trim();
			
			doc = doc.substring(3, doc.length() - 2);
			
			String [] lines = doc.split("\n");
			
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
		
		//throw new ParseException("Error parsing function documentation",0);
	}

	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((params == null) ? 0 : params.hashCode());
		result = prime * result + ((returns == null) ? 0 : returns.hashCode());
		result = prime * result + ((summary == null) ? 0 : summary.hashCode());
		return result;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		FunctionDoc other = (FunctionDoc) obj;
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



	public static class ParamDoc implements Serializable{
		private String name;
		private String doc;
		
		@SuppressWarnings("unused")
		private ParamDoc(){
			
		}
		
		public ParamDoc(String name, String doc) {
			super();
			this.name = name;
			this.doc = doc;
		}

		public String getName() {
			return name;
		}

		public String getDoc() {
			return doc;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((doc == null) ? 0 : doc.hashCode());
			result = prime * result + ((name == null) ? 0 : name.hashCode());
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
			ParamDoc other = (ParamDoc) obj;
			if (doc == null) {
				if (other.doc != null)
					return false;
			} else if (!doc.equals(other.doc))
				return false;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			return true;
		}
		
	}
	
	
	
}
