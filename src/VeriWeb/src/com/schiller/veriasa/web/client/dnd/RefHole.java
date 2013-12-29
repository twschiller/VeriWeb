package com.schiller.veriasa.web.client.dnd;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.schiller.veriasa.web.shared.dnd.InvRef;

public class RefHole extends HTML implements IsWidget{

	private Fragment parent;
	private InvRef me;
	
	public RefHole(Fragment parent, InvRef ref, boolean dropTarget){
		super("&nbsp;&nbsp;&nbsp;");
		this.parent = parent;
		this.me = ref;
		this.setStylePrimaryName(dropTarget ? "target" : "hole" );
	}
	
	public void fillHole(Fragment value){
		parent.fillHole(me, value);
	}
	
	public boolean hasTooltip(){
		return this.me.hasTooltip();
	}
	
	public String getTooltip(){
		return me.getTooltip();
	}
	
	public boolean isCompatible(Fragment f){
		return me.getRefType().equals(f.getModel().getRefType());
	}
	public Fragment getParentFragment(){
		return parent;
	}
	
}
