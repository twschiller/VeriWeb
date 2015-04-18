package com.schiller.veriasa.web.client.views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.schiller.veriasa.web.client.VeriService;
import com.schiller.veriasa.web.client.VeriServiceAsync;
import com.schiller.veriasa.web.shared.core.SourceElement.LanguageType;
import com.schiller.veriasa.web.shared.core.Clause;
import com.schiller.veriasa.web.shared.problems.Documentation;
import com.schiller.veriasa.web.shared.problems.MethodDocumentation;

/**
 * Source code display supporting inline conditions
 * @author Todd Schiller
 */
public class CodeView extends Composite{
	private final VeriServiceAsync veriService = GWT.create(VeriService.class);
	
	private static final int DOC_DELAY_MILLIS = 1000;
	
	private String code;
	
	private final VerticalPanel panel = new VerticalPanel();
	private FlexTable displayTable;

	private InformationView documentation;
	
	private final HashSet<Integer> showing = new HashSet<Integer>();
	
	/**
	 * Uses the postId as the index
	 */
	private final HashSet<String> showUnprovenEns = new HashSet<String>();
	
	/**
	 * Uses the postId as the index
	 */
	private final HashSet<String> showUnprovenExs = new HashSet<String>();
	
	
	private final HashMap<Integer, Widget> pres = new HashMap<Integer,Widget>();
	private final HashMap<Integer, Widget> posts = new HashMap<Integer,Widget>();
	
	/**
	 * true iff line numbers should be displayed
	 */
	private static boolean GUTTER = false;
	
	/**
	 * true iff the information box should be displayed
	 */
	private static boolean TOOLBAR = false;
	
	private final HashMap<Integer, Timer> docTimers = new HashMap<Integer,Timer>();
	private final HashMap<Integer, Timer> warningTimers = new HashMap<Integer,Timer>();
	
	public CodeView(){
		initWidget(panel);
		exportChangeDoc();
		exportChangeWarning();
		exportToggleDoc();
		exportCancelDoc();
		exportCancelWarning();
		this.documentation = null;
	}
	
	public void setCode(String code, LanguageType language, boolean highlight){
		this.code = code.replaceAll("<", "&lt;").replaceAll(">","&gt;")
			.replaceAll("\\[\\[DOC\\]\\]", "</span>")
			.replaceAll("\\[\\[DOCSR", "<span class=\"has-doc\" ")
			.replaceAll("\\[\\[DOCST", "<span class=\"has-doc-toggle\" ")
			.replaceAll("DOC\\]\\]", ">")
			.replaceAll("\\[\\[ERR\\]\\]", "</span>")
			.replaceAll("\\[\\[ERR", "<span class=\"has-warning\" ")
			.replaceAll("ERR\\]\\]", ">");
		
		this.code = fixIndent(this.code);
		
		this.panel.clear();
		this.displayTable = createTable();
		panel.add(this.displayTable);
	}
	
	public void setDocPanel(InformationView docPanel){
		this.documentation = docPanel;
	}
	
	public static String fixIndent(String body){
		String lines [] = body.split("\n");
		
		if (lines[0].trim().startsWith("/**") && lines[1].trim().startsWith("*")){
			int i1 = lines[0].indexOf('/');
			int i2 = lines[1].indexOf('*') - 1; 
			
			StringBuilder sb = new StringBuilder();
		
			sb.append(lines[0].substring(i1)).append("\n");
			for (int j = 1; j < lines.length; j++){
				if (lines[j].length() < i2){
					sb.append(lines[j]).append("\n");
				}else{
					sb.append(lines[j].substring(i2)).append("\n");
				}
			}
			
			return sb.toString();
		}else{
			return body;
		}	
	}
	
	public static String fixIndent2(String body){
		String lines [] = body.split("\n");
		StringBuilder sb = new StringBuilder();
		for (String line : lines){
			if (line.trim().isEmpty()){
				sb.append("\n");
			}else{
				int i = line.indexOf(line.trim().charAt(0));
				for (int j = 0 ; j < i; j++){
					sb.append("&nbsp;");
				}
				sb.append(line.trim()).append("\n");
			}
		}
		return sb.toString();
	}
	
	private static int leadingWhitespace(String line){
		if (line.trim().isEmpty()){
			return 0;
		}else{
			return line.indexOf(line.trim().charAt(0));
		}
	}
	
	private static List<Integer> leadings(String[] lines){
		Set<Integer> s = new HashSet<Integer>();
		for (String line : lines){
			s.add(leadingWhitespace(line));
		}
		List<Integer> ls = new ArrayList<Integer>(s);
		Collections.sort(ls);
		return ls;
	}
	
	private static boolean isSlotLine(String line){
		return line.trim().startsWith("[[");
	}
	
	private static String[] withoutSlots(String[] lines){
		List<String> x = new ArrayList<String>();
		
		for (String line : lines){
			if (!isSlotLine(line)){
				x.add(line);
			}
		}
		
		return x.toArray(new String[]{});
		
	}
	
	private FlexTable createTable(){
		FlexTable displayTable = new FlexTable();
		String[] lines = code.split("\n");
		
		List<Integer> leading = leadings(withoutSlots(lines));
		int n = leading.size();
		displayTable.setCellPadding(0);
		displayTable.setCellSpacing(0);
		displayTable.setWidth("600px");
		displayTable.setStylePrimaryName("code-table");
		for (int i = 0; i < lines.length; i++){
			if (isSlotLine(lines[i])){
				FlexTable p = new FlexTable();
				p.setWidth("100%");
				p.setCellPadding(0);
				p.setCellSpacing(0);
				int s = lines[i].indexOf('\"');
				int e = lines[i].indexOf('\"', s+1);
				String id = lines[i].substring(s+1, e);
				
				if (id.toUpperCase().contains("PRE")){
					pres.put( Integer.parseInt(id.substring("pre".length())) , p);
				}else{
					posts.put( Integer.parseInt(id.substring("post".length())) , p);
				}
				
				displayTable.setWidget(i, 0, p);
				displayTable.getCellFormatter().addStyleName(i, 0, "code-cell");
				displayTable.getFlexCellFormatter().setColSpan(i, 0, n);
			}else{
				int x = leadingWhitespace(lines[i]);
				int y = leading.indexOf(x);
				displayTable.setHTML(i, y, "<span class=\"source-code\">" + lines[i].trim() +  "</span>" );
				displayTable.getCellFormatter().setWordWrap(i, y, false);
				displayTable.getCellFormatter().addStyleName(i, y, "code-cell");
				displayTable.getFlexCellFormatter().setColSpan(i, y, n-y);
			}
		}
		
		for (int i = 0; i < n - 1; i++){
			displayTable.getColumnFormatter().setWidth(i, ((leading.get(i+1) - leading.get(i))*6) + "px");
		}
		return displayTable;
	}
	


	private static String langToStr(LanguageType t){
		switch(t){
		case JAVA:
			return "java";
		case CSHARP:
			return "csharp";
		default:
			throw new IllegalArgumentException("Unsupported language " + t.toString());
		}
	}
	
	private static void setRowStyle(FlexTable t){
		t.setStylePrimaryName("code-spec-table");
	}
	
	void fetchDoc(final int id, final int warningId, final FlexTable pre, final String preId,  final FlexTable post, final String postId){
		veriService.requestDoc(id, warningId >= 0 ? warningId : null, new AsyncCallback<Documentation>(){
			@Override
			public void onFailure(Throwable caught) {
				// TODO Auto-generated method stub
			}
			@Override
			public void onSuccess(Documentation result) {
				if (result instanceof MethodDocumentation){
					final MethodDocumentation d = (MethodDocumentation) result;
					Element docE = DOM.getElementById("doc" + postId.substring("POST".length()));
					int left = docE.getAbsoluteLeft();
					
					post.removeAllRows();
					pre.removeAllRows();
					pre.setWidth("100%");
					post.setWidth("100%");
					
					assert(post.getRowCount() == 0);
					assert(pre.getRowCount() == 0);
					
					if (d.getRequires().isEmpty()){
						int r = pre.getRowCount();
						HTML h =  new HTML("<span class=\"good-pre\">No Preconditions</span>");
						pre.setWidget(r, 1, h);
						pre.getFlexCellFormatter().setColSpan(r, 1, 2);
					}else{
						for (Clause s : d.getRequires()){
							int r = pre.getRowCount();
							
							if (d.getBadRequires().contains(s)){
								HTML h = new HTML("<span class=\"bad-pre\">" + s.getClause().trim() + "</span>");
								pre.setWidget(r, 1, new HTML("<span class=\"bad-pre\">NOT MET:</span>"));
								pre.setWidget(r, 2, h);
								pre.getCellFormatter().setAlignment(r, 1, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_TOP);
								pre.getCellFormatter().setAlignment(r, 2, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_TOP);
								//pre.getCellFormatter().setWordWrap(r, 2, false);
							}else{
								HTML h = new HTML("<span class=\"good-pre\">" + s.getClause() + "</span>");
								pre.setWidget(r, 1, new HTML("<span class=\"good-pre\">PRE MET:</span>"));
								pre.setWidget(r, 2, h);
								pre.getCellFormatter().setAlignment(r, 1, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_TOP);
								pre.getCellFormatter().setAlignment(r, 2, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_TOP);
								//pre.getCellFormatter().setWordWrap(r, 2, false);
							}	
						}
					}
					
					assert(post.getRowCount() == 0);
					
					if (d.getEnsures().isEmpty()){
						int r = post.getRowCount();
						HTML h =  new HTML("<span class=\"bad-post\">No PROVEN Postconditions</span>");
						post.setWidget(r, 1, h);
						post.getFlexCellFormatter().setColSpan(r, 1, 2);
					}
					for (Clause s : d.getEnsures()){
						int r = post.getRowCount();
						HTML h = new HTML("<span class=\"good-post\">" + s.getClause() + "</span>");
						post.setWidget(r, 1, new HTML("<span class=\"good-post\">POST:</span>"));
						post.setWidget(r, 2, h);
						post.getCellFormatter().setAlignment(r, 1, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_TOP);
						post.getCellFormatter().setAlignment(r, 2, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_TOP);
						//post.getCellFormatter().setWordWrap(r, 2, false);
					}	
					
					if (!d.getBadEnsures().isEmpty()){
						if (showUnprovenEns.contains(postId)){
							{
								int r = post.getRowCount();
								FocusPanel clickable = new FocusPanel();
								HorizontalPanel fp = new HorizontalPanel();
								
								clickable.addClickHandler(new ClickHandler(){
									@Override
									public void onClick(ClickEvent event) {
										showUnprovenEns.remove(postId);
										fetchDoc(id, warningId,pre,preId, post,postId);
									}	
								});
								HTML h =  new HTML("<span class=\"unproven-post\">Hide unproven postconditions</span>");
								
								fp.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
								fp.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
							    Image middle_down = new Image("img/down.png");
							    middle_down.setWidth("14px"); middle_down.setHeight("7px");
								fp.add(middle_down);
								fp.add(h);
								fp.addStyleName("img-btn");
								clickable.addStyleName("img-btn");
								clickable.setWidget(fp);
								
								post.setWidget(r, 1, clickable);
								post.getFlexCellFormatter().setColSpan(r, 1, 2);
							}
							for (Clause s : d.getBadEnsures()){
								int r = post.getRowCount();
								HTML h = new HTML("<span class=\"unproven-post\">" + s.getClause() + "</span>");
								post.setWidget(r, 1, new HTML("<span class=\"unproven-post\">POST:</span>"));
								post.setWidget(r, 2, h);
								post.getCellFormatter().setAlignment(r, 1, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_TOP);
								post.getCellFormatter().setAlignment(r, 2, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_TOP);
								//post.getCellFormatter().setWordWrap(r, 2, false);	
							}
						}else{
							int r = post.getRowCount();
							FocusPanel clickable = new FocusPanel();
							HorizontalPanel fp = new HorizontalPanel();
							
							clickable.addClickHandler(new ClickHandler(){
								@Override
								public void onClick(ClickEvent event) {
									showUnprovenEns.add(postId);
									fetchDoc(id, warningId,pre,preId, post,postId);
								}	
							});
							HTML h =  new HTML("<span class=\"unproven-post\">Show " + d.getBadEnsures().size() 
									+ " unproven postconditions</span>");
							//fp.setWidth(post.getOffsetWidth() + "px");
							fp.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
							fp.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
							Image middle_right = new Image("img/right.png");
							
							middle_right.setWidth("7px"); middle_right.setHeight("14px");
							fp.add(middle_right);
							fp.add(h);
							
							fp.addStyleName("img-btn");
							clickable.addStyleName("img-btn");
							clickable.setWidget(fp);
							post.setWidget(r, 1, clickable);
							post.getFlexCellFormatter().setColSpan(r, 1, 2);
						}
					}
					
					if (!d.getExsures().isEmpty() && d.getExsures().get("RuntimeException").isEmpty()){
						int r = post.getRowCount();
						HTML h =  new HTML("<span class=\"bad-post\">No PROVEN Exceptional Postconditions</span>");
						post.setWidget(r, 1, h);
						post.getFlexCellFormatter().setColSpan(r, 1, 2);
					}
					if (!d.getExsures().isEmpty()){
					
						for (Clause s : d.getExsures().get("RuntimeException")){
							int r = post.getRowCount();
							HTML h = new HTML("<span class=\"good-post\">" + s.getClause() + "</span>");
							post.setWidget(r, 1, new HTML("<span class=\"good-post\">EX:</span>"));
							post.setWidget(r, 2, h);
							post.getCellFormatter().setAlignment(r, 1, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_TOP);
							post.getCellFormatter().setAlignment(r, 2, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_TOP);
							//post.getCellFormatter().setWordWrap(r, 2, false);
						}	
					}
					
					if (!d.getBadExsures().isEmpty() && !d.getBadExsures().get("RuntimeException").isEmpty()){
						if (showUnprovenExs.contains(postId)){
							{
								int r = post.getRowCount();
								FocusPanel clickable = new FocusPanel();
								HorizontalPanel fp = new HorizontalPanel();
								
								clickable.addClickHandler(new ClickHandler(){
									@Override
									public void onClick(ClickEvent event) {
										showUnprovenExs.remove(postId);
										fetchDoc(id, warningId,pre,preId, post,postId);
									}	
								});
								HTML h =  new HTML("<span class=\"unproven-post\">Hide unproven exceptional post-conditions</span>");
								
								fp.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
								fp.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
							    Image middle_down = new Image("img/down.png");
							    middle_down.setWidth("14px"); middle_down.setHeight("7px");
								fp.add(middle_down);
								fp.add(h);
								fp.addStyleName("img-btn");
								clickable.addStyleName("img-btn");
								clickable.setWidget(fp);
								
								post.setWidget(r, 1, clickable);
								post.getFlexCellFormatter().setColSpan(r, 1, 2);
							}
							for (Clause s : d.getBadExsures().get("RuntimeException")){
								int r = post.getRowCount();
								HTML h = new HTML("<span class=\"unproven-post\">" + s.getClause() + "</span>");
								post.setWidget(r, 1, new HTML("<span class=\"unproven-post\">EX:</span>"));
								post.setWidget(r, 2, h);
								post.getCellFormatter().setAlignment(r, 1, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_TOP);
								post.getCellFormatter().setAlignment(r, 2, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_TOP);
								//post.getCellFormatter().setWordWrap(r, 2, false);	
							}
						}else{
							int r = post.getRowCount();
							FocusPanel clickable = new FocusPanel();
							HorizontalPanel fp = new HorizontalPanel();
							
							clickable.addClickHandler(new ClickHandler(){
								@Override
								public void onClick(ClickEvent event) {
									showUnprovenExs.add(postId);
									fetchDoc(id, warningId,pre,preId, post,postId);
								}	
							});
							HTML h =  new HTML("<span class=\"unproven-post\">Show " + d.getBadExsures().get("RuntimeException").size() 
									+ " unproven exceptional postconditions</span>");
							//fp.setWidth(post.getOffsetWidth() + "px");
							fp.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
							fp.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
							Image middle_right = new Image("img/right.png");
							
							middle_right.setWidth("7px"); middle_right.setHeight("14px");
							fp.add(middle_right);
							fp.add(h);
							
							fp.addStyleName("img-btn");
							clickable.addStyleName("img-btn");
							clickable.setWidget(fp);
							post.setWidget(r, 1, clickable);
							post.getFlexCellFormatter().setColSpan(r, 1, 2);
						}
					}
				
					pre.getColumnFormatter().setWidth(0, (left - displayTable.getAbsoluteLeft()) + "px");
					post.getColumnFormatter().setWidth(0,(left - displayTable.getAbsoluteLeft()) + "px");
					
				}
			}
		});
	}
	
	
	void toggleDoc(int id, int preId, int postId, int warningId){		
		FlexTable pre = (FlexTable) pres.get(preId);
		FlexTable post = (FlexTable) posts.get(postId);
		
		if (pre == null || post == null){
			return;
		}
		
		if (showing.contains(postId)){
			pre.setVisible(false);
			post.setVisible(false);
			showing.remove(postId);
			Element docE = DOM.getElementById("doc" + postId);
			docE.setClassName("has-doc-toggle");
		}else{	
			fetchDoc(id,warningId,pre,"pre" + preId,post,"post" + postId);
			pre.setVisible(true);
			post.setVisible(true);
			showing.add(postId);
			Element docE = DOM.getElementById("doc" + postId);
			docE.setClassName("has-doc-toggle-pressed");
		}
	}
	
	void showDoc(final int id){
		Timer t = new Timer(){
			@Override
			public void run() {
				if (documentation != null){
					documentation.showDoc(id);
					cancel();
				}
			}
		};
		docTimers.put(id, t);
		t.schedule(DOC_DELAY_MILLIS);
	}
	
	void showWarning(final int id){
		Timer t = new Timer(){
			@Override
			public void run() {
				if (documentation != null){
					documentation.showWarning(id);
					cancel();
				}
			}
		};
		warningTimers.put(id, t);
		t.schedule(DOC_DELAY_MILLIS);
	}
	
	void cancelDoc(int id){
		if (docTimers.containsKey(id)){
			docTimers.get(id).cancel();
		}
	}
	
	void cancelWarning(int id){
		if (warningTimers.containsKey(id)){
			warningTimers.get(id).cancel();
		}
	}
	
	public native void exportToggleDoc() /*-{
	var _this = this;
	$wnd.toggleDoc = function(docId, preId, postId, warningId){
		_this.@com.schiller.veriasa.web.client.views.CodeView::toggleDoc(IIII)(docId, preId, postId, warningId);
	};
}-*/;
	
	public native void exportCancelDoc() /*-{
	var _this = this;
	$wnd.cancelDoc = function(docId){
		_this.@com.schiller.veriasa.web.client.views.CodeView::cancelDoc(I)(docId);
	};
}-*/;
	
	public native void exportCancelWarning() /*-{
	var _this = this;
	$wnd.cancelWarning = function(warningId){
		_this.@com.schiller.veriasa.web.client.views.CodeView::cancelWarning(I)(warningId);
	};
}-*/;
	
	public native void exportChangeDoc() /*-{
	var _this = this;
	$wnd.showDoc = function(docId){
		_this.@com.schiller.veriasa.web.client.views.CodeView::showDoc(I)(docId);
	};
}-*/;
	
	public native void exportChangeWarning() /*-{
	var _this = this;
	$wnd.showWarning = function(warningId){
		_this.@com.schiller.veriasa.web.client.views.CodeView::showWarning(I)(warningId);
	};
}-*/;
	
	public static native void highlight() /*-{
	   $wnd.SyntaxHighlighter.highlight();
	}-*/;
}
