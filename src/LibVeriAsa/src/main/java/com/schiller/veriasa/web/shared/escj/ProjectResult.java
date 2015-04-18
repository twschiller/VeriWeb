package com.schiller.veriasa.web.shared.escj;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.schiller.veriasa.web.shared.escj.Chunk.ChunkType;

public class ProjectResult implements Serializable{
	private static final long serialVersionUID = 1L;

	private int id;
	private List<TypeResult> typeResults;
	private Map<String,AnnotatedFile> lineMaps;
	private List<Chunk> specErrors;
	
	public ProjectResult(
			int id,
			List<TypeResult> typeResults,
			List<Chunk> specErrors,
			Map<String, AnnotatedFile> lineMaps) {
		
		this.id = id;
		this.typeResults = new LinkedList<TypeResult>(typeResults);
		
		//TODO: The value lists are probably modifiable from outside this data structure
		this.lineMaps = new HashMap<String,AnnotatedFile>(lineMaps);
		
		this.specErrors = new LinkedList<Chunk>(specErrors);
	}
	
	public int getId() {
		return id;
	}

	public TypeResult getTypeResult(String fullyQualifiedName){
		for (TypeResult tr : typeResults){
			if (tr.getName().equals(fullyQualifiedName)){
				return tr;
			}
		}
		
		return null;
	}
	
	public List<TypeResult> getTypeResults() {
		return Collections.unmodifiableList(typeResults);
	}

	public List<Chunk> getSpecErrors(){
		return Collections.unmodifiableList(specErrors);
	}
	
	public boolean hasFatalProjError(){
		for (Chunk c : specErrors){
			if (c.getMessageType().equals(ChunkType.FATAL_ERROR)){
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Get a mapping from compilation unit to line map.
	 * Each index in a line map corresponds to a line in the
	 * annotated file. The value for a particular index is the
	 * corresponding line in the unannotated file (or the spec that
	 * is now located at the line).
	 * @return a mapping from compilation unit to line maps
	 */
	public Map<String, AnnotatedFile> getLineMaps() {
		return Collections.unmodifiableMap(lineMaps);
	}
}
