package com.schiller.veriasa.web.client.views;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.allen_sauer.gwt.dnd.client.DragContext;
import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.allen_sauer.gwt.dnd.client.VetoDragException;
import com.allen_sauer.gwt.dnd.client.drop.SimpleDropController;
import com.allen_sauer.gwt.dnd.client.drop.VerticalPanelDropController;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratedTabPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.schiller.veriasa.web.client.VeriService;
import com.schiller.veriasa.web.client.VeriServiceAsync;
import com.schiller.veriasa.web.client.dnd.Fragment;
import com.schiller.veriasa.web.client.dnd.FragmentDefinitions;
import com.schiller.veriasa.web.client.dnd.PalettePanel;
import com.schiller.veriasa.web.client.dnd.FragmentDefinitions.FreeEdit;
import com.schiller.veriasa.web.client.dnd.FragmentDefinitions.Opt;
import com.schiller.veriasa.web.shared.dnd.InvArg;
import com.schiller.veriasa.web.shared.dnd.InvElement;
import com.schiller.veriasa.web.shared.dnd.InvFixed;
import com.schiller.veriasa.web.shared.dnd.InvElement.RefType;


public class DndWrite extends Composite implements RequiresResize {

	public static enum ContractMode { PRE, POST };
	
	private ContractMode mode = ContractMode.POST;
	
	private final VeriServiceAsync veriService = GWT
		.create(VeriService.class);

	private final AbsolutePanel main = new AbsolutePanel();
	private final DecoratedTabPanel specZoo = new DecoratedTabPanel();
	
	private final static int DEFAULT_SPEC_WIDTH = 360;
	private final static int DEFAULT_COL_WIDTH = 375;
	
	private final SimplePanel trash = new SimplePanel();
	private final SimplePanel cancel = new SimplePanel();
	private final SimplePanel solution = new SimplePanel();
	private final ScratchPanel scratch = new ScratchPanel();
	private final HorizontalPanel submitStrip = new HorizontalPanel();
	private final HorizontalPanel dropActions = new HorizontalPanel();
	private WriteHooks hooks;
	
	private WidthProvider widthProvider;
	
	private final ScrollPanel solScroll = new ScrollPanel(solution);
	private final ScrollPanel scratchScroll = new ScrollPanel(scratch);
	
	private final DndController dragController  = new DndController(main,false);
	
	private final List<String> params = new ArrayList<String>();
	private final List<InvElement> locals = new ArrayList<InvElement>();	
	
	private final TrashController trashController = new TrashController(trash);
	private final UnoDropController solutionController = new UnoDropController(solution);
	private final ScratchDropController scratchController = new ScratchDropController(scratch);
	
	public interface WriteHooks{
		void onSubmit(String invariant);
	}

	@Override
	public void onResize() {
		if (widthProvider != null){
			int w = widthProvider.getWidth() - 20;
			main.setWidth((w + 5) + "px");
			solScroll.setWidth(w + "px");
			scratchScroll.setWidth(w + "px");
			specZoo.setWidth(w + "px");
			//System.out.println("dnd set:" + w);
		}
		resizeSolution();
		resizeScratch();
	}
	
	
	
	public void addToScratch(final String spec){
		veriService.stringToElt(spec, new AsyncCallback<InvElement>(){

			@Override
			public void onFailure(Throwable caught) {
				Window.alert("Error adding spec to scratch pad:" + spec);
			}

			@Override
			public void onSuccess(final InvElement result) {				
				FocusPanel fp = new FocusPanel();
				Fragment f = new Fragment(null,result,dragController, false, changeCallback);
				fp.add(f);
				scratch.add(fp);
				f.resize();
				dragController.makeDraggable(fp);
				f.setDropTarget(true);
			}

		});	
	}
	
	private void setupSolutionArea(){
		solScroll.setWidth(DEFAULT_COL_WIDTH + "px");
		solScroll.setHeight("45px");
		solScroll.setStylePrimaryName("solution");
		
		solution.setWidth(DEFAULT_SPEC_WIDTH + "px");
		solution.setHeight("30px");
			
	}
	private void setupScratchArea(){
		scratchScroll.setWidth(DEFAULT_COL_WIDTH + "px");
		scratchScroll.setHeight("130px");
		scratchScroll.setStylePrimaryName("inv-flow");
		
		scratch.setWidth(DEFAULT_SPEC_WIDTH + "px");
		scratch.setHeight("115px");
		
		scratch.setSpacing(2);
	}
	private void setupDndStrip(){
		trash.add(createImgPanel("Delete Fragment", "img/trash.png"));
		cancel.add(createImgPanel("Cancel Move", "img/cancel.png"));
		
		Label txt = new Label("Move Actions:");
		dropActions.add(txt);
		dropActions.setCellVerticalAlignment(txt, HasVerticalAlignment.ALIGN_MIDDLE);
		SimplePanel vvv = new SimplePanel();
		vvv.setWidth("10px");
		dropActions.add(vvv);
		
		dropActions.add(trash);
		SimplePanel qqq = new SimplePanel();
		qqq.setWidth("10px");
		dropActions.add(qqq);
		dropActions.add(cancel);
		dropActions.setVisible(false);
	}
	private void setupSubmitStrip(){
		submitStrip.setWidth("100%");
		HTML h = new HTML();
		submitStrip.add(h);
		submitStrip.setCellWidth(h, "100%");
		Button submit = new Button("Submit");
		submitStrip.add(submit);
		
		submit.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				if (hooks != null){
					FocusPanel fp =  (FocusPanel) solution.getWidget();
					Fragment f = (Fragment) fp.getWidget();
					f.setDropTarget(false);
						
					hooks.onSubmit(getInvariant());
					
					solution.remove(fp);
					
					FocusPanel nn = new FocusPanel();
					Fragment ff = f.cloneWidget(false);
					nn.add(ff);
					scratch.add(fp);
					
					dragController.makeDraggable(nn);
					f.setDropTarget(true);
					
					updateSubmitStrip();
				}
			}
		});
	}
	public void setWidthProvider(WidthProvider widthProvider){
		this.widthProvider = widthProvider;
	}
	
	public DndWrite(){
		initWidget(main);
		
		
		
		dragController.registerDropController(scratchController);
		dragController.registerDropController(solutionController);
		dragController.registerDropController(trashController);
				
		specZoo.setWidth(DEFAULT_COL_WIDTH + "px");
		
		setupDndStrip();
		setupSolutionArea();
		setupScratchArea();
		setupSubmitStrip();
		
		main.add(new Label("Condition (drag completed conditions here):"));
		main.add(solScroll);
		main.add(submitStrip);
		main.add(new Label("Scratch Pad (holds unlimited fragments):"));
		main.add(scratchScroll);
		main.add(dropActions);
		
		SimplePanel iii = new SimplePanel();
		iii.setHeight("7px");
		main.add(iii);
	
		main.add(specZoo);
		
		submitStrip.setVisible(false);
		
		
		veriService.requestLocals(new AsyncCallback<List<InvElement>>(){
			@Override
			public void onFailure(Throwable caught) {
				locals.clear();
				updateVarCage();
			}
			@Override
			public void onSuccess(List<InvElement> result) {
				locals.clear();
				locals.addAll(result);
				updateVarCage();
			}
		});
		
		veriService.requestParams(new AsyncCallback<List<String>>(){
			@Override
			public void onFailure(Throwable caught) {
				params.clear();
				updateVarCage();
			}
			@Override
			public void onSuccess(List<String> result) {
				params.clear();
				params.addAll(result);
				updateVarCage();
			}
		});
	}
	
	
	public void setWriteHooks(WriteHooks hooks){
		this.hooks = hooks;
	}
	
	public void resizeSolution(){
		int pw = solScroll.getOffsetWidth();
		
		if (solution.getWidget() != null){
			int w = Math.max(((FocusPanel)solution.getWidget()).getWidget().getOffsetWidth(), pw - 15);
			solution.setWidth(w + "px");
		}else{
			
			solution.setWidth(Math.max(pw-15,DEFAULT_SPEC_WIDTH) + "px");
		}
	}
	
	public void resizeScratch(){
		int pw = scratchScroll.getOffsetWidth();
		
		if (scratch.getWidgetCount() == 0){
			scratch.setWidth(Math.max(pw-15,DEFAULT_SPEC_WIDTH) + "px");
		}else{
			int lrg = -1;
			
			for (int i = 0; i < scratch.getWidgetCount(); i++){
				Widget chld = scratch.getWidget(i);
				
				if (chld instanceof FocusPanel){
					if (((FocusPanel) chld).getWidget().getOffsetWidth() > lrg){
						lrg = scratch.getWidget(i).getOffsetWidth();
					}
				}
				
				
			}
			
			scratch.setWidth(Math.max(lrg, pw-15) + "px");
			
		}
		
	}
	
	private static class ScratchPanel extends VerticalPanel{
		public ScratchPanel() {
			Label spacerLabel = new Label("");
			super.add(spacerLabel);
		}
		@Override
		public void add(Widget w) {
			super.insert(w, getWidgetCount() - 1);
		}

		@Override
		public void insert(Widget w, int beforeIndex) {
			if (beforeIndex == getWidgetCount()) {
				beforeIndex--;
			}
			super.insert(w, beforeIndex);
		}
	}
	
	private final Fragment.ChangeCallback changeCallback = new Fragment.ChangeCallback() {
		@Override
		public void onChange() {
			updateSubmitStrip();
			resizeSolution();
		}
	};
	
	public class UnoDropController extends SimpleDropController {

		private final SimplePanel dropTarget;

		public UnoDropController(SimplePanel dropTarget) {
			super(dropTarget);
			this.dropTarget = dropTarget;
		}

		@Override
		public void onDrop(DragContext context) {
			dropTarget.setWidget(context.draggable);

			FocusPanel f = ((FocusPanel) context.draggable);
			final Fragment t = (Fragment) f.getWidget();
			
			if (t.getFather() != null){
				t.getFather().makeHole(t.getModel());
			}
			t.getOldest().resize();
			super.onDrop(context);
			t.setDropTarget(true);
			
			//updateSubmitStrip();
			resizeSolution();
			
		}

		@Override
		public void onPreviewDrop(DragContext context) throws VetoDragException {
			if (dropTarget.getWidget() != null) {
				throw new VetoDragException();
			}
			super.onPreviewDrop(context);
		}
	}
	
	public class TrashController extends SimpleDropController {

		public TrashController(SimplePanel dropTarget) {
			super(dropTarget);
		}

		@Override
		public void onDrop(DragContext context) {
			FocusPanel f = ((FocusPanel) context.draggable);
			Fragment t = (Fragment) f.getWidget();
			
			t.setDropTarget(false);
			
			if (t.getFather() != null){
				t.getFather().makeHole(t.getModel());	
			}
			context.draggable.removeFromParent();
			resizeSolution();
			super.onDrop(context);
		
		}

		@Override
		public void onPreviewDrop(DragContext context) throws VetoDragException {
			super.onPreviewDrop(context);
		}
	}


	private  class ScratchDropController extends VerticalPanelDropController{
		public ScratchDropController(ScratchPanel dropTarget) {
			super(dropTarget);
		}

		/* (non-Javadoc)
		 * @see com.allen_sauer.gwt.dnd.client.drop.AbstractInsertPanelDropController#onDrop(com.allen_sauer.gwt.dnd.client.DragContext)
		 */
		@Override
		public void onDrop(DragContext context) {
			FocusPanel f = ((FocusPanel) context.draggable);
			final Fragment t = (Fragment) f.getWidget();
			
			if (t.getFather() != null){
				t.getFather().makeHole(t.getModel());
			}
			t.getOldest().resize();
			t.setDropTarget(true);
			resizeSolution();
			
			super.onDrop(context);
		}
	}
	
	
	private class DndController extends PickupDragController{

		public DndController(AbsolutePanel boundaryPanel,
				boolean allowDroppingOnBoundaryPanel) {
			super(boundaryPanel, allowDroppingOnBoundaryPanel);
			// TODO Auto-generated constructor stub
		}
		/* (non-Javadoc)
		 * @see com.allen_sauer.gwt.dnd.client.PickupDragController#dragStart()
		 */
		@Override
		public void dragStart() {
			dropActions.setVisible(true);
			super.dragStart();
		}
		/* (non-Javadoc)
		 * @see com.allen_sauer.gwt.dnd.client.PickupDragController#dragEnd()
		 */
		@Override
		public void dragEnd() {
			super.dragEnd();
			dropActions.setVisible(false);
			updateSubmitStrip();
			resizeSolution();
		}
		
	}
	
	
	private PalettePanel varCagePanel = new PalettePanel(dragController);
	private PalettePanel otherCagePanel = new PalettePanel(dragController);
	
	public interface ChangeListener{
		void onChange(String value, boolean hasHoles);
		
	}
	private final List<ChangeListener> listeners = new LinkedList<ChangeListener>();
	
	
	public void addChangeListener(DndWrite.ChangeListener changeListener){
		listeners.add(changeListener);
	}
	
	private void addCage(String caption, Opt[] opts){
		PalettePanel x = new PalettePanel(dragController);
		for (Opt o : opts){
			x.add(new Fragment(null,o.getElt(),dragController, false, changeCallback));
		}
		specZoo.add(x, caption);	
	}
	
	private void addVarCage(){
		updateVarCage();
		specZoo.add(varCagePanel,"Variables");
	}
	
	
	private void addOtherCage(){
		updateOtherCage();
		specZoo.add(otherCagePanel,"Other");
	}
	
	private List<String> others = new LinkedList<String>();
	
	private void updateOtherCage(){
		otherCagePanel.clear();
		otherCagePanel.removeAllRows();
		
		for (Opt o : mode.equals(ContractMode.PRE) ? FragmentDefinitions.SPECIAL_PRE : FragmentDefinitions.SPECIAL_POST){
			otherCagePanel.add(new Fragment(null,o.getElt(),dragController, false, changeCallback));
		}
		
		for (String f : others){
			otherCagePanel.add(new Fragment(null,new InvFixed(f, RefType.Expression),dragController, false, changeCallback));
		}
		
		Button b = new Button("Create Fragment");
		
		b.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				FragmentDefinitions.showFree(new FreeEdit(){

					@Override
					public void onSave(String value) {
						if (value != null && !value.trim().equals("")){
							others.add(value);
							updateOtherCage();
						
						}
						
					}
					
				}, null);
				
			}
		});
		
		otherCagePanel.add(b);
	}
	
	private void updateVarCage(){
		varCagePanel.clear();
		varCagePanel.removeAllRows();
		
		if (params.size() + locals.size() > 0){
			for (String l : params){
				varCagePanel.add(new Fragment(null,new InvArg(l, RefType.Expression),dragController, false,changeCallback));
			}
			for (InvElement l : locals){
				varCagePanel.add(new Fragment(null,l.duplicate(),dragController,false,changeCallback));
			}
		}else{
			varCagePanel.setWidget(0, 0, new Label("There are no method parameters or class fields"));
			
		}
		
		
	}

	public void buildSpecZoo(ContractMode mode){
		this.mode = mode;
		buildSpecZoo();
		specZoo.selectTab(0);
	}
	
	private void buildSpecZoo(){
		addCage("Logic", FragmentDefinitions.LOGIC);
		addCage("Comparison", FragmentDefinitions.COMPARE);
		addVarCage();
		addCage("Math", FragmentDefinitions.MATH);
		addOtherCage();
		//addCage("Other", mode.equals(Mode.PRE) ? EzDefs.SPECIAL_PRE : EzDefs.SPECIAL_POST);
	}
	
	private static HorizontalPanel createImgPanel(String caption, String imgPath){
		HorizontalPanel p = new HorizontalPanel();
		p.add(new Image(imgPath));
		Label txt = new Label(caption);
		p.add(txt);
		p.setCellVerticalAlignment(txt, HasVerticalAlignment.ALIGN_MIDDLE);
		return p;
	}
	
	private void updateSubmitStrip(){
		submitStrip.setVisible(canSubmit());
	}
	
	private boolean canSubmit(){
		if (solution.getWidget() == null){
			return false;
		}
		FocusPanel f = (FocusPanel) solution.getWidget();
		Fragment x = (Fragment) f.getWidget();
		return (!x.getModel().hasHole());
	}
	
	private String getInvariant(){
		if (!canSubmit()){
			throw new RuntimeException("Incomplete Invariant");
		}
		FocusPanel f = (FocusPanel) solution.getWidget();
		Fragment x = (Fragment) f.getWidget();
		return x.getModel().getValue();
	}

}
