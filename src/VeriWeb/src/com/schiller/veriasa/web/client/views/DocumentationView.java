package com.schiller.veriasa.web.client.views;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.schiller.veriasa.web.shared.core.MethodDocumentation;
import com.schiller.veriasa.web.shared.core.MethodDocumentation.ParameterDocumentation;

public class DocumentationView extends Composite {
	
	private static DocumentationViewUiBinder uiBinder = GWT
			.create(DocumentationViewUiBinder.class);

	interface DocumentationViewUiBinder extends UiBinder<Widget, DocumentationView> {
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

	public DocumentationView() {
		initWidget(uiBinder.createAndBindUi(this));
		paramsHeader.setVisible(false);
		returnsHeader.setVisible(false);
	}
	
	public void setDoc(MethodDocumentation doc){
		summary.setText(doc.getSummary());
		
		if (doc.getReturns() != null ){
			returnsHeader.setVisible(true);
			returnDoc.setVisible(true);
			returnDoc.setText(doc.getReturns());
		}else{
			returnsHeader.setVisible(false);
			returnDoc.setVisible(false);
		}
	
		if (doc.getParams() != null && doc.getParams().size() > 0){
			paramsHeader.setVisible(true);
			params.clear();
			
			for (ParameterDocumentation param : doc.getParams()){
				
				String ss = "<span class=\"param-name\">" + param.getName() + ":</span>" +
					"<span class=\"param-doc\">" + param.getDoc() + "</span>";
				
				params.add(new HTML(ss));
			}
			
		}else{
			paramsHeader.setVisible(false);
			
		}
	}
}
