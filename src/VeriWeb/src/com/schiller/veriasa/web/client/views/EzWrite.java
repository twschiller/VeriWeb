package com.schiller.veriasa.web.client.views;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.schiller.veriasa.web.client.VeriService;
import com.schiller.veriasa.web.client.VeriServiceAsync;
import com.schiller.veriasa.web.client.dnd.FragmentDefinitions;
import com.schiller.veriasa.web.client.dnd.FragmentDefinitions.Opt;
import com.schiller.veriasa.web.shared.dnd.InvArg;
import com.schiller.veriasa.web.shared.dnd.InvElement;
import com.schiller.veriasa.web.shared.dnd.InvFixed;
import com.schiller.veriasa.web.shared.dnd.InvFree;
import com.schiller.veriasa.web.shared.dnd.InvLocal;
import com.schiller.veriasa.web.shared.dnd.InvMethod;
import com.schiller.veriasa.web.shared.dnd.InvRef;
import com.schiller.veriasa.web.shared.dnd.InvElement.RefType;

//TODO: open manual entry box automatically
//TODO: adding constant should warn about data loss

public class EzWrite extends Composite {

	private final VeriServiceAsync veriService = GWT
		.create(VeriService.class);

	List<String> params = new ArrayList<String>();
	List<String> locals = new ArrayList<String>();
	
	HashMap<String,List<String>> localEdges = new HashMap<String,List<String>>();
	
	public interface ChangeListener{
		void onChange(String value, boolean hasHoles);
	}
	
	private boolean allowResult = true;
	
	
	private final VerticalPanel main = new VerticalPanel();
	private final FlowPanel invariant = new FlowPanel();
	
	private final PopupPanel topPop = new PopupPanel();
	private final InvRef model = new InvRef(RefType.Expression);
	
	
	private final List<ChangeListener> listeners = new LinkedList<ChangeListener>();
	
	public void setAllowResult(boolean allowResult){
		this.allowResult = allowResult;
	}
	
	public void addChangeListener(EzWrite.ChangeListener changeListener){
		listeners.add(changeListener);
	}
	
	public String getValue(){
		String value = "";
		if (model != null && model.getValue() != null){
			value = model.getValue().getValue();
		}
		return value;
	}
	
	public boolean hasHole(){
		return model.isHole() || model.getValue().hasHole();
	}
	
	private void notifyListeners(){
		String value = getValue();
		
		for (ChangeListener l : listeners){
			l.onChange(value, hasHole());
		}
		
	}
	
	
	
	HashMap<Integer,Integer> own = new HashMap<Integer,Integer>();
	HashMap<Integer,Widget> widgets = new HashMap<Integer,Widget>();
	HashMap<Widget,String> styles = new HashMap<Widget,String>();
	
	private void unhighlight(){
		for (Widget w : widgets.values()){
			w.setStylePrimaryName(styles.get(w));
		}
	}
	
	private void highlight(int owner, boolean sub){
		if (sub){
			Set<Integer> os = new HashSet<Integer>();
			
			boolean changed = false;
			os.add(owner);
			
			do{
				changed = false;
				for (int i : own.keySet()){
					int i2 = own.get(i);
					
					if (i2 != i && os.contains(i2) && !os.contains(i)){
						os.add(i);
						changed = true;
					}
				}
			}while(changed);
			
			for (int i : os){
				if (widgets.containsKey(i)){
					widgets.get(i).setStylePrimaryName("inv-highlight" );
				}
			}
		}else{
			//just highlight the one widget
			widgets.get(owner).setStylePrimaryName("inv-highlight" );
		}
	}
	
	private static MenuType menuForType(RefType t){
		switch(t){
		case Expression:
			return MenuType.Expression;
		case Id:
			return MenuType.Id;
		case Type:
			return MenuType.Type;
		default:
			throw new IllegalArgumentException();
		}
	}
	
	private void update(final InvRef e, final InvRef parent, final Integer owner){
		final int eltI = own.size();
		
		own.put(eltI, owner != null ? owner : eltI);
		
		if (e.isHole() || e.getValue() instanceof InvFixed 
				|| e.getValue() instanceof InvLocal 
				|| e.getValue() instanceof InvArg 
				|| e.getValue() instanceof InvMethod
				|| e.getValue() instanceof InvFree
		){
			Label l = null;
			String style = null;
			
			if (e.isHole()){
				l = new Label("( )");
				style = "hole";
			
				l.addMouseDownHandler(new MouseDownHandler(){
					@Override
					public void onMouseDown(MouseDownEvent event) {
						showTopPop(e, event.getClientX(), event.getClientY(), false, menuForType(e.getRefType()));
					}
				});
				
				
			}else{
				if (e.getValue() instanceof InvFixed){
					l = new Label(((InvFixed) e.getValue()).getValue());
					style = "inv-fixed";
					if (!e.getRefType().equals(RefType.BoilerPlate)){
						l.addMouseDownHandler(new MouseDownHandler(){
							@Override
							public void onMouseDown(MouseDownEvent event) {
								showTopPop(e, event.getClientX(), event.getClientY(), false,menuForType(e.getRefType()));
							}
						});
					}else if (parent != null){
						l.addMouseDownHandler(new MouseDownHandler(){
							@Override
							public void onMouseDown(MouseDownEvent event) {
								showTopPop(parent, event.getClientX(), event.getClientY(), false,menuForType(parent.getRefType()));
							}
						});
					}
					
					
					l.addMouseOverHandler(new MouseOverHandler(){
						@Override
						public void onMouseOver(MouseOverEvent event) {
							highlight(owner, true);
						}
					});
					
				}else if (e.getValue() instanceof InvLocal){
					final InvLocal loc = (InvLocal) e.getValue();
					
					final List<Opt> os = new ArrayList<Opt>();
					
					if (loc.getValue() == null){
						l = new Label("<local var>");
						style = "hole";

					}else{
						l = new Label(loc.getValue());
						
						if (!localEdges.containsKey(loc.getValue())){
							veriService.requestFields(loc.getValue(),new AsyncCallback<List<InvElement>>(){
								@Override
								public void onFailure(Throwable caught) {
									// TODO Auto-generated method stub
									
								}

								@Override
								public void onSuccess(List<InvElement> result) {
									throw new RuntimeException("not implemented anymore");
									
									/*localEdges.put(loc.getValue(), result);
									
									for (String local : result){
										os.add(new EzDefs.UnaryOpt(local, new InvLocal("this." + local, RefType.Expression), RefType.Expression));
									}*/
								}
							});
							
						}
						
						style = "inv-local";
					}
					
					if (localEdges.containsKey(loc.getValue())){
						for (String local : localEdges.get(loc.getValue())){
							os.add(new FragmentDefinitions.UnaryOpt(local, new InvLocal("this." + local, RefType.Expression), RefType.Expression));
						}
					}
					
					for (String local : locals){
						os.add(new FragmentDefinitions.UnaryOpt(local, new InvLocal("this." + local, RefType.Expression), RefType.Expression));
					}
			
					l.addMouseDownHandler(new MouseDownHandler(){
						@Override
						public void onMouseDown(MouseDownEvent event) {
							showVarPop(parent, os,event.getClientX(), event.getClientY());
						}
					});
					
					
				}else if (e.getValue() instanceof InvFree){
					final InvFree f = (InvFree) e.getValue();
					
					if (f.getValue() != null){
						l = new Label(f.getValue());
						style = "inv-free";
					}else{
						l = new Label("<any>");
						style = "hole";
					}

					l.addDoubleClickHandler(new DoubleClickHandler(){

						@Override
						public void onDoubleClick(DoubleClickEvent event) {
							showFree(new FreeEdit(){
								@Override
								public void onSave(String value) {
									e.setValue(new InvFree(value.trim().equals("") ? null : value, RefType.Expression), false);
									update();
								}

								@Override
								public void onDelete() {
									e.setValue(null, false);
									update();
								}
							}, f.getValue());
						}
					});
					

					invariant.add(l);
				}else if (e.getValue() instanceof InvArg){
					InvArg a = (InvArg) e.getValue();
					if (a.getValue() == null){
						l = new Label("<arg>");
						style = "hole";
					}else{
						l = new Label(a.getValue());
						style = "inv-arg";
					}

					final List<Opt> os = new ArrayList<Opt>();

					for (String p : params){
						os.add(new FragmentDefinitions.UnaryOpt(p, new InvArg(p, RefType.Expression), RefType.Expression));

					}
					
					
					l.addMouseDownHandler(new MouseDownHandler(){
						@Override
						public void onMouseDown(MouseDownEvent event) {
							showVarPop(parent, os,event.getClientX(), event.getClientY());
						}
					});
					
				}
			}
			
			l.setStylePrimaryName(style);
			styles.put(l, style);
			widgets.put(eltI, l);
			
			l.addMouseOverHandler(new MouseOverHandler(){
				@Override
				public void onMouseOver(MouseOverEvent event) {
					highlight(eltI,false);
				}
			});
			l.addMouseOutHandler(new MouseOutHandler(){
				@Override
				public void onMouseOut(MouseOutEvent event) {
					unhighlight();
				}
			});
			
			invariant.add(l);
		}else{
			for (InvRef s : e.getValue().getSubElements()){
				update(s, e, eltI);
			}
		}	

	}

	public void update(){
		invariant.clear();
		styles.clear();
		own.clear();
		widgets.clear();
		update(model, null, null);
		notifyListeners();
	}
	
	private interface FreeEdit{
		void onSave(String value);
		void onDelete();
	}
	
	private static void showFree(final FreeEdit fe, String old){
		final PopupPanel p  = new PopupPanel(true);
		
		Button cancel = new Button("Cancel");
		Button save = new Button("Save");
		Button del = new Button("Delete");
		
		VerticalPanel v = new VerticalPanel();
		final TextBox b = new TextBox();
		b.setText(old == null ? "" : old);
		
		v.add(new Label("Write an expression:"));
		
		v.add(b);
		HorizontalPanel h = new HorizontalPanel();
		h.add(cancel);
		h.add(del);
		h.add(save);
		v.add(h);
		
		cancel.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				p.hide();
			}	
		});
		save.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				fe.onSave(b.getText().trim());
				p.hide();
			}
		});
		del.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				fe.onDelete();
				p.hide();
			}
		});
		p.add(v);
		
		p.center();
		p.show();
	}
	
	
	
	
	
	
	public enum MenuType{
		Compare, Logic, Type, Id, Expression
	}
	

	
	private Button genSwapButton(final InvRef hole){
		Button c = new Button("Swap Expr.");
		c.setWidth("100px");
		c.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				hole.getValue().swap();
				topPop.hide();
				update();
			}
		});
		return c;
	}
	
	private Button genClearButton(final InvRef hole){
		Button c = new Button("** Clear **");
		c.setWidth("100px");
		c.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				hole.setValue(null, false);
				topPop.hide();
				update();
			}
		});
		return c;
	}
	
	private Button genMenuItem(String caption,final InvRef hole, final int left, final int top, final boolean surround, final MenuType type){
		Button c = new Button(caption);
		c.setWidth("100px");
		c.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				topPop.hide();
				showTopPop(hole, left, top, surround,type);
			}
		});
		return c;
	}
	
	public void showTopPop(final InvRef ref, final int left, final int top, final boolean surround, final MenuType type){
		topPop.hide();
		topPop.clear();
		
		VerticalPanel topMain = new VerticalPanel();
		topPop.add(topMain);
		
		Opt[] opts = new Opt[] {};
		
		switch(type){
		case Expression:
			if (!ref.isHole() && ref.getValue().isBinary() && !surround){
				//TODO: don't include swap button if right/left are both holes
				topMain.add(genSwapButton(ref));	

				topMain.add(genMenuItem("Embed", ref, left, top, true, type));	
			}
			topMain.add(genMenuItem("Logic", ref, left, top, surround, MenuType.Logic));
			topMain.add(genMenuItem("Comparators", ref, left, top, surround, MenuType.Compare));
			
			
			opts = FragmentDefinitions.EXPRESSION;
			break;
		case Compare:
			opts = FragmentDefinitions.COMPARE;
			break;
		case Logic:
			opts = FragmentDefinitions.LOGIC;
			break;
		case Type:
			opts = FragmentDefinitions.TYPE;
			break;
		case Id: 	
			opts = FragmentDefinitions.ID;
			break;
		}
		
		for (final Opt o : opts){
			if ((o.getCaption().equals("\\result") && !allowResult)){
				continue;
			}else if ((o.getElt() instanceof InvArg && params.isEmpty())){
				continue;
			}else if (surround && !o.getElt().hasHole()){
				continue;
			}
			
			Button b = new Button(o.getCaption());
			b.setWidth("100px");
			b.addClickHandler(new ClickHandler(){
				@Override
				public void onClick(ClickEvent event) {
					if (ref.needConfirmation(o.getElt())){
						topPop.hide();
						if (Window.confirm("If you continue, some information will be lost. Proceed?")){
							if (surround){
								InvElement old = ref.getValue();
								ref.setValue(o.getElt(), true);
								
								for (InvRef qq : ref.getValue().getSubElements()){
									if (qq.isHole()){
										qq.setValue(old, true);
										break;
									}
								}
								
								update();
								
							}else{
								ref.setValue(o.getElt(), true);
								update();
							}	
							
						}
					}else{
						if (surround){
							InvElement old = ref.getValue();
							ref.setValue(o.getElt(), true);
							
							for (InvRef qq : ref.getValue().getSubElements()){
								if (qq.isHole()){
									qq.setValue(old, true);
									break;
								}
							}
							topPop.hide();
							update();
							
						}else{
							ref.setValue(o.getElt(), false);	
							topPop.hide();
							update();
						}
					}
				}
			});
			topMain.add(b);
		}
		topPop.setPopupPosition(left, top);
		topPop.setAutoHideEnabled(true);
		topPop.show();	
	}
	

	public void showVarPop(final InvRef hole, List<Opt> opts, int left, int top){
		if (opts == null){
			throw new IllegalArgumentException();
		}
		if (hole == null){
			throw new IllegalArgumentException();
		}
		
		topPop.hide();
		topPop.clear();
		
		VerticalPanel topMain = new VerticalPanel();
		topPop.add(topMain);
	
		topMain.add(genClearButton(hole));		
		
		for (final Opt o : opts){
			
			Button b = new Button(o.getCaption());
			b.setWidth("100px");
			b.addClickHandler(new ClickHandler(){
				@Override
				public void onClick(ClickEvent event) {
					if (hole.needConfirmation(o.getElt())){
						topPop.hide();
						if (Window.confirm("If you continue, some information will be lost. Proceed?")){
							hole.setValue(o.getElt(), true);
							update();
						}
					}else{
						hole.setValue(o.getElt(), false);	
						topPop.hide();
						update();
					}
					
					
				}
			});
			topMain.add(b);
		}
		topPop.setPopupPosition(left, top);
		topPop.setAutoHideEnabled(true);
		topPop.show();	
	}
	

	
	public EzWrite(){
		initWidget(main);
		
		
		invariant.setWidth("325px");
		main.add(invariant);
		//main.add(buttons);
		
		update();
		
		
		/*
		veriService.requestLocals(new AsyncCallback<List<String>>(){
			@Override
			public void onFailure(Throwable caught) {
				// TODO Auto-generated method stub
				locals.clear();
			}

			@Override
			public void onSuccess(List<String> result) {
				locals.clear();
				locals.addAll(result);
			}
		});*/
		
		veriService.requestParams(new AsyncCallback<List<String>>(){

			@Override
			public void onFailure(Throwable caught) {
				// TODO Auto-generated method stub
				params.clear();
			}

			@Override
			public void onSuccess(List<String> result) {
				params.clear();
				params.addAll(result);
			}
		});
	}
}
