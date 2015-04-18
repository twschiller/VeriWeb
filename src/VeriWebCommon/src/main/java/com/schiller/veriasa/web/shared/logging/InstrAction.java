package com.schiller.veriasa.web.shared.logging;

import java.io.Serializable;

public class InstrAction extends LogAction implements Serializable {
	private static final long serialVersionUID = 1L;

	public enum ActionType {Show, Hide};
	
	public ActionType type;
	
	public InstrAction(ActionType type){		
	}
}
