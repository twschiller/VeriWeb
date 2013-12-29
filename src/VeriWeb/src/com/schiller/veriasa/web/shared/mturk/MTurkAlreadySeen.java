package com.schiller.veriasa.web.shared.mturk;

import java.io.Serializable;

import com.schiller.veriasa.web.shared.problems.NoProblemInfo;

/**
 * No problem is given to the MTurk worker because they have already
 * performed another HIT in the batch
 * @author Todd Schiller
 */
public class MTurkAlreadySeen implements NoProblemInfo, Serializable {

	private static final long serialVersionUID = 1L;

	public MTurkAlreadySeen(){		
	}
}
