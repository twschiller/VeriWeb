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
import com.schiller.veriasa.web.shared.feedback.WriteExsuresFeedback;
import com.schiller.veriasa.web.shared.messaging.UserMessage;
import com.schiller.veriasa.web.shared.messaging.UserMessage.Vote;
import com.schiller.veriasa.web.shared.messaging.UserMessageThread;
import com.schiller.veriasa.web.shared.problems.WriteExsuresProblem;
import com.schiller.veriasa.web.shared.solutions.ImpossibleInfo;
import com.schiller.veriasa.web.shared.solutions.WriteExsuresSolution;
import com.schiller.veriasa.web.shared.update.WriteExsuresUpdate;
import com.schiller.veriasa.web.shared.util.SpecUtil;

public class WriteExsures extends WriteView {

	private int pendingCount = 0;
	
	private SubProblemCallback<WriteExsuresSolution,WriteExsuresUpdate,WriteExsuresFeedback> callback;
		
	private final WriteExsuresProblem problem;
	
	private List<Clause> pendingObjectInvariants = new ArrayList<Clause>();
	private List<Clause> approvedObjectInvariants = new ArrayList<Clause>();
	
	/**
	 * Build a write-ensures clause subproblem
	 * @param problem the problem
	 * @param callback the callback to use
	 */
	public WriteExsures(
			final WriteExsuresProblem problem, 
			SubProblemCallback<WriteExsuresSolution,WriteExsuresUpdate,WriteExsuresFeedback> callback) {
		
		super("Write Postconditions for " + problem.getException(), DndWrite.ContractMode.POST, new WritePlug(){
			@Override
			public List<Clause> getTried() {
				return problem.getKnown();
			}
			@Override
			public List<Clause> getKnown() {
				return SpecUtil.goodOrPending(problem.getKnown());
			}
		});
		
		this.callback = callback;
		this.problem = problem;
		
		callback.updateCode(problem.getAnnotatedBody(), problem.getFunction().getInfo().getSrcLanguage(), true);
		
		//check all in parallel
		writer.addSpecs(SpecUtil.goodOrPending(problem.getKnown()), true);
	}
	
	@Override
	void submitIssue(List<Clause> statements, ImpossibleInfo info) {
		callback.onFinish(new WriteExsuresSolution(statements, approvedObjectInvariants, info));
	}
	
	@Override
	void finish() {
		if (pendingObjectInvariants.isEmpty()){
			callback.onFinish((new WriteExsuresSolution(writer.getSpecs(), approvedObjectInvariants)));
		}else{
			doneButton.setEnabled(false);
			
			final PopupPanel dialog = new PopupPanel();
			
			ApproveObjectInvariants form = new ApproveObjectInvariants(pendingObjectInvariants, new ApproveObjectInvariants.Callback() {
				@Override
				public void onSubmit(List<Clause> approved) {
					approvedObjectInvariants = approved;
					dialog.hide();
					callback.onFinish((new WriteExsuresSolution(writer.getSpecs(), approvedObjectInvariants)));
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
				+ "box to form expressions that are true when the method throws the specified exception; "
				+ "write as many expressions as you can without repeating yourself");
	}
	@Override
	public Instructions getInstructions() {
		return new Instructions(getInstructionHtml(), true,false);
	}
	
	private class ResponseHandler implements UpdateResponseHandler<WriteExsuresFeedback>{
		private final SpecDecisionHandler handler;
		
		public ResponseHandler(SpecDecisionHandler handler) {
			super();
			this.handler = handler;
		}
		
		@Override
		public void onUpdateResponse(WriteExsuresFeedback response) {
			handler.onDecision(response.getStatement());
		
			if (response.promptAsObjectInvariant()){
				pendingObjectInvariants.add(response.getStatement());
			}
			
			if (--pendingCount == 0){
				doneButton.setEnabled(true);
			}
		}

		@Override
		public void onUpdateResponse(List<WriteExsuresFeedback> responses) {
			for (WriteExsuresFeedback feedback : responses){
				onUpdateResponse(feedback);
			}
		}	
	}
	
	
	@Override
	protected SpecEventHandler getSpecEventHandler() {
		return new SpecEventHandler(){	
			@Override
			public void added(final String statement, final SpecDecisionHandler handler) {
				pendingCount++;
				doneButton.setEnabled(false);
				WriteExsures.this.callback.onUpdate(new WriteExsuresUpdate(new Clause(statement,"WEB",Status.PENDING,"")), new ResponseHandler(handler));
			}
			@Override
			public void removed(String statement) {
				throw new RuntimeException("Cannot remove postconditions");
			}
			@Override
			public void added(List<String> statement, SpecDecisionHandler handler) {
				pendingCount += statement.size();
				doneButton.setEnabled(false);
				
				ArrayList<WriteExsuresUpdate> s = new ArrayList<WriteExsuresUpdate>();
				for (String i : statement){
					s.add(new WriteExsuresUpdate(new Clause(i,"WEB",Status.PENDING,"")));
				}
				WriteExsures.this.callback.onUpdate(s,new ResponseHandler(handler));
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
				WriteExsures.this.callback.onVote(thread, message, vote, response);
			}
		};
	}
	
	@Override
	public void setInformationView(InformationView d) {
		writer.setDocPanel(d);
	}
}
