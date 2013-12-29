package com.veriasa.speceditor.client;


import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;


public class CodeView extends Composite{

	private final HTML src = new HTML();
	private final VerticalPanel vp = new VerticalPanel();
	
	private String code;
	
	/**
	 * true iff line numbers should be displayed
	 */
	private static boolean GUTTER = false;
	
	/**
	 * true iff the information box should be displayed
	 */
	private static boolean TOOLBAR = false;
	

	public CodeView(){
		initWidget(vp);
		vp.add(src);
		SetCode("public function foo(){\n int bar = 0; \n}",false);	
	}

	
	
	
	public void SetCode(String code, boolean highlight){
		
		this.code = code.replaceAll("<", "&lt;").replaceAll(">","&gt;")
			.replaceAll("\\[/REF\\]", "</span>")
			.replaceAll("\\[REF", "<span class=\"hasinfo\" ")
			.replaceAll("REF\\]", ">");
		
		src.setHTML("<pre class=\"brush: csharp; gutter: " + ( GUTTER ? "true" : "false") + "; toolbar: " + 
				(TOOLBAR ? "true" : "false")+ "\">" + this.code +  "</pre>");
		
		
		/*if (highlight){
			highlight();
		}*/
	}
	
	public static native void highlight() /*-{
	   $wnd.SyntaxHighlighter.highlight();
	}-*/;

}
