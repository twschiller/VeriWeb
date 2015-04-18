package com.schiller.veriasa.web.client.dnd;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.schiller.veriasa.web.shared.dnd.InvArg;
import com.schiller.veriasa.web.shared.dnd.InvElement;
import com.schiller.veriasa.web.shared.dnd.InvFixed;
import com.schiller.veriasa.web.shared.dnd.InvFree;
import com.schiller.veriasa.web.shared.dnd.InvLocal;
import com.schiller.veriasa.web.shared.dnd.InvMethod;
import com.schiller.veriasa.web.shared.dnd.InvRef;
import com.schiller.veriasa.web.shared.dnd.InvElement.RefType;

/**
 * Contains <i>declarative</i> definitions for fragments in the drag-and-drop interface
 * @author Todd Schiller
 */
public final class FragmentDefinitions {

	public static final String FORALL_PRED_TOOLTIP = "Predicate indicating which values to inspect";
	public static final String FORALL_EXPR_TOOLTIP = "Expression to evaluate for each value";
	
	public interface Opt{
		InvElement getElt();
		String getCaption();
	}
	
	public static class BinaryOpt implements Opt{
		private String caption;
		private InvElement middle;
		private RefType type;
		
		public BinaryOpt(String caption, InvElement middle, RefType type){
			this.caption = caption;
			this.middle = middle;
			this.type = type;
		}

		@Override
		public InvElement getElt() {
			return new InvElement(
					new InvRef[] { 
							new InvRef(RefType.Expression), 
							new InvRef(middle), 
							new InvRef(RefType.Expression),
					},
					type
					);
		}

		@Override
		public String getCaption() {
			return caption;
		}
	}
	
	public static class UnaryOpt implements Opt{
		private String caption;
		private InvElement middle;
		private RefType type;
		
		public UnaryOpt(String caption, InvElement middle, RefType type){
			this.caption = caption;
			this.middle = middle;
			this.type = type;
		}

		@Override
		public InvElement getElt() {
			return new InvElement(new InvRef[] {
					new InvRef(middle), 
					},
					type
			);
		}

		@Override
		public String getCaption() {
			return caption;
		}
	}
	
	/**
	 * \forall expression with index <code>i</code>
	 */
	public static Opt forallOpt1 = new Opt(){

		@Override
		public InvElement getElt() {
			return new InvElement(new InvRef[] { 
					new InvRef(new InvFixed("(\\forall int i;",RefType.BoilerPlate)), 
					new InvRef(RefType.Expression, FORALL_PRED_TOOLTIP),
					new InvRef(new InvFixed(";",RefType.BoilerPlate)),
					new InvRef(RefType.Expression, FORALL_EXPR_TOOLTIP),
					new InvRef(new InvFixed(")",RefType.BoilerPlate)),
			},
			RefType.Expression);
		}

		@Override
		public String getCaption() {
			throw new RuntimeException("Not expected!");
		}
		
	};
	
	/**
	 * \forall expression with index <code>j</code>
	 */
	public static Opt forallOpt2 = new Opt(){

		@Override
		public InvElement getElt() {
			return new InvElement(new InvRef[] { 
					new InvRef(new InvFixed("(\\forall int j;",RefType.BoilerPlate)), 
					new InvRef(RefType.Expression, FORALL_PRED_TOOLTIP),
					new InvRef(new InvFixed(";",RefType.BoilerPlate)),
					new InvRef(RefType.Expression, FORALL_EXPR_TOOLTIP),
					new InvRef(new InvFixed(")",RefType.BoilerPlate)),
			},
			RefType.Expression);
		}

		@Override
		public String getCaption() {
			throw new RuntimeException("Not expected!");
		}
		
	};
	
	/**
	 * \forall expression with index <code>i</code> and <code>j</code>
	 */
	public static Opt forallOpt3 = new Opt(){

		@Override
		public InvElement getElt() {
			return new InvElement(new InvRef[] { 
					new InvRef(new InvFixed("(\\forall int i, j;",RefType.BoilerPlate)), 
					new InvRef(RefType.Expression, FORALL_PRED_TOOLTIP),
					new InvRef(new InvFixed(";",RefType.BoilerPlate)),
					new InvRef(RefType.Expression, FORALL_EXPR_TOOLTIP),
					new InvRef(new InvFixed(")",RefType.BoilerPlate)),
			},
			RefType.Expression);
		}

		@Override
		public String getCaption() {
			throw new RuntimeException("Not expected!");
		}
		
	};
	
	public static Opt parenOpt = new Opt(){
		@Override
		public InvElement getElt() {
			return new InvElement(new InvRef[] { 
					new InvRef(new InvFixed("(",RefType.BoilerPlate)), 
					new InvRef(RefType.Expression), 
					new InvRef(new InvFixed(")",RefType.BoilerPlate)),
			},
			RefType.Expression);
		}
		@Override
		public String getCaption() {
			return "()";
		}
	};
	
	public static Opt oldOpt = new Opt(){
		@Override
		public InvElement getElt() {
			return new InvElement(new InvRef[] { 
					new InvRef(new InvFixed("\\old(",RefType.BoilerPlate)), 
					new InvRef(RefType.Expression, "Refers to the value of the expression at the beginning of the method"), 
					new InvRef(new InvFixed(")",RefType.BoilerPlate)),
			},
			RefType.Expression);
		}
		@Override
		public String getCaption() {
			return "\\old";
		}
	};
	
	public static Opt lNotOpt = new Opt(){
		@Override
		public InvElement getElt() {
			return new InvElement(new InvRef[] { 
					new InvRef(new InvFixed("!(",RefType.BoilerPlate)), 
					new InvRef(RefType.Expression), 
					new InvRef(new InvFixed(")",RefType.BoilerPlate)),
			},
			RefType.Expression);
		}
		@Override
		public String getCaption() {
			return "Not";
		}
	};
	
	public static Opt lNotNull = new Opt(){
		@Override
		public InvElement getElt() {
			return new InvElement(new InvRef[] { 
					new InvRef(RefType.Expression), 
					new InvRef(new InvFixed("!=",RefType.BoilerPlate)), 
					new InvRef(new InvFixed("null",RefType.BoilerPlate)),
			},
			RefType.Expression);
		}
		@Override
		public String getCaption() {
			return "NonNull";
		}
	};


	public static class FixedOpt implements Opt{
		private String caption;
		private String value;
		private RefType type;
		
		public FixedOpt(String caption, String value, RefType type){
			this.caption = caption;
			this.value = value;
			this.type = type;
		}

		@Override
		public InvElement getElt() {
			return new InvFixed(value, type);
		}

		@Override
		public String getCaption() {
			return caption;
		}
	}
	
	public static Opt constantOpt = new Opt(){

		@Override
		public InvElement getElt() {
			return new InvFree(RefType.Expression);
		}

		@Override
		public String getCaption() {
			return "* constant *";
		}
		
	};
	
	public interface FreeEdit{
		void onSave(String value);
	}
	
	public static void showFree(final FreeEdit fe, String old){
		final PopupPanel p  = new PopupPanel(true);
		
		Button cancel = new Button("Cancel");
		Button save = new Button("Save");
		
		VerticalPanel v = new VerticalPanel();
		final TextBox b = new TextBox();
		b.setText(old == null ? "" : old);
		
		v.add(new Label("Write an expression:"));
		
		v.add(b);
		HorizontalPanel h = new HorizontalPanel();
		h.add(cancel);
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
		
		p.add(v);
		
		p.center();
		p.show();
	}
	
	
	public static Opt impliesOpt = new BinaryOpt("==>", new InvFixed("==>", RefType.BoilerPlate),RefType.Expression);
	public static Opt definesOpt = new BinaryOpt("<==>", new InvFixed("<==>", RefType.BoilerPlate),RefType.Expression);
	public static Opt eqOpt = new BinaryOpt("==", new InvFixed("==", RefType.BoilerPlate),RefType.Expression);
	public static Opt neqOpt = new BinaryOpt("!=", new InvFixed("!=", RefType.BoilerPlate),RefType.Expression);
	public static Opt ltOpt = new BinaryOpt("<", new InvFixed("<", RefType.BoilerPlate),RefType.Expression);
	public static Opt gtOpt = new BinaryOpt(">", new InvFixed(">", RefType.BoilerPlate),RefType.Expression);
	public static Opt lteOpt = new BinaryOpt("<=", new InvFixed("<=", RefType.BoilerPlate),RefType.Expression);
	public static Opt gteOpt = new BinaryOpt(">=", new InvFixed(">=", RefType.BoilerPlate),RefType.Expression);
	
	public static Opt lAndOpt = new BinaryOpt("&&", new InvFixed("&&", RefType.BoilerPlate),RefType.Expression);
	public static Opt lOrOpt = new BinaryOpt("||", new InvFixed("||", RefType.BoilerPlate),RefType.Expression);
	
	public static Opt resultOpt = new FixedOpt("\\result", "\\result", RefType.Expression);
	public static Opt nullOpt = new FixedOpt("null", "null", RefType.Expression);
	
	public static Opt trueOpt = new FixedOpt("true", "true", RefType.Expression);
	public static Opt falseOpt = new FixedOpt("false", "false", RefType.Expression);
	
	public static Opt intOpt = new FixedOpt("int", "int", RefType.Type);
	public static Opt doubleOpt = new FixedOpt("double", "double", RefType.Type);
	
	public static Opt localOpt = new UnaryOpt("Member Var", new InvLocal(RefType.Expression), RefType.Expression);
	public static Opt argOpt = new UnaryOpt("Argument", new InvArg(RefType.Expression), RefType.Expression);
	//public static Opt constantOpt = new UnaryOpt("Constant", new InvFree(RefType.Expression), RefType.Expression);
	public static Opt otherOpt = new UnaryOpt("Other", new InvFree(RefType.Expression), RefType.Expression);
	public static Opt methodOpt = new UnaryOpt("Member Method", new InvMethod(RefType.Expression), RefType.Expression);

	public static Opt idIOpt = new FixedOpt("i", "i", RefType.Id);
	public static Opt idJOpt = new FixedOpt("j", "j", RefType.Id);
	public static Opt expIOpt = new FixedOpt("i", "i", RefType.Expression);
	public static Opt expJOpt = new FixedOpt("j", "j", RefType.Expression);

	public static Opt zeroOpt = new FixedOpt("0", "0", RefType.Expression);
	public static Opt oneOpt = new FixedOpt("1", "1", RefType.Expression);
	public static Opt negOpt = new FixedOpt("-1", "-1", RefType.Expression);
	public static Opt plusOpt = new BinaryOpt("+", new InvFixed("+", RefType.BoilerPlate),RefType.Expression);
	public static Opt minusOpt = new BinaryOpt("-", new InvFixed("-", RefType.BoilerPlate),RefType.Expression);
	
	
	//MENUS
	public static Opt ALL[] = new Opt[] { 
		forallOpt1,  forallOpt2,forallOpt3, definesOpt, impliesOpt, lAndOpt,lOrOpt,lNotOpt,
		neqOpt, eqOpt,  lteOpt, gteOpt,ltOpt, gtOpt,plusOpt, minusOpt,
		oldOpt,lNotNull
	};
	public static Opt COMPARE[] = new Opt[] { eqOpt, neqOpt, ltOpt, gtOpt, lteOpt, gteOpt, lNotNull };
	public static Opt LOGIC[] = new Opt[] { forallOpt1,  expIOpt, forallOpt2, expJOpt, forallOpt3, impliesOpt, definesOpt, lAndOpt,lOrOpt,lNotOpt,trueOpt, falseOpt };
	public static Opt TYPE[] = new Opt[] {intOpt, doubleOpt};
	public static Opt ID[] = new Opt[] {idIOpt, idJOpt};
	public static Opt MATH[] = new Opt[] {plusOpt, minusOpt, zeroOpt, oneOpt, negOpt};
	public static Opt SPECIAL_PRE[] = new Opt[] { nullOpt, parenOpt};
	public static Opt SPECIAL_POST[] = new Opt[] { oldOpt, resultOpt, nullOpt, parenOpt};
	public static Opt EXPRESSION[] = new Opt[] { localOpt, argOpt, constantOpt, oldOpt, resultOpt,otherOpt}; 
}
