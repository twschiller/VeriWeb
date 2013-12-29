package com.schiller.veriasa.web.server.escj;

import java.util.List;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.schiller.veriasa.web.shared.escj.Chunk;
import com.schiller.veriasa.web.shared.escj.Chunk.ChunkType;
import com.schiller.veriasa.web.shared.escj.MethodResult;
import com.schiller.veriasa.web.shared.escj.ProjectResult;
import com.schiller.veriasa.web.shared.escj.TypeResult;

/**
 * Utility classes for interacting with ESC/Java2
 * @author Todd Schiller
 */
public final class EscJUtil {

	/**
	 * true iff ESC/Java2 chunk refers to postcondition
	 */
	public static final Predicate<Chunk> ONLY_POST = new Predicate<Chunk>(){
		@Override
		public boolean apply(Chunk warning) {
			return warning.getMessage().trim().endsWith("(Post)");
		}
	};
	
	/**
	 * true iff the ESC/Java2 chunk is an error or warning
	 */
	public static final Predicate<Chunk> IS_FATAL = new Predicate<Chunk>(){
		@Override
		public boolean apply(Chunk warning) {
			return warning.getMessageType() == Chunk.ChunkType.ERROR 
					|| warning.getMessageType() == Chunk.ChunkType.WARNING
					|| warning.getMessageType() == Chunk.ChunkType.FATAL_ERROR;
		}
	};
	
	/**
	 * returns <code>true</code> iff <code>result</code> contains any error or warning messages
	 * ({@link ChunkType#ERROR}, {@link ChunkType#WARNING}, or {@link ChunkType#FATAL_ERROR})
	 * @param result the ESC/Java2 result
	 * @return returns <code>true</code> iff <code>result</code> contains any error or warning messages
	 */
	public static final boolean hasErrors(ProjectResult result){
		if (result.hasFatalProjError() || Iterables.any(result.getSpecErrors(), IS_FATAL)){
			return true;
		}
	
		for (TypeResult type : result.getTypeResults()){
			if (Iterables.any(type.getWarnings(), IS_FATAL)){
				return true;
			}
			for (MethodResult method : type.getMethodResults()){
				if (Iterables.any(method.getWarnings(), IS_FATAL)){
					return true;
				}
			}
		}
		return false;
	}
	
	
	/**
	 * true iff ESC/Java2 chunk does not refer to post condition
	 */
	public static final Predicate<Chunk> requiresExFilter = new Predicate<Chunk>(){
		@Override
		public boolean apply(Chunk warning) {
			return !warning.getMessage().trim().endsWith("(Post)");
		}
	};
	
	/**
	 * Get whether or not a set of method chunks indicates that method
	 * (1) won't raise a runtime exception (2) meets all of it's callee
	 * pre-conditions
	 * @param methodChunks the verifier chunks for the method
	 * @return true iff the method chunks indicates that a method's pre-conditions
	 * are sufficient
	 */
	public static boolean isSufficient(List<Chunk> methodChunks){
		return !Iterables.any(methodChunks, new Predicate<Chunk>(){
			@Override
			public boolean apply(Chunk warning) {
				return warning.getMessageType().equals(ChunkType.WARNING);
			}
		});
	}
	
	/**
	 * Returns true iff <code>warning</code> is for an unsatisfied precondition
	 * @param warning the ESC/Java2 warning
	 * @return true iff <code>warning</code> is for an unsatisfied precondition
	 */
	public static boolean isUnsatisfiedPreconditionWarning(Chunk warning){
		return warning.getMessage().trim().toUpperCase().endsWith("(PRE)");
	}
	
	/**
	 * Returns true iff <code>warning</code> is associated with a Java source file.
	 * @param warning the ESC/Java2 warning
	 * @return true iff <code>warning</code> is associated with a Java source file.
	 */
	public static boolean isJavaSourceWarning(Chunk warning){
		return warning.getFilePath() != null && warning.getFilePath().endsWith(".java");
	}
	
	/**
	 * Return the Java compilation unit associated with <code>warning</code>
	 * @param warning the ESC/Java2 warning
	 * @return the compilation unit associated with <code>warning</code>
	 */
	public static String getCompilationUnit(Chunk warning){
		if (!isJavaSourceWarning(warning)){
			throw new IllegalArgumentException("No Java compilation unit is associated with the warning");
		}
		return warning.getFilePath().substring(warning.getFilePath().lastIndexOf('/') + 1);
	}
	
}
