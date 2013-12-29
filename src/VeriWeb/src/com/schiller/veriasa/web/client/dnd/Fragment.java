package com.schiller.veriasa.web.client.dnd;

import java.util.ArrayList;

import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.google.gwt.event.dom.client.HasMouseOutHandlers;
import com.google.gwt.event.dom.client.HasMouseOverHandlers;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.schiller.veriasa.web.shared.dnd.InvArg;
import com.schiller.veriasa.web.shared.dnd.InvElement;
import com.schiller.veriasa.web.shared.dnd.InvFixed;
import com.schiller.veriasa.web.shared.dnd.InvFree;
import com.schiller.veriasa.web.shared.dnd.InvLocal;
import com.schiller.veriasa.web.shared.dnd.InvRef;
import com.schiller.veriasa.web.shared.dnd.InvElement.RefType;

public class Fragment extends Composite implements IsWidget{
	private InvElement model;
	
	private final FragmentPanel panel; 
	private final Fragment father;
	
	private final ArrayList<IsWidget> parts = new ArrayList<IsWidget>();
		
	private final PickupDragController dragController;
	private final ArrayList<FillHoleDropController> dropControllers = new ArrayList<FillHoleDropController>();
	private boolean dropTarget = false;
	
	public interface ChangeCallback{
		void onChange();
	}
		
	private final ChangeCallback callback;
	
	public static SimplePanel invisiPanel = null;
	
	@Override
	public String toString(){
		return model.getValue();
	}
	
	/**
	 * 
	 * @param father parent fragment, or null if orphan
	 * @param model the element displayed by the fragment
	 * @param dragController drag and drop controller for managing holes
	 * @param dropTarget true iff the holes in the fragment are drop targets
	 */
	public Fragment(Fragment father, InvElement model, PickupDragController dragController, boolean dropTarget, ChangeCallback callback){
		panel = new FragmentPanel(this,dragController);
		initWidget(panel);
			
		this.callback = callback;
		this.father = father;
		this.model = model;
		this.dropTarget = dropTarget;
		this.dragController = dragController;
		render();
	
		
	}
	
	public Fragment getOldest(){
		if (this.father == null){
			return this;
		}else{
			return this.father.getOldest();
		}
	}
	
	public InvElement getModel(){
		return model;
	}
	
	/**
	 * get the fragment containing this fragment, or null
	 * if the fragment is an orphan
	 * @return
	 */
	public Fragment getFather(){
		return father;
	}

	/**
	 * create a cloned fragment with a deep copy of the model of the fragment
	 * @param dropTarget true iff the holes of the clone should be drop targets
	 * @return a cloned fragment with a deep copy of the model of the fragment
	 */
	public Fragment cloneWidget(boolean dropTarget){
		Fragment f = new Fragment(father, model.duplicate(), dragController,dropTarget,callback);
		return f;
	}
	
	private void addDropController(Widget w){
		FillHoleDropController c = new FillHoleDropController((Widget) w);
		dropControllers.add(c);
		dragController.registerDropController(c);	
	}
	
	/**
	 * Sets whether or not the holes in the fragment are drop targets,
	 * re-rendering if necessary
	 * @param dropTarget
	 */
	public void setDropTarget(boolean dropTarget){
		if (this.dropTarget != dropTarget){
			this.dropTarget = dropTarget;
			if (dropTarget){
				for (IsWidget w : parts){
					if (w instanceof RefHole){
						addDropController((Widget) w);
					}else if (w instanceof Fragment){
						((Fragment) w).setDropTarget(true);
					}
				}
				
			}else{
				unregAll();
			}
		}
	}
	
	public void removeHighlight(){
		panel.removeStyleName("inv-highlight");
		
		for (IsWidget w : parts){
			if (w instanceof Fragment){
				((Fragment) w).removeHighlight();
			}else if(w instanceof InvElement || w instanceof RefHole){
				w.asWidget().removeStyleName("inv-highlight");
			}
		}
	}
	
	public void performHighlight(){
		panel.addStyleName("inv-highlight");
		
		for (IsWidget w : parts){
			if (w instanceof Fragment){
				((Fragment) w).performHighlight();
			}else if(w instanceof InvElement || w instanceof RefHole){
				w.asWidget().addStyleName("inv-highlight");
			}
		}
	}
	
	/**
	 * Remove the specified sub-element from the fragment 
	 * and re-render
	 * @param toRemove the sub-element to remove
	 */
	public void makeHole(InvElement toRemove){
		unregAll();
		removeHighlight();
		for (InvRef r : model.getSubElements()){
			if (r.getValue() == toRemove){
				r.setValue(null, true);
			}
		}
		render();
		getOldest().resize();
		
	}
	
	public void fillHole(InvRef hole, Fragment value){
		hole.setValue(value.model, true);
		render();
		getOldest().resize();
	}
	

	
	/**
	 *  Unregisters all drop targets in the fragment (recursively
	 *  unregistering drop targets for sub fragments)
	 */
	private void unregAll(){
		for (FillHoleDropController c : dropControllers){
			dragController.unregisterDropController(c);
		}
		dropControllers.clear();
			
		for (IsWidget q : parts){
			if (q instanceof Fragment){
				((Fragment) q).unregAll();
			}
		}
	}
	
	private static int findWidth(Widget w){
		if (invisiPanel.isAttached()){
			
			invisiPanel.setWidget(w);
			int width = w.getOffsetWidth();
			
			return width;
		}else{
			return -1;
		}

	}
	
	private void attachHighlight(FocusPanel fp){
		fp.addMouseOverHandler(new MouseOverHandler(){
			@Override
			public void onMouseOver(MouseOverEvent event) {
				performHighlight();
			}
		});
		fp.addMouseOutHandler(new MouseOutHandler(){
			@Override
			public void onMouseOut(MouseOutEvent event) {
				removeHighlight();
			}
		});
	}
	
	private void addToPanel(Widget w, boolean whole, boolean triggersHighlight){
		if (triggersHighlight){
			FocusPanel fp;
			if (w instanceof FocusPanel){
				fp = (FocusPanel) w;
			}else{
				fp = new FocusPanel();
				fp.add(w);
			}
			attachHighlight(fp);
			panel.add(fp);
		}else{
			panel.add(w);	
		}
	}
	private void addToPanel(Widget w, int width,boolean triggersHighlight){
		if (triggersHighlight){
			FocusPanel fp;
			if (w instanceof FocusPanel){
				fp = (FocusPanel) w;
			}else{
				fp = new FocusPanel();
				fp.add(w);
			}
			attachHighlight(fp);
			panel.add(fp);
			panel.setCellWidth(fp, width + "px");
		}else{
			panel.add(w);
			panel.setCellWidth(w, width + "px");
		}
	}
	
	public void resize(){
		if (!invisiPanel.isAttached()){
			return;
		}
		
		this.setWidth("auto");
		
		Label qq = null;
		if (model instanceof InvFixed){
			qq = new Label(((InvFixed) model).getValue().trim());
			qq.setStylePrimaryName("inv-fixed");
		}else if (model instanceof InvArg){
			qq = new Label(((InvArg) model).getValue().trim());
			qq.setStylePrimaryName("inv-arg");
		}else if (model instanceof InvLocal){
			qq = new Label(((InvLocal) model).getValue().trim());
			qq.setStylePrimaryName("inv-local");
		}else if (model instanceof InvFixed){
			qq = new Label(((InvFixed) model).getValue().trim());
		}
	
		if (qq != null){
			int width = findWidth(qq);
			panel.setCellWidth(panel.getWidget(0), width + "px");
			panel.setWidth(width + "px");
		}else{
			/*int sum = 0;
			for (int i=0; i < panel.getWidgetCount(); ++i){
				Widget w  = panel.getWidget(i);
				if (w instanceof RefHole){
					panel.setCellWidth(w, "20px");
					sum += 20;
				}else if (w instanceof FocusPanel){
					Fragment f = (Fragment) ((FocusPanel) w).getWidget();
					f.resize();
					panel.setCellWidth(w, f.desiredWidth + "px");
					sum += f.desiredWidth;
				}
			}*/
			int width = findWidth(this.cloneWidget(false));
			
			panel.setWidth(width + "px");
		}
	}

	private static void addTooltip(Widget w, final String tooltip){
		if (w instanceof HasMouseOverHandlers && w instanceof HasMouseOutHandlers){
			final PopupPanel p = new PopupPanel();
			
			((HasMouseOverHandlers) w).addMouseOverHandler(new MouseOverHandler(){
				@Override
				public void onMouseOver(MouseOverEvent event) {
					p.clear();
					p.add(new Label(tooltip));
					p.setPopupPosition(event.getClientX() + 5, event.getClientY() + 10);
					p.show();
				}
			});
			
			((HasMouseOutHandlers) w).addMouseOutHandler(new MouseOutHandler(){
				@Override
				public void onMouseOut(MouseOutEvent event) {
					p.hide();
				}
			});
			
			
		}else{
			throw new IllegalArgumentException("widget cannot handle mouse events");
		}
	}
	
	
	private void render(){	
		unregAll();
		panel.clear();
		parts.clear();
		this.panel.setStylePrimaryName("outlined");
		
		if (model instanceof InvFixed){
			Label qq = new Label(((InvFixed) model).getValue().trim());
			qq.setStylePrimaryName("inv-fixed");
			addToPanel(qq,true,true);
		}else if (model instanceof InvArg){
			Label qq = new Label(((InvArg) model).getValue().trim());
			qq.setStylePrimaryName("inv-arg");
			addToPanel(qq,true,true);
		}else if (model instanceof InvLocal){
			Label qq = new Label(((InvLocal) model).getValue().trim());
			qq.setStylePrimaryName("inv-local");
			addToPanel(qq,true,true);
		}else if (model instanceof InvFixed){
			Label l = new Label(((InvFixed) model).getValue().trim());
			addToPanel(l,true,true);
		}else if (model instanceof InvFree){
			throw new RuntimeException("InvFree not supported");
		}else{
			for (final InvRef sub : model.getSubElements()){
				if (sub.isHole()){
					RefHole h = new RefHole(this,sub, dropTarget);
					
					if (sub.hasTooltip()){
						addTooltip(h, sub.getTooltip());
					}
					
					addToPanel(h,20,true);
					if (dropTarget){
						addDropController(h);
					}
				
					parts.add(h);
				}else{
					final Fragment f = new Fragment(this, sub.getValue(),dragController,false,callback);
					if (sub.getValue().getRefType().equals(RefType.BoilerPlate)){
						parts.add(f);
					
						addToPanel(f,false,true);
						
					}else{
						final FocusPanel focus = new FocusPanel();
						focus.setStylePrimaryName("outlined");
						focus.add(f);
						parts.add(f);
						
						addToPanel(focus,false,false);
						dragController.makeDraggable(focus);
					}
				
					if (dropTarget){
						f.setDropTarget(dropTarget);	
					}
				}
			}
		}
	}
}
