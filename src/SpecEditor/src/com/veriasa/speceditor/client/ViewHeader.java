package com.veriasa.speceditor.client;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;

public class ViewHeader extends Composite{

	private final HorizontalPanel hp = new HorizontalPanel();
	
	private final Label funName = new Label("int Foo(string x, object y)");
	
	public void setFunName(String name){
		funName.setText(name);
	}
	
	public ViewHeader(){
		initWidget(hp);
		
		hp.add(funName);
		
		funName.setStyleName("fun-name");
		
	}
	
}
