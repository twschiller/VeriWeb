package com.schiller.veriasa.distance;

import java.util.Set;

import com.google.common.collect.Sets;

/**
 * Textual difference between two specifications
 * @author Todd Schiller
 */
public class SpecDiff{
	private final Set<String> added;
	private final Set<String> removed;
	
	/**
	 * The number of specifications added and removed
	 * @return the number of specifications added and removed
	 */
	public int getDistance(){
		return added.size() + removed.size();
	}
	
	public SpecDiff(){
		added = Sets.newHashSet();
		removed = Sets.newHashSet();
	}
	
	public SpecDiff(Set<String> added, Set<String> removed) {
		super();
		this.added = added;
		this.removed = removed;
	}
	
	public Set<String> getAdded() {
		return added;
	}

	public Set<String> getRemoved() {
		return removed;
	}
	
	public String toString(){
		StringBuilder result = new StringBuilder();
		
		for (String x : added){
			result.append("add: " + x + "\n");
		}
		
		for (String x : removed){
			result.append("rem: " + x + "\n");
		}
		
		return result.toString();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((added == null) ? 0 : added.hashCode());
		result = prime * result
				+ ((removed == null) ? 0 : removed.hashCode());
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
		SpecDiff other = (SpecDiff) obj;
		if (added == null) {
			if (other.added != null)
				return false;
		} else if (!added.equals(other.added))
			return false;
		if (removed == null) {
			if (other.removed != null)
				return false;
		} else if (!removed.equals(other.removed))
			return false;
		return true;
	}
}
