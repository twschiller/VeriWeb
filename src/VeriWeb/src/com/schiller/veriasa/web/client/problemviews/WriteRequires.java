package com.schiller.veriasa.web.client.problemviews;

import java.util.LinkedList;
import java.util.List;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.schiller.veriasa.web.client.SubProblemCallback;
import com.schiller.veriasa.web.client.SubProblemCallback.UpdateResponseHandler;
import com.schiller.veriasa.web.client.views.Instructions;
import com.schiller.veriasa.web.client.views.DndWrite;
import com.schiller.veriasa.web.client.views.InformationView;
import com.schiller.veriasa.web.client.views.MessageCenter.VoteHandler;
import com.schiller.veriasa.web.client.views.ClauseWriter.SpecDecisionHandler;
import com.schiller.veriasa.web.client.views.ClauseWriter.SpecEventHandler;
import com.schiller.veriasa.web.shared.core.Clause;
import com.schiller.veriasa.web.shared.core.Clause.Status;
import com.schiller.veriasa.web.shared.feedback.WriteRequiresFeedback;
import com.schiller.veriasa.web.shared.messaging.UserMessage;
import com.schiller.veriasa.web.shared.messaging.UserMessage.Vote;
import com.schiller.veriasa.web.shared.messaging.UserMessageThread;
import com.schiller.veriasa.web.shared.problems.WriteRequiresProblem;
import com.schiller.veriasa.web.shared.solutions.ImpossibleInfo;
import com.schiller.veriasa.web.shared.solutions.WriteRequiresSolution;
import com.schiller.veriasa.web.shared.update.WriteRequiresUpdate;

public class WriteRequires extends WriteView  implements IView{
	private final static boolean HIGHLIGHT = false;
	
	private SubProblemCallback<WriteRequiresSolution,WriteRequiresUpdate,WriteRequiresFeedback> callback;

	private boolean sufficient;
	
	private int pendingCount = 0;
	
	final WriteRequiresProblem problem;
	
	/**
	 * Build a write-ensures clause subproblem
	 * @param problem the problem
	 * @param callback the callback to use
	 */
	public WriteRequires(final WriteRequiresProblem problem, 
			final SubProblemCallback<WriteRequiresSolution,
			WriteRequiresUpdate,
			WriteRequiresFeedback> callback) {
		
		super("Write Preconditions", DndWrite.ContractMode.PRE, new WritePlug(){
			@Override
			public List<Clause> getTried() {
				return problem.getKnown();
			}
			@Override
			public List<Clause> getKnown() {
				List<Clause> known = new LinkedList<Clause>();
				for (Clause choice : problem.getKnown()){
					if (choice.getStatus().equals(Status.KNOWN_GOOD)){
						known.add(choice);
					}
				}
				return known;
			}
		});

		this.problem = problem;
		
		this.callback = callback;
				
		callback.updateCode(problem.getAnnotatedBody(), 
				problem.getFunction().getInfo().getSrcLanguage(),
				true);
		
		sufficient = problem.isSufficient();
		doneButton.setEnabled(sufficient);
		
		List<Clause> known = new LinkedList<Clause>();
		for (Clause choice : problem.getKnown()){
			if (choice.getStatus().equals(Status.KNOWN_GOOD)){
				known.add(choice);
			}
		}
		writer.addSpecs(known);
		for (Clause s : problem.getFunction().getRequires()){
			if (!known.contains(s)){
				writer.addToScratch(s.getClause());
			}
		}
	}

	@Override
	void submitIssue(List<Clause> specs, ImpossibleInfo info) {
		callback.onFinish(new WriteRequiresSolution(specs,info));
	}

	@Override
	void finish() {
		callback.onFinish((new WriteRequiresSolution(writer.getSpecs())));
	}
	
	@Override
	public SafeHtml getInstructionHtml() {
		return SafeHtmlUtils.fromTrustedString("Drag condition fragments from the palette to the condition "
				+"box to form conditions that <b>MUST</b> be true for the function to not throw an "
				+"unexpected <b>runtime</b> exception. A submit button will appear when the condition "
				+"in the box is complete.");
	}
	@Override
	public Instructions getInstructions() {
		return new Instructions(getInstructionHtml(), true,true);
	}

	@Override
	protected SpecEventHandler getSpecEventHandler() {
		return new SpecEventHandler(){
			
			@Override
			public void added(final String spec, final SpecDecisionHandler handler) {
				pendingCount++;
				doneButton.setEnabled(false);
				
				WriteRequires.this.callback.onUpdate(new WriteRequiresUpdate(writer.getSpecs()),
						new UpdateResponseHandler<WriteRequiresFeedback>(){
							@Override
							public void onUpdateResponse(WriteRequiresFeedback response) {
								
								callback.updateCode(response.getAnnotatedBody(), 
										problem.getFunction().getInfo().getSrcLanguage(),
										HIGHLIGHT);

								for (Clause s : response.getFeedback()){
									if (s.getClause().equals(spec)){
										handler.onDecision(s);
									}
								}
								
								Clause last = response.getFeedback().get(response.getFeedback().size()-1);
								if (response.hasDynamicFeedback()){
									writer.attachDynamicFeedback(last, response.getDynamicFeedback());
								}
								
								sufficient = response.isSufficient();

								pendingCount--;
								if (pendingCount == 0 ){
									doneButton.setEnabled(sufficient);
								}
								
							}

							@Override
							public void onUpdateResponse(
									List<WriteRequiresFeedback> responses) {
								throw new RuntimeException();
							}
				});
			}

			@Override
			public void removed(String spec) {
				pendingCount++;
				doneButton.setEnabled(false);
				
				WriteRequires.this.callback.onUpdate(new WriteRequiresUpdate(writer.getSpecs()),
						new UpdateResponseHandler<WriteRequiresFeedback>(){
							@Override
							public void onUpdateResponse(WriteRequiresFeedback response) {
								if (response != null){
									callback.updateCode(response.getAnnotatedBody(), 
											problem.getFunction().getInfo().getSrcLanguage(),
											HIGHLIGHT);

									if (response.hasDynamicFeedback()){
										throw new RuntimeException("Removing a specification should not result in a dynamic error");
									}
									
									sufficient = response.isSufficient();

									pendingCount--;
									if (pendingCount == 0 ){
										doneButton.setEnabled(sufficient);
									}
								}
							}

							@Override
							public void onUpdateResponse(
									List<WriteRequiresFeedback> responses) {
								throw new RuntimeException();
							}
				});
			}

			@Override
			public void added(List<String> spec, SpecDecisionHandler handler) {
				throw new RuntimeException();
			}
			
		};
	}
	@Override
	public List<Clause> getAssumedRequires() {
		return null;
	}
	
	@Override
	public VoteHandler getVoteHandler() {
		return new VoteHandler(){
			@Override
			public void recordVote(UserMessageThread thread, UserMessage message, Vote vote, UserMessage response) {
				WriteRequires.this.callback.onVote(thread, message, vote, response);
			}
		};
	}

	@Override
	public void setInformationView(InformationView d) {
		writer.setDocPanel(d);
	}
	
	
}
