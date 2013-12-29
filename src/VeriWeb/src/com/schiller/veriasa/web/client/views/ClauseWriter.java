package com.schiller.veriasa.web.client.views;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasMouseOutHandlers;
import com.google.gwt.event.dom.client.HasMouseOverHandlers;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.schiller.veriasa.web.client.ToolTips;
import com.schiller.veriasa.web.client.VeriService;
import com.schiller.veriasa.web.client.VeriServiceAsync;
import com.schiller.veriasa.web.client.dnd.Fragment;
import com.schiller.veriasa.web.client.views.DndWrite.WriteHooks;
import com.schiller.veriasa.web.shared.core.Clause;
import com.schiller.veriasa.web.shared.core.Clause.Status;
import com.schiller.veriasa.web.shared.feedback.DynamicFeedback;
import com.schiller.veriasa.web.shared.parsejml.JmlSpan;
import com.schiller.veriasa.web.shared.util.CallbackUtils;
import com.schiller.veriasa.web.shared.util.SpecUtil;

/**
 * The area of the UI for writing specifications
 * @author Todd Schiller
 */
public class ClauseWriter extends Composite implements RequiresResize {
		
	private static SpecWriterUiBinder uiBinder = GWT
			.create(SpecWriterUiBinder.class);

	private final VeriServiceAsync veriService = GWT
		.create(VeriService.class);
	
	interface SpecWriterUiBinder extends UiBinder<Widget, ClauseWriter> {
	}
	
	public interface ExistsCheck{
		public boolean alreadyExists(String statement);
	}
	
	public interface SpecDecisionHandler{
		public void onDecision(Clause statement);
	}
	
	public interface SpecEventHandler{
		public void added(String statement, SpecDecisionHandler handler);
		public void added(List<String> statements, SpecDecisionHandler handler);
		public void removed(String statement);
	}
	
	/**
	 * # of milliseconds to delay documentation switch after mousing over a contract
	 */
	private static final int DOC_DELAY_MILLIS = 1000;
	
	private final static int UP_COL = 0;
	private final static int INFO_COL = 1;
	private final static int REMOVE_COL = 2;
	private final static int INV_COL = 3;

	private final static int ICON_COL_WIDTH = 20;
	
	/**
	 * number of contracts to submit simultaneously when submitting
	 * contracts in groups
	 */
	private final static int PARALLEL_SUBMIT = 3;
	
	public enum WriteMode { DND, FREE };
	
	@UiField
	FlexTable specTable;
	
	@UiField
	VerticalPanel main;
	
	@UiField
	HTML boxExplain;

	@UiField 
	RadioButton dndBtn;

	@UiField 
	RadioButton typingBtn;

	/**
	 * The documentation view panel
	 * @deprecated remove dependence for improved modularity
	 */
	private InformationView documentation;
	
	/**
	 * manages contract highlighting in writer area
	 */
	private final Spanner highlightManager = new Spanner("reg-spec-def", "reg-spec-high", true);

	/**
	 * The visible (and hidden) clauses
	 */
	private final List<Clause> clauses = new ArrayList<Clause>();
	
	/**
	 * Text entry panel
	 */
	private final FreeWrite freeWriter = new FreeWrite();
	
	/**
	 * Drag and drop entry panel
	 */
	private final DndWrite dndWriter = new DndWrite();
	
	/**
	 * The active writer (text entry panel, or drag and drop entry panel);
	 * tracked via {@link ClauseWriter#writeMode}
	 */
	private final HorizontalPanel writer = new HorizontalPanel();
	
	/**
	 * Whether free text entry is enabled, or drag and drop entry is enabled;
	 * corresponds to {@link ClauseWriter#writer}
	 */
	private WriteMode writeMode = WriteMode.DND;
	
	/**
	 * Whether the drag and drop writer is in pre- or post- condition mode
	 */
	private DndWrite.ContractMode mode;
	
	/**
	 * displayed contract -> dynamic feedback for why the contract is invalid
	 */
	private final Map<Clause, DynamicFeedback> dynamicFeedback = new HashMap<Clause, DynamicFeedback>();
	
	/**
	 * cache for constructed dynamic feedback views
	 */
	private HashMap<DynamicFeedback, DynamicFeedbackPanel> dynamicFeedbackUxCache = new HashMap<DynamicFeedback, DynamicFeedbackPanel>();
	
	/**
	 * contract -> corresponding {@link JmlSpan}
	 */
	private final Map<String, JmlSpan> contractParseCache = new HashMap<String, JmlSpan>();

	/**
	 * set of post conditions that the user has chosen to hide
	 */
	private final HashSet<String> hiddenContracts = new HashSet<String>();
	
	/**
	 * invisible panel used to calculate true size of invariant fragments
	 */
	private final SimplePanel invisible = new SimplePanel();

	private SpecEventHandler listener = null;
	
	/**
	 * Object to poll for allowable width
	 */
	private WidthProvider widthProvider = null;
		
	private ExistsCheck existsCheck = new ExistsCheck(){
		@Override
		public boolean alreadyExists(String spec) {
			return false;
		}
	};
	
	/**
	 * Create an area for writing contracts, defaults to using a drag and drop
	 * interface
	 */
	public ClauseWriter() {
		initWidget(uiBinder.createAndBindUi(this));
		boxExplain.setVisible(false);
	
		dndBtn.setValue(true);
		
		typingBtn.addValueChangeHandler(new ValueChangeHandler<Boolean>(){
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				if (event.getValue()){
					setWriteMode(WriteMode.FREE);
				}
			}
		});
		dndBtn.addValueChangeHandler(new ValueChangeHandler<Boolean>(){
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				if (event.getValue()){
					setWriteMode(WriteMode.DND);
				}
			}
		});
		
		writer.add(dndWriter);
		
		main.insert(writer, 1);
		
		ScrollPanel invisiScroll = new ScrollPanel(invisible);
		main.add(invisiScroll);
		invisiScroll.setHeight("0px");
		invisiScroll.setWidth("30px");//must have width so Chrome doesn't assign negative width
		invisible.setWidth("2000px");
		Fragment.invisiPanel = invisible;
		invisible.setStylePrimaryName("offscreen");
		invisiScroll.setStylePrimaryName("offscreen-scroll");
		
		WriteHooks h = new WriteHooks(){
			@Override
			public void onSubmit(String invariant) {
				trySubmit(SpecUtil.clean(invariant));
			}
		};
		
		dndWriter.setWriteHooks(h);
		freeWriter.setWriteHooks(h);
		
		dndBtn.setValue(writeMode.equals(WriteMode.DND));
		typingBtn.setValue(writeMode.equals(WriteMode.FREE));
		
		registerMouseTracking();
	}
	
	/**
	 * Set the documentation view panel
	 * @param view the documentation view panel
	 * @deprecated dependency on {@link InformationView} will be removed
	 */
	public void setDocPanel(InformationView view){
		this.documentation = view;
	}

	public void setMode(DndWrite.ContractMode mode){
		dndWriter.buildSpecZoo(mode);
		this.mode = mode;
	}
	
	public void setWidthProvider(WidthProvider widthProvider){
		this.widthProvider = widthProvider;
		dndWriter.setWidthProvider(widthProvider);
		freeWriter.setWidthProvider(widthProvider);
	}
	
	private void setWriteMode(WriteMode writeMode){
		this.writeMode = writeMode;
		
		writer.clear();
		writer.add(writeMode == WriteMode.DND ? dndWriter : freeWriter);
		
		veriService.writeModeChanged(writeMode, CallbackUtils.<Boolean>ignore());
		
		if (writeMode == WriteMode.DND){
			dndWriter.onResize();
		}else{
			freeWriter.onResize();
		}
	}
	
	/**
	 * @deprecated I don't know what this function is supposed to do
	 */
	public void setCanWrite(boolean canWrite){
		// TODO why was this commented out?
		//submit.setEnabled(canWrite);
		//suggestionBox.setEnabled(canWrite);
	}
	

	public void setSpecEventHandler(SpecEventHandler handler){
		this.listener = handler;
	}
	
	public void setExistsCheck(ExistsCheck check){
		this.existsCheck = check;
	}
	
	/**
	 * Associates dynamic feedback with a displayed contract
	 * @param contract the contract to associate the feedback with
	 * @param f the dynamic trace feedback
	 */
	public void attachDynamicFeedback(Clause contract, DynamicFeedback f){
		dynamicFeedback.put(contract, f);
		
		// TODO updating the whole table again is inefficient
		updateTable();
	}
	
	@Override
	public void onResize() {
		if (widthProvider != null){
			int w = widthProvider.getWidth();
			main.setWidth(w + "px");
			specTable.setWidth((w - 20) + "px");
		}
	
		if (writeMode.equals(WriteMode.DND)){
			dndWriter.onResize();
		}else{
			freeWriter.onResize();
		}
	}
	
	/**
	 * Get the list of contracts
	 * @return the list of contracts
	 * @deprecated Leaks implementation
	 */
	public List<Clause> getSpecs(){
		return clauses;
	}

	/**
	 * Add a contract to the scratch pad
	 * @param contract the contract
	 */
	public void addToScratch(final String contract){
		dndWriter.addToScratch(contract);
	}
	
	/**
	 * Try a list of contracts, in order. If {@value parallel} is true,
	 * will partition the contracts into groups and try the groups
	 * @param specs the contracts to try
	 * @param parallel true iff it is OK to submit the contracts in parallel
	 */
	public void addSpecs(List<Clause> specs, boolean parallel){
		if (!parallel){
			addSpecs(specs);
			return;
		}
		
		// TODO rewrite using Lists.partition
		
		List<List<String>> xs = new ArrayList<List<String>>();
		List<String> x = new ArrayList<String>();
		
		for (Clause s : specs){
			x.add(s.getClause());
			if (x.size() >= PARALLEL_SUBMIT){
				xs.add(x);
				x = new ArrayList<String>();
			}
		}
		if (x.size() > 0){
			xs.add(x);
		}
		
		for (List<String> i : xs ){
			listener.added(i, new SpecDecisionHandler(){
				@Override
				public void onDecision(Clause spec) {
					updateSpec(spec);
				}
			});
		}
		
		this.clauses.addAll(specs);
		updateTable();
	}
	
	/**
	 * Add contracts to the interface, submitting a contract for validation if its
	 * status is pending
	 * @param specs the list of contracts to try
	 * @deprecated 
	 */
	public void addSpecs(List<Clause> specs){
		for (Clause s : specs){
			if (s.getStatus().equals(Status.PENDING)){
				if (listener != null){	
					listener.added(s.getClause(), new SpecDecisionHandler(){
						@Override
						public void onDecision(Clause spec) {
							updateSpec(spec);
						}
					});
				}
			}
		}
		
		this.clauses.addAll(specs);
		updateTable();
	}

	/**
	 * Get the CSS class corresponding to the given contract status
	 * @param s the status of the specification
	 * @return the CSS class corresponding to the given contract status
	 */
	private static String statusToClass(Status s){
		switch(s){
		case PENDING:
			return "user-spec-pending";
		case KNOWN_GOOD:
			return "user-spec-good";
		case KNOWN_BAD:
			return "user-spec-bad";
		case SYNTAX_BAD:
			return "user-spec-syntax-bad";
		default:
			throw new IllegalArgumentException("Unsupported status " + s.toString());
		}
	}
	
	/**
	 * Remove <code>contract</code> from {@link ClauseWriter#clauses}. If <code>contract</code>
	 * is a pre-condition, alerts {@link ClauseWriter#listener}. If <code>contract</code> is a
	 * post-condition, adds <code>contract</code> to the set of post-conditions.
	 * @param clause the contract to remove from the table
	 */
	private void removeClause(Clause clause){
		for (Clause visible : new ArrayList<Clause>(clauses)){
			if (SpecUtil.invEqual(visible, clause)){
				if ( clause.getStatus() == Status.SYNTAX_BAD){
					clauses.remove(visible);
				}else if (mode.equals(DndWrite.ContractMode.PRE)){
					clauses.remove(visible);
					listener.removed(visible.getClause());
				}else if (mode.equals(DndWrite.ContractMode.POST)){
					hiddenContracts.add(clause.getClause().trim());
				}
				updateTable();
				break;
			}
		}
	}
	
	/**
	 * Associate a piece of documentation with displayed widget; display
	 * the documentation with a delay of {@link DOC_DELAY_MILLIS} when the
	 * user mouses over the widget. If the user moves the mouse off of the widget,
	 * the request to display the documentation is cancelled.
	 * @param widget the widget to associate the documentation with
	 * @param info the information to display in the documentation panel
	 */
	public <T extends Widget & HasMouseOverHandlers & HasMouseOutHandlers> 
	void addDocChange(final T widget, final Widget info){

		final Timer timer = new Timer(){
			@Override
			public void run() {
				documentation.show(info);
				cancel();
			}
		};

		widget.addMouseOverHandler(new MouseOverHandler(){
			@Override
			public void onMouseOver(MouseOverEvent event) {
				timer.schedule(DOC_DELAY_MILLIS);
			}
		});

		widget.addMouseOutHandler(new MouseOutHandler(){
			@Override
			public void onMouseOut(MouseOutEvent event) {
				timer.cancel();
			}
		});
	}
	

	// TODO refactor the mkImage functions so null isn't passed around
	
	private Image mkImage(String path, String style, String tooltip){
		return mkImage(path, style, tooltip, null);
	}
	
	private Image mkImage(String path, String style, String tooltip, Widget widget){
		return mkImage(path,style, new HTML(tooltip), widget);
	}
	
	private Image mkImage(String path, String style, HTML tooltip, Widget widget){
		Image result = new Image(path);
		result.setStylePrimaryName(style);
		result.setWidth(ICON_COL_WIDTH + "px"); 
		result.setHeight(ICON_COL_WIDTH + "px");
		
		if (tooltip != null){
			ToolTips.addTooltip(result, tooltip, true, true);
		}
		if (widget != null){
			addDocChange(result, widget);
		}
		return result;
	}
	
	/**
	 * Returns <code>true</code> iff <code>clause</code> has failure reason associated with it
	 * that is not from a post-condition.
	 * @param clause the clause
	 * @return <code>true</code> iff <code>clause</code> has failure reason associated with it.
	 */
	private boolean hasEscJavaFeedback(Clause clause){
		return clause.getReason() != null
				&& !clause.getReason().trim().equals("") 
				&& !clause.getReason().trim().endsWith("(Post)");
	}
		
	/**
	 * Memoized call to {@link DynamicFeedbackPanel#DynamicFeedbackPanel(DynamicFeedback)}
	 * @param feedback the dynamic feedback
	 * @return the corresponding dynamic feedback view
	 */
	private DynamicFeedbackPanel buildDynamicFeedback(DynamicFeedback feedback){
		if (dynamicFeedbackUxCache.containsKey(feedback)){
			return dynamicFeedbackUxCache.get(feedback);
		}
	
		DynamicFeedbackPanel result = new DynamicFeedbackPanel(feedback);
		dynamicFeedbackUxCache.put(feedback, result);
		return result;
	}
	
	private void updateTable(){
		FlexTable table = specTable;
		FlexCellFormatter formatter = table.getFlexCellFormatter();
		
		table.removeAllRows();
		
		int row = 0;
	
		boxExplain.setVisible(clauses.size() > 0);
		
		for (final Clause clause : clauses){
			
			if (hiddenContracts.contains(clause.getClause().trim())){
				continue;
			}
			
			Image up = mkImage("img/up_arrow.jpg","img-btn","Add condition to scratch pad");
			up.addClickHandler(new ClickHandler(){
				@Override
				public void onClick(ClickEvent event) {
					dndWriter.addToScratch(clause.getClause());
				}
			});
			
			table.setWidget(row, UP_COL, up);
			up.setWidth(ICON_COL_WIDTH + "px");
			formatter.setWidth(row, UP_COL, ICON_COL_WIDTH + "px");
			
			if (hasEscJavaFeedback(clause) || dynamicFeedback.containsKey(clause)){
				
				HTML txt = new HTML("The condition is falsified during a correct method call");
				
				Image infoIcon = hasEscJavaFeedback(clause) ? 
						mkImage("img/get_info.png","img-help", clause.getReason()) 
						: mkImage("img/get_info.png", "img-help",txt, buildDynamicFeedback(dynamicFeedback.get(clause)));
				Image removeIcon = mkImage("img/redx.gif","img-btn", "Remove condition (cannot undo)");
				
				table.setWidget(row, INFO_COL, infoIcon);
				formatter.setWidth(row, INFO_COL, ICON_COL_WIDTH + "px");
				
				Widget contract = createContractWidget(clause);
				
				if (dynamicFeedback.containsKey(clause) && dynamicFeedback.get(clause).getFragment() != null){
					// the clause has dynamic feedback 
					
					// cause mouse-over to display feedback
					FocusPanel focusWrapper = new FocusPanel();
					focusWrapper.add(contract);
					addDocChange(focusWrapper, buildDynamicFeedback(dynamicFeedback.get(clause)));
					
					table.setWidget(row, INV_COL, focusWrapper);
					
				}else{
					table.setWidget(row, INV_COL, contract);
				}
			
				formatter.setStyleName(row,INV_COL, statusToClass(clause.getStatus()));
				
				table.setWidget(row, REMOVE_COL,removeIcon);
				formatter.setWidth(row, REMOVE_COL, ICON_COL_WIDTH + "px");
			
				removeIcon.addClickHandler(new ClickHandler(){
					@Override
					public void onClick(ClickEvent event) {
						removeClause(clause);
					}
				});
			
			}else if (clause.getStatus().equals(Status.PENDING)){
				Image pendingIcon = mkImage("img/loader-small.gif", "img-wait", "Evaluating condition (please wait)");
				table.setWidget(row, INFO_COL, pendingIcon);
				formatter.setWidth(row, INFO_COL, ICON_COL_WIDTH + "px");
				
				table.setWidget(row, REMOVE_COL,createContractWidget(clause));
				formatter.setStyleName(row, REMOVE_COL, statusToClass(clause.getStatus()));
				formatter.setColSpan(row, REMOVE_COL, 2);
				
			}else{
				Image removeIcon = mkImage("img/redx.gif","img-btn","Remove condition (cannot undo)");
				
				table.setWidget(row, INFO_COL, removeIcon);
				formatter.setWidth(row, INFO_COL, ICON_COL_WIDTH + "px");
			
				removeIcon.addClickHandler(new ClickHandler(){
					@Override
					public void onClick(ClickEvent event) {
						removeClause(clause);
					}
				});
				
				table.setWidget(row, REMOVE_COL, createContractWidget(clause));
								
				formatter.setStyleName(row,REMOVE_COL, statusToClass(clause.getStatus()));
				formatter.setColSpan(row, REMOVE_COL, 2);
			}
			row++;
		}
	}
	
	/**
	 * Memoized creation of views for contracts; the {@link JmlSpan} for the 
	 * contract is saved in {@link ClauseWriter#contractParseCache}.
	 * @param contract the contract
	 * @return the view for the contract
	 */
	private Panel createContractWidget(final Clause contract){
		final String clauseText = contract.getClause();
		
		if (contractParseCache.containsKey(clauseText)){
			JmlSpan cached = contractParseCache.get(clauseText);
			
			HTMLPanel span = new HTMLPanel(cached.generateHtml(highlightManager));
			highlightManager.registerMouseListener(span);
			return span;
			
		}else{
			final SimplePanel spanContainer = new SimplePanel();
		
			// use a non-interactive label until the clause is parsed
			spanContainer.setWidget(new HTML(clauseText));
			
			veriService.specToSpan(clauseText, new AsyncCallback<JmlSpan>(){
				@Override
				public void onSuccess(JmlSpan result) {
					// cache the parsed value
					contractParseCache.put(clauseText, result);
					
					// replace the non-interactive label with the interactive span
					HTMLPanel span = new HTMLPanel(result.generateHtml(highlightManager));
					highlightManager.registerMouseListener(span);
					spanContainer.setWidget(span);
				}
				@Override
				public void onFailure(Throwable caught) {
					System.out.println("Error parsing " + contract.getClause() + "; using non-interactive label");
				}
			});
			return spanContainer;
		}
	}
	
	/**
	 * Update the given contract in the table (as determined by contract text)
	 * @param clause the new contract
	 */
	private void updateSpec(Clause clause){	
		for (int i = 0; i < clauses.size(); i++){
			if (SpecUtil.invEqual(clauses.get(i), clause)){
				clauses.set(i, clause);
				break;
			}
		}
		updateTable();
	}
	
	
	private void trySubmit(String n){
		if (!"".equals(n)){
			if (SpecUtil.anyEqual(clauses, n)){
				Window.alert("The clause you entered has already been entered.");
			}else if(existsCheck.alreadyExists(n)){
				Window.alert("The clause you entered is already known.");
			}else{
				clauses.add(new Clause(n,"WEB",Status.PENDING));
				if (listener != null){
					listener.added(n, new SpecDecisionHandler(){
						@Override
						public void onDecision(Clause spec) {
							updateSpec(spec);
						}
					});
				}
				updateTable();
			}
		}
	}

	/**
	 * Javascript to register user mouse tracking
	 */
	public native void registerMouseTracking() /*-{
		$wnd.mx = 0;
		$wnd.my = 0;
		$wnd.IE = document.all?true:false;
		if (!$wnd.IE) document.captureEvents(Event.MOUSEMOVE)
		
		
		$wnd.getMouseXY = function(e) {
		if ($wnd.IE) { // grab the x-y pos.s if browser is IE
			$wnd.mx = event.clientX + document.body.scrollLeft;
			$wnd.my = event.clientY + document.body.scrollTop;
		}
		else {  // grab the x-y pos.s if browser is NS
			$wnd.mx = e.pageX;
			$wnd.my = e.pageY;
		}  
		}
		
		$wnd.onmousemove = $wnd.getMouseXY;
	}-*/;
}
