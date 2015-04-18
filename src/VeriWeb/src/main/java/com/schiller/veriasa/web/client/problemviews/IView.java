package com.schiller.veriasa.web.client.problemviews;

import java.util.List;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.Widget;
import com.schiller.veriasa.web.client.views.Instructions;
import com.schiller.veriasa.web.client.views.InformationView;
import com.schiller.veriasa.web.client.views.InformationView.DocumentationHandler;
import com.schiller.veriasa.web.client.views.MessageCenter.VoteHandler;
import com.schiller.veriasa.web.shared.core.Clause;

public interface IView {
	public DocumentationHandler getDocHandler();
	public Widget asWidget();
	public SafeHtml getInstructionHtml();
	public Instructions getInstructions();
	public List<Clause> getAssumedRequires();
	public VoteHandler getVoteHandler();
	public void setInformationView(InformationView view);
}
