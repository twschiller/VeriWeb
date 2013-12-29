package com.schiller.veriasa.web.client.views;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.schiller.veriasa.web.client.VeriService;
import com.schiller.veriasa.web.client.VeriServiceAsync;
import com.schiller.veriasa.web.client.dnd.PalettePanel;
import com.schiller.veriasa.web.client.views.DndWrite.ContractMode;
import com.schiller.veriasa.web.client.views.DndWrite.WriteHooks;
import com.schiller.veriasa.web.shared.dnd.InvElement;

public class FreeWrite extends Composite implements RequiresResize{

	private final VeriServiceAsync veriService = GWT
		.create(VeriService.class);
	
	private final VerticalPanel main = new VerticalPanel();
	
	private ContractMode mode = ContractMode.POST;
	private WriteHooks hooks;
	private WidthProvider widthProvider;
	
	private final static int DEFAULT_SPEC_WIDTH = 360;
	private final static int DEFAULT_COL_WIDTH = 375;
	
	private final TextArea inv = new TextArea();
	private final Button submitBtn = new Button("Submit");
	
	private final List<String> params = new ArrayList<String>();
	private final List<InvElement> locals = new ArrayList<InvElement>();	
	
	
	private final DisclosurePanel dp = new DisclosurePanel("Method parameters and class fields");
	private final PalettePanel varTable = new PalettePanel(null);
	
	public void setWriteHooks(WriteHooks hooks){
		this.hooks = hooks;
	}
	
	public String cleanInv(String dirty){
		return dirty.replace(new String("\r"), "").replace(new String("\n"), "").replaceAll("\\s+", " ");
	}
	
	private Label varLbl(String txt){
		txt = txt.replace((CharSequence)"<hole>", "..").replace(" ", "");
		Label l = new Label(txt);
		
		l.setStylePrimaryName("freewrite-frag");
		return l;
	}
	
	private void updateVarTable(){
		varTable.clear();
		varTable.removeAllRows();
		
		varTable.setStylePrimaryName("freewrite-varTable");
		
		if (!params.isEmpty() || !locals.isEmpty()){
			for (String p : params){
				varTable.add(varLbl(p));
			}
			for (InvElement p : locals){
				varTable.add(varLbl(p.getValue()));
			}
		}else{
			varTable.setWidget(0, 0, new Label("There are no method parameters or class fields"));
		}
	}
	
	public FreeWrite(){
		initWidget(main);
		
		main.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
	
		main.setWidth(DEFAULT_COL_WIDTH + "px");
		
		main.add(inv);
		
		final String defTxt = "Type a condition here and click submit";
		
		inv.setText(defTxt);
	
		inv.addFocusHandler(new FocusHandler(){
			@Override
			public void onFocus(FocusEvent event) {
				if (inv.getText().trim().equals(defTxt)){
					inv.setText("");
				}
			}
		});
		
		inv.addBlurHandler(new BlurHandler(){
			@Override
			public void onBlur(BlurEvent event) {
				if (inv.getText().trim().isEmpty()){
					inv.setText(defTxt);
				}
			}
		});
		
		inv.setWidth(DEFAULT_SPEC_WIDTH + "px");
		inv.setStylePrimaryName("freewrite-inv");
	
		main.add(submitBtn);

		dp.setWidth(DEFAULT_SPEC_WIDTH + "px");
		
		//main.add(varTable);
		dp.setContent(varTable);
		dp.setOpen(true);
		
		main.add(dp);
		
		varTable.setWidth(DEFAULT_SPEC_WIDTH + "px");
		
		submitBtn.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				if (hooks != null){
					hooks.onSubmit(cleanInv(inv.getText()));
				}	
			}
		});

		veriService.requestLocals(new AsyncCallback<List<InvElement>>(){
			@Override
			public void onFailure(Throwable caught) {
				locals.clear();
			}
			@Override
			public void onSuccess(List<InvElement> result) {
				locals.clear();
				locals.addAll(result);
				updateVarTable();
			}
		});
		
		veriService.requestParams(new AsyncCallback<List<String>>(){
			@Override
			public void onFailure(Throwable caught) {
				params.clear();
			}
			@Override
			public void onSuccess(List<String> result) {
				params.clear();
				params.addAll(result);
				updateVarTable();
			}
		});
	}
	
	public void setWidthProvider(WidthProvider widthProvider){
		this.widthProvider = widthProvider;
	}
	
	@Override
	public void onResize() {
		if (widthProvider != null){
			int w = widthProvider.getWidth() - 20;
			main.setWidth((w + 5) + "px");
			inv.setWidth(w + "px");
			dp.setWidth(w + "px");
			varTable.setWidth(w + "px");
		}
	}
}
