package com.schiller.veriasa.web.shared.escj;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Verification results for a type
 * @author Todd Schiller
 */
public class TypeResult implements Serializable{
	private static final long serialVersionUID = 2L;

	private String name;

	private List<MethodResult> methodResults;
	
	private List<Chunk> warnings;
	
	private boolean aborted;
	
	@SuppressWarnings("unused")
	private TypeResult(){
	}
	
	/**
	 * Create an empty type result for type <tt>name</tt>
	 * @param name the name of the type
	 */
	public TypeResult(String name) {
		super();
		this.name = name;
		this.methodResults = new LinkedList<MethodResult>();
		this.warnings = new LinkedList<Chunk>();
	}

	/**
	 * @return the type name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Add <tt>result</tt> to the type result
	 * @param result the method result
	 */
	public void addMethodResult(MethodResult result){
		methodResults.add(result);
	}
	
	/**
	 * Returns the result for method <tt>signature</tt>, or <tt>null</tt>
	 * iff no result exists for the method
	 * @param signature the query method signature
	 * @return the result for method <tt>signature</tt>
	 */
	public MethodResult getMethodResult(String signature){
		for (MethodResult result : methodResults){
			if (result.qualifiedSignature().equals(signature)){
				return result;
			}
		}
	
		return null;
	}
	
	/**
	 * @return an unmodifiable view of the method results
	 */
	public List<MethodResult> getMethodResults() {
		return Collections.unmodifiableList(methodResults);
	}

	/**
	 * @return an unmodifiable view of the type warnings
	 */
	public List<Chunk> getWarnings() {
		return Collections.unmodifiableList(warnings);
	}
	
	/**
	 * Add a specification error (e.g., undefined variable)
	 * @param the error information
	 */
	public void addWarning(Chunk warning){
		warnings.add(warning);
	}
	
	/**
	 * <tt>true</tt> iff extended type checking was aborted for the type
	 * @return <tt>true</tt> iff extended type checking was aborted for the type
	 */
	public boolean isAborted() {
		return aborted;
	}

	/**
	 * Set whether extended type checking was aborted for the type because,
	 * e.g., there was a syntax error in an invariant
	 * @param aborted true iff extended type checking was aborted for the type
	 */
	public void setAborted(boolean aborted) {
		this.aborted = aborted;
	}

	@Override
	public String toString(){
		return "[" + name + " # methods:" + methodResults.size() + "]";
	}


}