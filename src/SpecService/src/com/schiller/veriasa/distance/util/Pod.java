package com.schiller.veriasa.distance.util;

import com.schiller.veriasa.web.shared.core.ProjectSpecification;

/**
 * A common interface for time-stamped specifications
 * @author Todd Schiller
 */
public interface Pod{
	public long getTimestamp();
	public String getUser();
	public ProjectSpecification getSpec();
	public int getPenalty();
}