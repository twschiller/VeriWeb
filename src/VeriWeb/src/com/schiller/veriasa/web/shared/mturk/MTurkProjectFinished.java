package com.schiller.veriasa.web.shared.mturk;

import java.io.Serializable;

import com.schiller.veriasa.web.shared.problems.NoProblemInfo;
import com.schiller.veriasa.web.shared.problems.ProjectFinished;

/**
 * No problem is available to the MTurk worker because the project is finished
 * @author Todd Schiller
 */
public class MTurkProjectFinished extends ProjectFinished implements NoProblemInfo, Serializable {
	private static final long serialVersionUID = 1L;

	private MTurkProgress progress;

	@SuppressWarnings("unused")
	private MTurkProjectFinished(){
	}
	
	/**
	 * @param progress the final progress information
	 */
	public MTurkProjectFinished(MTurkProgress progress){
		this.progress = progress;
	}

	/**
	 * @return the final progress information
	 */
	public MTurkProgress getProgress() {
		return progress;
	}	
}
