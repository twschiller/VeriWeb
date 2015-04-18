package com.schiller.veriasa.web.client.problemviews;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.PopupPanel;
import com.schiller.veriasa.web.client.SubProblemCallback;
import com.schiller.veriasa.web.client.SubProblemCallback.UpdateResponseHandler;
import com.schiller.veriasa.web.client.views.ApproveObjectInvariants;
import com.schiller.veriasa.web.client.views.DndWrite;
import com.schiller.veriasa.web.client.views.InformationView;
import com.schiller.veriasa.web.client.views.Instructions;
import com.schiller.veriasa.web.client.views.MessageCenter.VoteHandler;
import com.schiller.veriasa.web.client.views.ClauseWriter.SpecDecisionHandler;
import com.schiller.veriasa.web.client.views.ClauseWriter.SpecEventHandler;
import com.schiller.veriasa.web.shared.core.Clause;
import com.schiller.veriasa.web.shared.core.Clause.Status;
import com.schiller.veriasa.web.shared.feedback.WriteEnsuresFeedback;
import com.schiller.veriasa.web.shared.messaging.UserMessage;
import com.schiller.veriasa.web.shared.messaging.UserMessage.Vote;
import com.schiller.veriasa.web.shared.messaging.UserMessageThread;
import com.schiller.veriasa.web.shared.problems.WriteEnsuresProblem;
import com.schiller.veriasa.web.shared.solutions.ImpossibleInfo;
import com.schiller.veriasa.web.shared.solutions.WriteEnsuresSolution;
import com.schiller.veriasa.web.shared.update.WriteEnsuresUpdate;
import com.schiller.veriasa.web.shared.util.SpecUtil;

public class WriteEnsures extends WriteView implements IView{
	
	private int pendingCount = 0;
	
	private SubProblemCallback<WriteEnsuresSolution,WriteEnsuresUpdate,WriteEnsuresFeedback> callback;
	
	private final WriteEnsuresProblem problem;
	
	private List<Clause> pendingObjectInvariants = new ArrayList<Clause>();
	private List<Clause> approvedObjectInvariants = new ArrayList<Clause>();
	
	/**
	 * Build a write-ensures clause subproblem
	 * @param problem the problem
	 * @param callback the callback to use
	 */
	public WriteEnsures(final WriteEnsuresProblem problem, 
			SubProblemCallback<WriteEnsuresSolution,WriteEnsuresUpdate,WriteEnsuresFeedback> callback) {	
		
		super("Write Postconditions", DndWrite.ContractMode.POST, new WritePlug(){
			@Override
			public List<Clause> getTried() {
				return problem.getKnown();
			}
			@Override
			public List<Clause> getKnown() {
				return SpecUtil.goodOrPending(problem.getKnown());
			}
		});
		
		this.problem = problem;
		
		this.callback = callback;
		
		callback.updateCode(problem.getAnnotatedBody(), 
				problem.getFunction().getInfo().getSrcLanguage(),
				true);
	
		//check all in parallel
		writer.addSpecs(SpecUtil.goodOrPending(problem.getKnown()), true);
	}
	
	private class ResponseHandler implements UpdateResponseHandler<WriteEnsuresFeedback>{
		private final SpecDecisionHandler handler;
		
		public ResponseHandler(SpecDecisionHandler handler) {
			super();
			this.handler = handler;
		}
		
		@Override
		public void onUpdateResponse(WriteEnsuresFeedback feedback) {
			handler.onDecision(feedback.getStatement());
		
			if (feedback.hasDynamicFeedback()){
				writer.attachDynamicFeedback(feedback.getStatement(), feedback.getDynamicFeedback());
			}
			
			if (feedback.promptAsObjectInvariant()){
				pendingObjectInvariants.add(feedback.getStatement());
			}
			
			if (--pendingCount == 0){
				doneButton.setEnabled(true);
			}
		}
		
		@Override
		public void onUpdateResponse(List<WriteEnsuresFeedback> responses) {
			for (WriteEnsuresFeedback feedback : responses){
				onUpdateResponse(feedback);
			}
		}	
	}

	@Override
	protected SpecEventHandler getSpecEventHandler() {
		return new SpecEventHandler(){
			@Override
			public void added(final String spec, final SpecDecisionHandler handler) {
				pendingCount++;
				doneButton.setEnabled(false);
				WriteEnsures.this.callback.onUpdate(new WriteEnsuresUpdate(new Clause(spec,"WEB",Status.PENDING,"")),new ResponseHandler(handler));
			}
			@Override
			public void removed(String spec) {
				throw new RuntimeException("Cannot remove post-conditions");
			}
			@Override
			public void added(List<String> spec, SpecDecisionHandler handler) {
				pendingCount += spec.size();
				doneButton.setEnabled(false);
				
				ArrayList<WriteEnsuresUpdate> s = new ArrayList<WriteEnsuresUpdate>();
				for (String i : spec){
					s.add(new WriteEnsuresUpdate(new Clause(i,"WEB",Status.PENDING,"")));
				}
				WriteEnsures.this.callback.onUpdate(s,new ResponseHandler(handler));
			}
		};
	}

	@Override
	public List<Clause> getAssumedRequires() {
		return problem.getRequires();
	}
	
	@Override
	public VoteHandler getVoteHandler() {
		return new VoteHandler(){
			@Override
			public void recordVote(UserMessageThread thread, UserMessage message, Vote vote, UserMessage response) {
				WriteEnsures.this.callback.onVote(thread, message, vote, response);
			}
		};
	}

	@Override
	public void setInformationView(InformationView d) {
		writer.setDocPanel(d);
	}
	
	@Override
	void submitIssue(List<Clause> specs, ImpossibleInfo info) {
		callback.onFinish(new WriteEnsuresSolution(specs, approvedObjectInvariants, info));
	}
	
	@Override
	void finish() {
		if (pendingObjectInvariants.isEmpty()){
			callback.onFinish((new WriteEnsuresSolution(writer.getSpecs(), approvedObjectInvariants)));
		}else{
			doneButton.setEnabled(false);
			
			final PopupPanel dialog = new PopupPanel();
			
			ApproveObjectInvariants form = new ApproveObjectInvariants(pendingObjectInvariants, new ApproveObjectInvariants.Callback() {
				@Override
				public void onSubmit(List<Clause> approved) {
					approvedObjectInvariants = approved;
					dialog.hide();
					callback.onFinish((new WriteEnsuresSolution(writer.getSpecs(), approvedObjectInvariants)));
				}
			});
			
			dialog.add(form);
			dialog.center();
			dialog.show();
		}
	}
	
	@Override
	public SafeHtml getInstructionHtml() {
		return SafeHtmlUtils.fromTrustedString("Drag condition fragments from the palette to the condition " 
			 + "box to form expressions that are true when the function returns; write as many expressions as you can "
			 + "without repeating yourself.");
	}
	
	@Override
	public Instructions getInstructions() {
		return new Instructions(getInstructionHtml(), true, false);
	}
}
