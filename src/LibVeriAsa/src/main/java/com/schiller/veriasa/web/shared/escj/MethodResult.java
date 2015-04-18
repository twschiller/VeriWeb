package com.schiller.veriasa.web.shared.escj;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.schiller.veriasa.web.shared.core.HasQualifiedSignature;

/**
 * Verification results for a method
 * @author Todd Schiller
 */
public class MethodResult implements Serializable, HasQualifiedSignature{
	private static final long serialVersionUID = 2L;
	
	private String type;
	private String signature;
	
	private List<Chunk> warnings;
	
	@SuppressWarnings("unused")
	private MethodResult(){
	}
	
	/**
	 * Create an empty result for method <tt>signature</tt> in <tt>type</tt>
	 * @param type the type
	 * @param signature the signature of the method
	 */
	public MethodResult(String type, String signature) {
		this.type = type;
		this.signature = signature;
		this.warnings = new LinkedList<Chunk>();
	}
	
	/**
	 * Add <tt>warning</tt> to the method results
	 * @param warning the warning to add
	 */
	public void addWarning(Chunk warning){
		warnings.add(warning);
	}
	
	/**
	 * @return the type the method is contained in
	 */
	public String getType() {
		return type;
	}
	
	/**
	 * @deprecated use {@link MethodResult#qualifiedSignature()}
	 */
	public String getSignature() {
		return signature;
	}
	
	@Override
	public String qualifiedSignature() {
		return type + "." + signature;
	}
	
	@Override
	public String toString(){
		return "[" + type + "." + signature + "]";
	}

	/**
	 * @return an unmodifiable view of the method warnings
	 */
	public List<Chunk> getWarnings() {
		return Collections.unmodifiableList(warnings);
	}
}

