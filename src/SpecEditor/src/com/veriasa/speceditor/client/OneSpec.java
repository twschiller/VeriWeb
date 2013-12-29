package com.veriasa.speceditor.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.veriasa.speceditor.shared.FunctionDoc;
import com.veriasa.speceditor.shared.SpecProblem;

public class OneSpec extends Composite {

	private static OneSpecUiBinder uiBinder = GWT.create(OneSpecUiBinder.class);

	private static GreetingServiceAsync service = (GreetingServiceAsync) GWT.create(GreetingService.class);
	
	interface OneSpecUiBinder extends UiBinder<Widget, OneSpec> {
	}
	
	@UiField
	CodeView fun;
	
	@UiField
	SpecSelector pre;
	
	@UiField
	SpecSelector post;
	
	@UiField
	FunctionDocView doc;
	
	@UiField
	ViewHeader head;
	
	@UiField
	FunctionDocView activeDoc;
	
	public void requestProblem(){
		service.requestProblem(new AsyncCallback<SpecProblem>(){

			@Override
			public void onFailure(Throwable caught) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onSuccess(SpecProblem result) {
				fun.SetCode(result.getInfo().getBody(),true);
				pre.setValues(result.getInferredPres());
				post.setValues(result.getInferredPosts());
				doc.setDoc(result.getInfo().getDoc());
				
				head.setFunName(result.getInfo().getSig().getText());
			}			
		});		
	}
	
	public OneSpec() {
		initWidget(uiBinder.createAndBindUi(this));
		requestProblem();
		exportChangeDoc();
	}
	
	public void changeDoc(int docId){
		service.requestDoc(docId, new AsyncCallback<FunctionDoc>(){

			@Override
			public void onFailure(Throwable caught) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onSuccess(FunctionDoc result) {
				activeDoc.setDoc(result);
			}
			
		});
	}

	public native void exportChangeDoc() /*-{
		var _this = this;
		$wnd.changeDoc = function(docId){
			_this.@com.veriasa.speceditor.client.OneSpec::changeDoc(I)(docId);
		};
	}-*/;
}
