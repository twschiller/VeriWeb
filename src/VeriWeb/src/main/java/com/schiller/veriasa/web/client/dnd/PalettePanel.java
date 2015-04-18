package com.schiller.veriasa.web.client.dnd;

import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * @author Todd Schiller
 */
public class PalettePanel extends FlexTable {
	private final PickupDragController dragController;

	public PalettePanel(PickupDragController dragController) {
		this.dragController = dragController;
	}

	private static class PaletteElement extends SimplePanel{

		private final PickupDragController dragController;

		public PaletteElement(PickupDragController dragController){
			this.dragController = dragController;
		}

		@Override
		public void setWidget(Widget w) {
			if (this.getWidget() == null){
				super.setWidget(w);
			}else{

			}
		}
	
		@Override
		public void setWidget(IsWidget w) {
			if (this.getWidget() == null){
				super.setWidget(w);
			}else{

			}
		}
		
		@Override
		public boolean remove(Widget w) {
			if (w instanceof FocusPanel){
				Fragment clone = ((Fragment) ((FocusPanel) w).getWidget()).cloneWidget(false);
				FocusPanel n = new FocusPanel();
				n.add(clone);
				dragController.makeDraggable(n);
				super.remove(w);
				super.setWidget(n);
				return true;
			}	
			return true;
		}
	}

	@Override
	public void add(Widget child) {
		if (super.getRowCount() == 0 || super.getCellCount(super.getRowCount()-1) >= 3){
			super.insertRow(super.getRowCount());
		}
		super.addCell(super.getRowCount()-1);
		super.setWidget(super.getRowCount()-1, super.getCellCount(super.getRowCount()-1), child);
	}

	public void add(Fragment w) {		
		FocusPanel p = new FocusPanel();
		p.add(w);
		dragController.makeDraggable(p);

		if (super.getRowCount() == 0 || super.getCellCount(super.getRowCount()-1) >= 3){
			super.insertRow(super.getRowCount());
		}

		PaletteElement q = new PaletteElement(dragController);

		super.addCell(super.getRowCount()-1);
		super.setWidget(super.getRowCount()-1, super.getCellCount(super.getRowCount()-1), q);
		q.add(p);
	}
}
