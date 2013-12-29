package com.schiller.veriasa.web.server;

import java.util.List;

import com.schiller.veriasa.web.shared.core.SourceLocation;

/**
 * Utility functions for working with text and source code
 * @author Todd Schiller
 */
public class TextUtil {
	
	/**
	 * Insert <code>toInsert</code> at position <code>index</code> in <code>original</code>
	 * @param original the original string
	 * @param index insertion point
	 * @param toInsert the text to insert
	 * @return
	 */
	public static String insert(String original, int index, String toInsert){
		if (index == 0){
			return toInsert.concat(original);
		}else{
			return original.substring(0, index).concat(toInsert).concat(original.substring(index));
		}
	}
	
	/**
	 * Get the zero-based line number for a zero-based offset
	 * @param lineOffsets list that specifies the starting offset for each line
	 * @param offset the zero-based character offset
	 * @return zero-based line number for the offset
	 */
	public static int lineForOffset(List<Integer> lineOffsets, int offset){
		for (int i = 0; i < lineOffsets.size() - 1 ; i++){
			if (lineOffsets.get(i) <= offset && lineOffsets.get(i + 1) > offset){
				return i;
			}
		}
		return lineOffsets.size() - 1;
	}

	/**
	 * true iff <code>query</code> is contained within <code>region</code>
	 * @param region 
	 * @param query
	 * @return true iff <code>query</code> is contained within <code>region</code>
	 */
	public static boolean contains(SourceLocation region, SourceLocation query){
		int regionStart = region.getOffset();
		int regionEnd = regionStart + region.getLength();
		
		int queryStart = query.getOffset();
		int queryEnd = queryStart + query.getLength();
		
		return (queryStart >= regionStart && queryEnd <= regionEnd);
	}
	
	/**
	 * Get the end position of a source location (offset plus length)
	 * @param location the source location
	 * @return the end position of a source location
	 */
	public static int getEnd(SourceLocation location){
		return location.getLength() + location.getOffset();
	}
	
	/**
	 * Returns the number of leading whitespace characters
	 * @param x a string
	 * @return the number of leading whitespace characters
	 */
	public static int indent(String x){
		if (x.trim().isEmpty()){
			return 0;
		}
		return x.indexOf(x.trim().charAt(0));
	}
	
	/**
	 * Returns the string <code>x</code> without whitespace
	 * @param x original string
	 * @return the string <code>x</code> without whitespace
	 */
	public static String removeWhitespace(String x){
		 return x.replaceAll("\\s", "");
	}
}
