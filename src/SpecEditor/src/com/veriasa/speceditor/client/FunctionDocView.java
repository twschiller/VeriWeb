package com.veriasa.speceditor.client;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.veriasa.speceditor.shared.DocId;
import com.veriasa.speceditor.shared.FunctionDoc;
import com.veriasa.speceditor.shared.ParamDoc;


public class FunctionDocView extends Composite {
	private static GreetingServiceAsync service = (GreetingServiceAsync) GWT.create(GreetingService.class);
	
	private static FunctionDocViewUiBinder uiBinder = GWT
			.create(FunctionDocViewUiBinder.class);

	interface FunctionDocViewUiBinder extends UiBinder<Widget, FunctionDocView> {
	}

	@UiField
	Label summary;

	@UiField
	Label paramsHeader;

	@UiField
	Label returnsHeader;

	@UiField
	Label returnDoc;

	@UiField
	VerticalPanel params;

	public FunctionDocView() {
		initWidget(uiBinder.createAndBindUi(this));
		paramsHeader.setVisible(false);
		returnsHeader.setVisible(false);
	}
	
	public void setDoc(FunctionDoc doc){
		summary.setText(doc.getSummary());
		
		if (doc.getReturnValue() != null ){
			returnsHeader.setVisible(true);
			returnDoc.setVisible(true);
			returnDoc.setText(doc.getReturnValue());
		}else{
			returnsHeader.setVisible(false);
			returnDoc.setVisible(false);
		}
	
		if (doc.getParams() != null && doc.getParams().size() > 0){
			paramsHeader.setVisible(true);
			params.clear();
			
			for (ParamDoc param : doc.getParams()){
				
				String ss = "<span class=\"param-name\">" + param.getName() + ":</span>" +
					"<span class=\"param-doc\">" + param.getDoc() + "</span>";
				
				params.add(new HTML(ss));
			}
			
		}else{
			paramsHeader.setVisible(false);
			
		}
	}
}
