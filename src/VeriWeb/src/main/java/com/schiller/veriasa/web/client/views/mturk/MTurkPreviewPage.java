package com.schiller.veriasa.web.client.views.mturk;


import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;

public strictfp class MTurkPreviewPage extends Composite {

	private static MTurkPreviewPageUiBinder uiBinder = GWT
			.create(MTurkPreviewPageUiBinder.class);

	interface MTurkPreviewPageUiBinder extends UiBinder<Widget, MTurkPreviewPage> {
	}

	public interface ContinueCallback{
		void onContinue();
	}
	
	@UiField
	Button continueBtn;
	
	@UiField 
	Button previewBtn;
	
	@UiField
	HTML atmRate;
	
	@UiField
	HTMLPanel welcome;
	
	@UiField
	HTMLPanel instr;
	
	public MTurkPreviewPage(double rate, final ContinueCallback callback) {
		initWidget(uiBinder.createAndBindUi(this));
	
		String msg = "<span class='preview-main'> If you accept this HIT, you will earn " +
			"<span class='preview-rate'>$0." + ((int)(rate * 100)) + "</span>&nbsp;" +
			"for every question you answer.</span>";
		
		atmRate.setHTML(msg);
		
		continueBtn.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				welcome.setVisible(false);
				instr.setVisible(true);
			}
		});
		previewBtn.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				callback.onContinue();
			}
		});
		
	}

}
