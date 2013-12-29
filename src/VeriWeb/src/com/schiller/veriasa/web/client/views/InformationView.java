package com.schiller.veriasa.web.client.views;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasMouseOutHandlers;
import com.google.gwt.event.dom.client.HasMouseOverHandlers;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.schiller.veriasa.web.client.VeriService;
import com.schiller.veriasa.web.client.VeriServiceAsync;
import com.schiller.veriasa.web.shared.core.Clause;
import com.schiller.veriasa.web.shared.problems.Documentation;
import com.schiller.veriasa.web.shared.problems.MethodDocumentation;

/**
 * Documentation (information) view panel
 * @author Todd Schiller
 */
public class InformationView extends Composite{
	private final VeriServiceAsync veriService = GWT.create(VeriService.class);
	private static final boolean STRIP_DOC_ANCHORS = true;

	public enum Display { WARNINGS, INSTRUCTIONS, DOCUMENTATION, NONE, UNKNOWN };

	public static interface DocumentationHandler{
		void addEnsures(String method);
		void addExsures(String method, String statement);
		void removeRequires(String method, Clause statement);
		void copyStatement(Clause statement);
		boolean canCopy();
	}
	
	private DocumentationHandler handler;
	private final SimplePanel main = new SimplePanel();
	private Display displayType = Display.NONE;
	
	protected InformationView(){
		this(null);
	}
	
	public InformationView(DocumentationHandler handler){
		initWidget(main);
		this.handler = handler;
	}
	
	public void setHandler(DocumentationHandler handler){
		this.handler = handler;
	}
	
	private static HTML invariant(Clause i){
		HTML xx =new HTML(i.getClause());
		xx.setStylePrimaryName("doc-spec");
		return xx;
	}
	private static HTML header(String text, boolean bad){
		HTML h = new HTML(text);
		h.setStylePrimaryName(bad ? "doc-spec-header-bad" : "doc-spec-header");
		return h;
	}
	private static HTML header(String text){
		return header(text,false);
	}
	
	public interface BtnDef{
		String getCaption();
		ClickHandler getHandler(Clause spec, String signature);
	}
	
	public static void addTooltip(final Widget w, final String text){
		if (w instanceof HasMouseOverHandlers && w instanceof HasMouseOutHandlers){
			final PopupPanel infoPopup = new PopupPanel(true);
			
			final MouseOverHandler moh = new MouseOverHandler(){
				@Override
				public void onMouseOver(MouseOverEvent event) {
					infoPopup.clear();
					infoPopup.add(new Label(text));
					
					infoPopup.setPopupPosition(event.getClientX() + 5, event.getClientY() + 10);
					
					if (!infoPopup.isShowing()){
						infoPopup.show();
					}
				}
			};
			final MouseOutHandler mout = new MouseOutHandler(){
				@Override
				public void onMouseOut(MouseOutEvent event) {
					infoPopup.hide();
				}
			};
			((HasMouseOverHandlers) w).addMouseOverHandler(moh);
			((HasMouseOutHandlers) w).addMouseOutHandler(mout);
		}else{
			throw new RuntimeException("Cannot add tooltip to widget");
		}
	}
	
	public static FlexTable genSpecTable(List<Clause> specs, MethodDocumentation doc, BtnDef def, final DocumentationHandler handler){
		FlexTable t = new FlexTable();
		int row = 0;
		
		if (handler.canCopy()){
			t.getColumnFormatter().setWidth(0, "20px");
		}
		
		for (final Clause s : specs){
			boolean shift = false;
			
			if (handler.canCopy()){
				Image up = new Image("img/up_arrow.jpg");
				up.setWidth("20px"); up.setHeight("20px");
				up.addStyleName("img-btn");
				addTooltip(up, "Add condition to scratch pad");
				up.addClickHandler(new ClickHandler(){
					@Override
					public void onClick(ClickEvent event) {
						handler.copyStatement(s);
					}
				});
				shift = true;
				t.setWidget(row, 0, up);
				
			}
			
			t.setWidget(row, shift ? 1 : 0, invariant(s));
			t.getCellFormatter().setStylePrimaryName(row, shift ? 1 : 0, "doc-inv");
			
			if (def != null){
				Button b = new Button(def.getCaption());
				b.addClickHandler(def.getHandler(s,doc.getSignature()));
				t.setWidget(row, shift ? 2 : 1,b);
			}
			row++;
		}
		
		return t;
	}
	
	private void addRequires(final MethodDocumentation doc, final VerticalPanel panel){
		if (doc.getRequires().isEmpty()){
			panel.add(header("No Preconditions"));
		}else{
			panel.add(header("Preconditions:"));
			
			FlexTable t = genSpecTable(doc.getRequires(), doc, new BtnDef(){
				@Override
				public String getCaption() {
					return "Bad";
				}

				@Override
				public ClickHandler getHandler(final Clause spec,final String signature) {
					return new ClickHandler(){
						@Override
						public void onClick(ClickEvent event) {
							InformationView.this.handler.removeRequires(signature, spec);
						}
					};
				}
			}, handler);
			panel.add(t);
		}
	}
	
	private void addEnsures(final MethodDocumentation doc, final VerticalPanel panel){
		HorizontalPanel head = new HorizontalPanel();
		//head.setWidth("100%");
		HTML h = doc.getEnsures().isEmpty() 
				? header("No Regular Postconditions",true)
				: header("Postconditions:");
		
		head.add(h);
		
		Label spc = new Label();
		head.add(spc);
		head.setCellWidth(spc, "10px");
		
		head.setCellVerticalAlignment(h, HasVerticalAlignment.ALIGN_MIDDLE);
		
		Button b = new Button("Add Postcondition");
		b.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				handler.addEnsures(doc.getSignature());
			}
		});
		head.add(b);
		head.setCellHorizontalAlignment(b,HasHorizontalAlignment.ALIGN_LEFT);
		
		head.setStylePrimaryName("doc-sec-head");
		
		panel.add(head);
		
		if (!doc.getEnsures().isEmpty()){
			FlexTable t = genSpecTable(doc.getEnsures(), doc, null, handler);
			panel.add(t);
		}
	}
	
	private void addExsures(final MethodDocumentation doc, final VerticalPanel panel){
		for (final String ex : doc.getExsures().keySet()){
			List<Clause> xxx = doc.getExsures().get(ex);
			
			HorizontalPanel head = new HorizontalPanel();
			//head.setWidth("100%");
			
			HTML h = xxx.isEmpty() 
					? header("No postconditions for " + ex, true)
					: header("Postconditions for " + ex);
			
			head.add(h);
			head.setCellVerticalAlignment(h, HasVerticalAlignment.ALIGN_MIDDLE);

			Label spc = new Label();
			head.add(spc);
			head.setCellWidth(spc, "10px");
			
			Button b = new Button("Add Exceptional Postcondition");
			b.addClickHandler(new ClickHandler(){
				@Override
				public void onClick(ClickEvent event) {
					handler.addExsures(doc.getSignature(),ex);
				}
			});
			head.add(b);
			head.setCellHorizontalAlignment(b,HasHorizontalAlignment.ALIGN_LEFT);
			head.setStylePrimaryName("doc-sec-head");
			panel.add(head);

			if (!xxx.isEmpty()){
				FlexTable t = genSpecTable(xxx, doc, null, handler);
				panel.add(t);
			}
		}
	}
	
	
	public void update(String caption, Documentation contents){
		if (contents instanceof MethodDocumentation){		
			//add content
			VerticalPanel contentPanel = new VerticalPanel();
			contentPanel.add(new HTML(STRIP_DOC_ANCHORS ? stripAnchors(contents.getDocHtml()) : contents.getDocHtml()));
			final MethodDocumentation d = (MethodDocumentation) contents;
			addRequires(d, contentPanel);
			addEnsures(d, contentPanel);
			addExsures(d, contentPanel);
			contentPanel.setWidth("500px"); contentPanel.setHeight("100%");
			
			addPopupInstr(contentPanel,handler.canCopy());
		
			main.setWidget(contentPanel);			
		}else if (contents != null){
			update(caption,contents.getDocHtml());
		}
	}
	
	public void update(String caption,String htmlContents){
		if (STRIP_DOC_ANCHORS){
			htmlContents = stripAnchors(htmlContents);
		}
		HTML h = new HTML(htmlContents);
		
		h.setWidth("500px");
		main.setWidget(h);
	}
	
	private static String stripAnchors(String html){
		html = html.replaceAll("\\<A.*?\\>", "");
		html = html.replaceAll("\\</A.*?\\>", "");
		html = html.replaceAll("\\<a.*?\\>", "");
		html = html.replaceAll("\\</a.*?\\>", "");
		return html;
	}
	
	private static void addPopupInstr(VerticalPanel o, boolean includeScratch){
		if (includeScratch){
			HTML hh = new HTML("<p>Clicking <img src=\"img/up_arrow.jpg\" width=\"15\" height=\"15\"/> copies a condition to the scratch pad</p>");
			o.add(hh);
			o.setCellVerticalAlignment(hh, HasVerticalAlignment.ALIGN_BOTTOM);
			o.setCellHorizontalAlignment(hh,HasHorizontalAlignment.ALIGN_CENTER);
			o.setCellHeight(hh, "15px");
		}
	}
	
	public void showDoc(int docId){
		veriService.requestDoc(docId, new AsyncCallback<Documentation>(){
			@Override
			public void onFailure(Throwable caught) {
				// TODO Auto-generated method stub
			}
			@Override
			public void onSuccess(Documentation result) {
				update("Documentation",result);	
				displayType = Display.DOCUMENTATION;
			}
		});
	}

	public void showWarning(int warningId){
		veriService.requestWarning(warningId, new AsyncCallback<String>(){
			@Override
			public void onFailure(Throwable caught) {
				// TODO Auto-generated method stub
			}
			@Override
			public void onSuccess(String result) {
				update("Warnings",result);	
				displayType = Display.WARNINGS;
			}
		});
	}
	
	/**
	 * A hack to show other types of information in the Documentation View panel
	 * @param w the widget
	 * @deprecated 
	 */
	public void show(Widget w){
		main.clear();
		main.setWidget(w);
		displayType = Display.UNKNOWN;
	}
	
	public Display getDisplayType(){
		return displayType;
	}
}
