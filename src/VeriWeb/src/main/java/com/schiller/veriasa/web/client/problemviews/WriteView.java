package com.schiller.veriasa.web.client.problemviews;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.schiller.veriasa.web.client.views.DndWrite;
import com.schiller.veriasa.web.client.views.InformationView.DocumentationHandler;
import com.schiller.veriasa.web.client.views.IssueView;
import com.schiller.veriasa.web.client.views.ClauseWriter;
import com.schiller.veriasa.web.client.views.ClauseWriter.ExistsCheck;
import com.schiller.veriasa.web.client.views.ClauseWriter.SpecEventHandler;
import com.schiller.veriasa.web.client.views.WidthProvider;
import com.schiller.veriasa.web.shared.core.Clause;
import com.schiller.veriasa.web.shared.solutions.ImpossibleInfo;
import com.schiller.veriasa.web.shared.solutions.ImpossibleInfo.Reason;

abstract public class WriteView extends Composite implements IView, RequiresResize{
	
	private static WriteViewUiBinder uiBinder = GWT
		.create(WriteViewUiBinder.class);

	interface WriteViewUiBinder extends UiBinder<Widget, WriteView> {
	}

	@UiField
	VerticalPanel main;
	
	@UiField 
	Button doneButton;
	
	@UiField
	ClauseWriter writer;
	
	@UiField
	Label tagline;
	
	interface WritePlug{
		List<Clause> getTried();
		List<Clause> getKnown();
	}
	
	@Override
	public void onResize(){
		writer.onResize();
	}
	
	abstract protected SpecEventHandler getSpecEventHandler();
	
	public WriteView(String tagline, DndWrite.ContractMode mode, final WritePlug plug) {
		initWidget(uiBinder.createAndBindUi(this));
		
		this.tagline.setText(tagline + ":");
		
		writer.setMode(mode);
		
		writer.setExistsCheck(new ExistsCheck(){
			@Override
			public boolean alreadyExists(String spec) {
				for (Clause s : plug.getTried()){
					if (s.getClause().trim().equals(spec.trim())){
						return true;
					}
				}
				return false;
			}
		});

		writer.setSpecEventHandler(getSpecEventHandler());
		
		writer.setWidthProvider(new WidthProvider(){
			@Override
			public int getWidth() {
				return WriteView.this.getParent().getOffsetWidth();
			}
		});
	
	}
	
	abstract void submitIssue(List<Clause> specs, ImpossibleInfo info);
	
	void showIssueDialog(final String method, final Reason reason){
		showIssueDialog(method, reason, null);
	}
	
	void showIssueDialog(final String method, final Reason reason, final Clause spec){
		final PopupPanel notCompleted = new PopupPanel();
		
		writer.setCanWrite(false);
		
		notCompleted.clear();
		IssueView form = new IssueView(reason);
		notCompleted.add(form);
		
		form.setHandler(new IssueView.DialogHandler(){

			@Override
			public void onOk(SafeHtml comment) {
				submitIssue(writer.getSpecs(), 
						spec == null ? new ImpossibleInfo(reason, method, comment.asString())
									: new ImpossibleInfo(reason, method, spec, comment.asString()));
				notCompleted.hide();
			}
			@Override
			public void onCancel() {
				notCompleted.hide();
				writer.setCanWrite(true);
			}
		});
		notCompleted.center();
		notCompleted.show();
		form.focus();
	}

	@Override
	public DocumentationHandler getDocHandler() {
		return new DocumentationHandler(){
			@Override
			public void addEnsures(String method) {
				showIssueDialog(method, Reason.WEAK_ENS);
			}
			@Override
			public void addExsures(String method, String ex) {
				showIssueDialog(method, Reason.WEAK_EXS);
			}
			@Override
			public void removeRequires(String method, Clause spec) {
				showIssueDialog(method, Reason.STRONG_REQ, spec);
			}
			@Override
			public void copyStatement(Clause spec) {
				writer.addToScratch(spec.getClause());
			}
			@Override
			public boolean canCopy() {
				return true;
			}
		};
	}
	
	abstract void finish();

	@UiHandler("doneButton")
	void onDone(ClickEvent e) {
		finish();
	}
	
}
