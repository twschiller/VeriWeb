package com.schiller.veriasa.web.shared;

import java.io.Serializable;

@SuppressWarnings("serial")
public final class SrcLoc implements Serializable{
	private int offset;
	private int length;
	private String compilationUnit;
	
	@SuppressWarnings("unused")
	private SrcLoc(){
		
	}
	
	public SrcLoc(String compilationUnit, int offset, int length){
		this.compilationUnit = compilationUnit;
		this.offset = offset;
		this.length = length;
	}

	public int getOffset() {
		return offset;
	}

	public int getLength() {
		return length;
	}

	public String getCompilationUnit() {
		return compilationUnit;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((compilationUnit == null) ? 0 : compilationUnit.hashCode());
		result = prime * result + length;
		result = prime * result + offset;
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
		SrcLoc other = (SrcLoc) obj;
		if (compilationUnit == null) {
			if (other.compilationUnit != null)
				return false;
		} else if (!compilationUnit.equals(other.compilationUnit))
			return false;
		if (length != other.length)
			return false;
		if (offset != other.offset)
			return false;
		return true;
	}
	
	
}