package com.schiller.veriasa.web.shared.escj;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * An error, caution, warning, or other message from ESC/Java2
 * @author Todd Schiller
 */
public class Chunk implements Serializable{
	private static final long serialVersionUID = 2L;

	public static enum ChunkType {WARNING, ERROR, CAUTION, UNKNOWN, FATAL_ERROR}

	private String filePath;
	private ChunkType type;
	
	private int line;
	private String message;
	
	private String badLine;
	private int badLineOffset = -1;
	
	private AssociatedDeclaration associated;
	private List<TraceEntry> trace = null;
		
	@SuppressWarnings("unused")
	private Chunk(){
	}
	
	public static ChunkType chunkType(String header){
		String upper = header.toUpperCase();
		if (upper.equals("WARNING")){
			return ChunkType.WARNING;
		}else if (upper.equals("ERROR")){
			return ChunkType.ERROR;
		}else if (upper.equals("CAUTION")){
			return ChunkType.CAUTION;
		}else if (upper.equals("FATAL ERROR")){
			return ChunkType.FATAL_ERROR;
		}else{
			return ChunkType.UNKNOWN;
		}
	}
	
	public void addTraceEntry(TraceEntry entry){
		if (trace == null){
			trace = new LinkedList<TraceEntry>();
		}
		trace.add(entry);
	}
	
	public boolean hasTrace(){
		return trace != null;
	}
	
	public List<TraceEntry> getTrace(){
		return trace == null ? null : new LinkedList<TraceEntry>(trace);
	}
	
	/**
	 * Create an ESC/Java2 warning chunk with no associated file
	 */
	public Chunk(int line, String type, String message, String badLine, int badLineOffset) {
		this(null, line, type, message, badLine, badLineOffset);
	}
	
	/**
	 * Create an ESC/Java2 warning chunk
	 */
	public Chunk(String filePath, int line, String type, String message,
			String badLine, int badLineOffset) {
	
		this.filePath = filePath;
		this.line = line;
		this.type = chunkType(type);
		this.message = message;
		this.badLine = badLine;
		this.badLineOffset = badLineOffset;
	}

	@Override
	public String toString() {
		return type + ":" +  message ;
	}
	
	/**
	 * Get the line number of the error in the annotated file
	 * @return the line number of the error in the annotated file
	 */
	public int getLine() {
		return line;
	}

	/**
	 * Get the contents of the line of interest
	 * @return the contents of the line of interest
	 */
	public String getBadLine() {
		return badLine;
	}

	/**
	 * Get the offset of the item of interest in the bad line.
	 * This is the offset of the caret in the ESC/Java2 output
	 * @see getBadLine()
	 * @return the offset of the item of interest in the bad line
	 */
	public int getBadLineOffset() {
		return badLineOffset;
	}
	
	/**
	 * @return the file path associated with the warning, or <tt>null</tt> if there is no associated
	 * file
	 */
	public String getFilePath() {
		return filePath;
	}

	/**
	 * Get the type of message (e.g., error or warning) 
	 * @return the type of the message (e.g., error or warning)
	 */
	public ChunkType getMessageType() {
		return type;
	}

	public AssociatedDeclaration getAssociatedDeclaration() {
		return associated;
	}

	/**
	 * @deprecated 
	 */
	public void setAssociatedDeclaration(AssociatedDeclaration associatedDeclaration) {
		this.associated = associatedDeclaration;
	}

	/**
	 * Get the message text 
	 * @return the message text
	 */
	public String getMessage() {
		return message;
	}

	public static class TraceEntry implements Serializable{
		private static final long serialVersionUID = 2L;
		
		public String what;
		public String compilationUnit;
		public int line;
		public int col;
		
		@SuppressWarnings("unused")
		private TraceEntry(){
		}

		public TraceEntry(String what, String compilationUnit, int line, int col) {
			super();
			this.what = what;
			this.compilationUnit = compilationUnit;
			this.line = line;
			this.col = col;
		}

		public String getWhat() {
			return what;
		}

		public String getCompilationUnit() {
			return compilationUnit;
		}

		public int getLine() {
			return line;
		}

		public int getCol() {
			return col;
		}

		@Override
		public String toString() {
			return "TraceEntry [what=" + what + ", line=" + line + ", col=" + col + "]";
		}
	}
	
	public static class AssociatedDeclaration implements Serializable{
		private static final long serialVersionUID = 2L;
		
		private String filePath;
		private int line;
		private int col;

		private String contents;
		private int offset;

		@SuppressWarnings("unused")
		private AssociatedDeclaration(){
		}
		
		public AssociatedDeclaration(String filePath, int line, int col, String contents, int offset) {
			this.filePath = filePath;
			this.line = line;
			this.col = col;
			this.contents = contents;
			this.offset = offset;
		}

		public int getOffset() {
			return offset;
		}

		public String getFilePath() {
			return filePath;
		}

		public String getContents() {
			return contents;
		}

		public int getLine() {
			return line;
		}

		public int getCol() {
			return col;
		}
	}

}
	
