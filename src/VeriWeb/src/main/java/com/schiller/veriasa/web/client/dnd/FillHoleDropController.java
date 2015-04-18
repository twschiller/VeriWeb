package com.schiller.veriasa.web.client.dnd;

import com.allen_sauer.gwt.dnd.client.DragContext;
import com.allen_sauer.gwt.dnd.client.VetoDragException;
import com.allen_sauer.gwt.dnd.client.drop.SimpleDropController;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Drop controller for fragment holes
 * @author Todd Schiller
 */
public class FillHoleDropController extends SimpleDropController{

	private final RefHole dropTarget;

	public FillHoleDropController(Widget dropTarget) {
		super(dropTarget);
		if (dropTarget instanceof RefHole){
			this.dropTarget = (RefHole) dropTarget;
		}else{
			throw new IllegalArgumentException();
		}
	}

	@Override
	public void onDrop(DragContext context) {
		//(1) get the fragment that is being dragged
		FocusPanel f = (FocusPanel) context.draggable;
		Fragment dragged = (Fragment) f.getWidget();
		
		//(2) remove all drop targets (they will be re-added by the new parent)
		dragged.setDropTarget(false);
	
		if (dragged.getFather() != null){
			dragged.getFather().makeHole(dragged.getModel());
		}
		
		//(3) fill the hole in the model
		dropTarget.fillHole(dragged);
		
		//(4) let the dnd library do its thing
		super.onDrop(context);
	}
	
	@Override
	public void onPreviewDrop(DragContext context) throws VetoDragException {
		//(1) get the fragment that is being dragged
		FocusPanel f = (FocusPanel) context.draggable;
		Fragment dragged = (Fragment) f.getWidget();
		
		if (! dropTarget.isCompatible(dragged)){
			throw new VetoDragException();
		}
			
		super.onPreviewDrop(context);
	}	
}
