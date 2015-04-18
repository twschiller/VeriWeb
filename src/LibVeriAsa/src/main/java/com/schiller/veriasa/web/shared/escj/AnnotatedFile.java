package com.schiller.veriasa.web.shared.escj;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.schiller.veriasa.web.shared.core.Clause;

/**
 * File annotated with JML contracts. Maintains mapping from line in orginal file to 
 * lines in the new file
 * @author Todd Schiller
 */
public class AnnotatedFile implements Serializable{
	private static final long serialVersionUID = 2L;

	transient private ArrayList<String> original;
	transient private ArrayList<String> annotated;
	
	ArrayList<Object> lineMap = new ArrayList<Object>();
	
	private String lineSeparator;
	
	@SuppressWarnings("unused")
	private AnnotatedFile(){
	}
	
	/**
	 * Create an annotated file with no annotations
	 * @param lines the original lines
	 * @param lineSeparator the line separator to use
	 */
	public AnnotatedFile(List<String> lines, String lineSeparator){
		this.lineSeparator = lineSeparator;
		this.original = new ArrayList<String>(lines);
		this.annotated = new ArrayList<String>(lines);
		
		for (int i = 0; i < lines.size(); i++){
			lineMap.add(i);
		}
		checkRep();
	}
	
	/**
	 * Check that the line mapping still makes sense
	 */
	private void checkRep(){
		if (annotated != null && original != null){
			for (int i = 0; i < lineMap.size(); i++){
				if (lineMap.get(i) instanceof Integer){
					int old = (Integer) lineMap.get(i);

					if (!annotated.get(i).equals(original.get(old))){
						throw new RuntimeException("Line mismatch; expected: " + annotated.get(i) + " actual: " + original.get(old) );
					}
				}
			}
		}
	}
	
	/**
	 * Gets the zero-based line number in the original file
	 * given the zero-based line number in the annotated file
	 * @param newLine zero-based line number in the annotated file
	 * @return zero-based line number in the old file
	 */
	public int originalLine(int newLine){
		if (lineMap.get(newLine) instanceof Integer){
			return (Integer) lineMap.get(newLine);
		}else{
			throw new RuntimeException("Line " + newLine + " is not mapped to an old line");
		}
	}
	
	/**
	 * <tt>true</tt> iff line <tt>newLine</tt> in the annotated file contains a clause
	 * @param newLine the line in the annotated file
	 * @return <tt>true</tt> iff line <tt>newLine</tt> in the annotated file contains a clause
	 */
	public boolean refersToSpec(int newLine){
		return (lineMap.get(newLine) instanceof Clause);
	}
	
	/**
	 * Returns the clause at line <tt>newLine</tt> in the annotated file
	 * @param newLine the line in the annotated file
	 * @return the clause at line <tt>newLine</tt> in the annotated file
	 */
	public Clause getSpec(int newLine){
		if (refersToSpec(newLine)){
			return (Clause) lineMap.get(newLine);
		}else{
			if (lineMap.get(newLine) instanceof Integer && original != null){
				throw new RuntimeException(
						"Line " + newLine + " does not contain a spec; contents " + original.get((Integer) lineMap.get(newLine)));
			}else{
				throw new RuntimeException(
						"Line " + newLine + " does not contain a spec; contents " + lineMap.get(newLine).toString());
			}
		}
	}

	/**
	 * @return the body of the annotated file
	 */
	public String getAnnotatedBody(){
		StringBuilder builder = new StringBuilder();
		
		for (String line : annotated){
			builder.append(line).append(lineSeparator);
		}
		return builder.toString();
	}
	
	/**
	 * Find the zero-based line index for an offset in the <i>original</i> file
	 * @param offset the offset in the original file
	 * @return the zero-based line index in the original file
	 */
	public int lineForOffset(int offset){
		for (int i = 0; i < original.size(); i++){
			if (offset < original.get(i).length() + lineSeparator.length()){
				return i;
			}
			offset -= (original.get(i).length() + lineSeparator.length());
		}
		throw new RuntimeException("Offset not found: " + offset);
	}
	
	/**
	 * Returns the line number in the <i>original file</i> for the fist
	 * line containing <tt>query</tt>
	 * @param query a query string
	 * @return the line number in the <i>original file</i> for the fist
	 * line containing <tt>query</tt>
	 * @throws RuntimeException iff the query is not found
	 */
	public int firstLine(String query){
		for (int line = 0; line < original.size(); line++){
			if (original.get(line).contains(query)){
				return line;
			}
		}
		throw new RuntimeException("Query " + query + " not found");
	}
	
	/**
	 * Get the index for the first line after the JavaDoc in the original file
	 * @param startLine the starting line in the original file
	 * @return the index for the first line after the JavaDoc
	 */
	public int endOfJavaDoc(int startLine){
		if (!original.get(startLine).trim().startsWith("/**") || !original.get(startLine).trim().startsWith("*")){
			return startLine;
		}else{
			for (int i = startLine; i < original.size(); i++){
				if (original.get(i).trim().endsWith("*/")){
					return i + 1;
				}
			}
			throw new RuntimeException("Unexpected end of file");
		}
	}
	
	/**
	 * Insert clauses into the new file
	 * @param line the line in the <i>annotated file</tt> to insert the clauses at
	 * @param prefix clause prefixes (e.g., requires)
	 * @param toInsert the clauses to insert
	 */
	public void insert(int line, List<String> prefix, List<Clause> toInsert){
		if (prefix.size() != toInsert.size()){
			throw new RuntimeException("prefix / spec list size mismatch");
		}
		
		for (int i = toInsert.size() - 1; i >= 0; i--){
			String inv = "//@" + prefix.get(i) + " " + toInsert.get(i).getClause() + ";";
			annotated.add(line, inv);
			lineMap.add(line, toInsert.get(i));	
		}
		checkRep();
	}
}
