package com.schiller.veriasa.executejml;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.jmlspecs.checker.JmlLexer;
import org.jmlspecs.checker.JmlMLLexer;
import org.jmlspecs.checker.JmlOldExpression;
import org.jmlspecs.checker.JmlParser;
import org.jmlspecs.checker.JmlRelationalExpression;
import org.jmlspecs.checker.JmlResultExpression;
import org.jmlspecs.checker.JmlSLLexer;
import org.jmlspecs.checker.JmlSpecExpression;
import org.jmlspecs.checker.JmlSpecQuantifiedExpression;
import org.jmlspecs.checker.JmlVisitorNI;
import org.jmlspecs.checker.Main;
import org.jmlspecs.checker.TokenStreamSelector;
import org.multijava.mjc.Constants;
import org.multijava.mjc.JAddExpression;
import org.multijava.mjc.JArrayAccessExpression;
import org.multijava.mjc.JBooleanLiteral;
import org.multijava.mjc.JConditionalAndExpression;
import org.multijava.mjc.JConditionalOrExpression;
import org.multijava.mjc.JEqualityExpression;
import org.multijava.mjc.JExpression;
import org.multijava.mjc.JMinusExpression;
import org.multijava.mjc.JNameExpression;
import org.multijava.mjc.JNullLiteral;
import org.multijava.mjc.JOrdinalLiteral;
import org.multijava.mjc.JParenthesedExpression;
import org.multijava.mjc.JRealLiteral;
import org.multijava.mjc.JThisExpression;
import org.multijava.mjc.JUnaryExpression;
import org.multijava.mjc.JVariableDefinition;
import org.multijava.mjc.JavadocLexer;
import org.multijava.mjc.ParsingController;
import org.multijava.mjc.ParsingController.ConfigurationException;
import org.multijava.mjc.ParsingController.KeyException;

import antlr.RecognitionException;
import antlr.TokenStreamException;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.schiller.veriasa.web.shared.executejml.BlameRecord;
import com.schiller.veriasa.web.shared.executejml.MultiFragment;
import com.schiller.veriasa.web.shared.executejml.SingleFragment;
import com.schiller.veriasa.web.shared.executejml.ValFragment;

import daikon.PptTopLevel;
import daikon.ProglangType;
import daikon.ValueTuple;
import daikon.VarInfo;

/**
 * Evaluate a JML expression
 * @author Todd Schiller
 */
public class ExecuteJml {
	
	/**
	 * Parse a JML expression
	 * @param expr the expression
	 * @return the parsed expression
	 * @throws RecognitionException
	 * @throws TokenStreamException
	 */
	public static JmlSpecExpression tryParse(String expr) throws RecognitionException, TokenStreamException{
		Main compiler = new Main();
		
		Reader r = new StringReader(expr);
		ParsingController parsingController = new ParsingController(r, null );
		
		TokenStreamSelector lexingController = new TokenStreamSelector();
		boolean allowUniverses = false; // WMD
		JmlLexer jmlLexer = new JmlLexer( parsingController, lexingController, 
				true, true, allowUniverses, compiler );
		JavadocLexer docLexer = new JavadocLexer( parsingController );
		JmlMLLexer jmlMLLexer = new JmlMLLexer( parsingController, lexingController, 
				true, true, allowUniverses, compiler );
		JmlSLLexer jmlSLLexer = new JmlSLLexer( parsingController, lexingController, 
				true, true, allowUniverses, compiler );

		try{
			lexingController.addInputStream( jmlLexer, "jmlTop" );
			lexingController.addInputStream( jmlMLLexer, "jmlML" );
			lexingController.addInputStream( jmlSLLexer, "jmlSL" );
			lexingController.addInputStream( docLexer, "javadoc" );
			lexingController.select( "jmlTop" );
			parsingController.addInputStream( lexingController, "jml" );
			parsingController.addInputStream( docLexer, "javadoc" );
			parsingController.selectInitial( "jml" );

			final boolean ACCEPT_MULTIJAVA = true;
			final boolean ACCEPT_RELAXEDMULTIJAVA = false;
			JmlParser parser = 
				new JmlParser( compiler, 
						parsingController.initialOutputStream(),
						parsingController,
						false,
						ACCEPT_MULTIJAVA, 
						ACCEPT_RELAXEDMULTIJAVA,
						allowUniverses );
			lexingController.push( "jmlML" );
			return parser.jmlSpecExpression();	
		}catch(KeyException kex){
			throw new RuntimeException("JML parser error");
		}catch(ConfigurationException kex){
			throw new RuntimeException("JML parser error");
		}
	}
	
	public static class UnknownVariableException extends RuntimeException{
		private static final long serialVersionUID = 1L;

		public UnknownVariableException(String message) {
			super(message);
		}
	}
	
	/**
	 * Visitor for calculating the values of JML expression (sub)fragments;
	 * maintains blame information to determine the subexpression(s) that
	 * cause an expression to be false
	 * @author Todd Schiller
	 */
	public static class ExecutionVisitor extends JmlVisitorNI{
		public enum CoarseType { NONE, BOOL, REF, VAL};
		public enum Mode { PRE, POST };
		
		/**
		 * <tt>true</tt> iff the expression is in an \old region
		 */
		private boolean inOld;
		
		private final Mode mode;
		private CoarseType type;
	
		private Object value;
		public ValFragment fragment;

		private final PptTopLevel ppt;
		private final ValueTuple vt;
		
		private final HashMap<String, Integer> known;

		private final HashMap<String,Integer> blameKnown = new HashMap<String, Integer>();
		private final HashMap<JExpression, Object> blameTrue = new HashMap<JExpression, Object>();
		private final HashMap<JExpression, Object> blameFalse = new HashMap<JExpression, Object>();
		private final HashMap<JExpression, Object> interest = new HashMap<JExpression, Object>();
		
		private void bt(JExpression e, ExecutionVisitor v, boolean flip){
			blameTrue.put(e, v.value);
			blameTrue.putAll(flip ? v.blameFalse : v.blameTrue);
			interest.put(e, v.value);
			interest.putAll(v.blameFalse);
			interest.putAll(v.blameTrue);
			interest.putAll(v.interest);
		}
		
		private void bf(JExpression e, ExecutionVisitor v, boolean flip){
			blameFalse.put(e, v.value);
			blameFalse.putAll(flip ? v.blameTrue : v.blameFalse);
			interest.put(e, v.value);
			interest.putAll(v.blameFalse);
			interest.putAll(v.blameTrue);
			interest.putAll(v.interest);
		}
		
		private VarInfo findVar(String daikonName){
			return DaikonUtil.findVar(ppt, daikonName);
		}
	
		public static ExecutionVisitor exec(JmlSpecExpression expr, PptTopLevel ppt, ValueTuple vt, Mode mode){
			ExecutionVisitor ex = new ExecutionVisitor(ppt, vt, new HashMap<String,Integer>(),mode, false);
			expr.expression().accept(ex);
			assert ex.type == CoarseType.BOOL;
			return ex;
		}
		
		private ExecutionVisitor(PptTopLevel ppt,  ValueTuple vt, HashMap<String,Integer> known, Mode mode){
			this(ppt,vt, known, mode, false);
		}
		
		private ExecutionVisitor(PptTopLevel ppt,  ValueTuple vt, HashMap<String,Integer> known, Mode mode, boolean oldMode){
			this.ppt = ppt;
			this.vt = vt;
			this.mode = mode;
			this.inOld = oldMode;
			this.type = CoarseType.NONE;
			this.known = known;
		}
		
		public boolean getStatus(){
			return (Boolean) value;
		}
	
		private String cleanToString(JExpression e){
			return e.toString().replaceAll("org.*?ResultExpression.*?@\\S+", "\\\\result").replaceAll("org.*?OldExpression.*?@\\S+", "\\\\old");
		}
		
		public List<BlameRecord> getBlame(){
			List<BlameRecord> result = new ArrayList<BlameRecord>();
			
			for (String k : blameKnown.keySet()){
				result.add(new BlameRecord(k, blameKnown.get(k).toString())  );
			}
			
			HashSet<String> seen = new HashSet<String>();
			
			for (JExpression expr : interest.keySet()){
				if (expr instanceof JOrdinalLiteral || expr instanceof JRealLiteral || expr instanceof JNullLiteral
						|| expr instanceof JBooleanLiteral){
					continue;
				}else if (!seen.contains(expr.toString())){
					result.add(new BlameRecord(cleanToString(expr),interest.get(expr).toString()));
					seen.add(expr.toString());
				}
			}
			
			return result;
		}

		private static String qualifyName(JNameExpression name){
			String result = name.getName();
			
			JExpression prefix = name.getPrefix();
			while (prefix != null){
				if (prefix instanceof JThisExpression){
					result = "this." + result;
					break;
				}else if (prefix instanceof JNameExpression){
					result = ((JNameExpression) prefix).getName() + "." + result;
					prefix = ((JNameExpression) prefix).getPrefix();
				}else{
					throw new RuntimeException("Unexpected expression " + prefix);
				}
			}
			return result;
		}
				
		private boolean tryArrayLength(JNameExpression nameExpr){
			String qualifiedName = qualifyName(nameExpr);

			// check local variables first
			VarInfo info = inOld ? findVar("orig(size(" + qualifiedName + "[..]))") : findVar("size(" + qualifiedName + "[..])");

			// if information is not available as a local-variable, check for instance information
			if (info == null){
				info = inOld ? findVar("orig(size(this." + qualifiedName + "[..]))") : findVar("size(this." + qualifiedName + "[..])");
			}

			if (info == null){
				// no length information is available
				return false;
			}else{
				type = CoarseType.VAL;
				value = clean(vt.getValue(info));
				fragment = new SingleFragment(qualifiedName + ".length", extract());
				return true;
			}
		}
		
		/**
		 * Recursively flatten conjunctions
		 * @param the JML expression
		 * @return list of top-level conjuncts
		 */
		private List<JExpression> liftConjuncts(JExpression expr){
			if (expr instanceof JConditionalAndExpression){
				List<JExpression> result = Lists.newArrayList();
				result.addAll(liftConjuncts(((JConditionalAndExpression) expr).left().unParenthesize()));
				result.addAll(liftConjuncts(((JConditionalAndExpression) expr).right().unParenthesize()));
				return result;
			}else{
				return Lists.newArrayList(expr);	
			}
		}
		
		/**
		 * Try to interpret <tt>expr</tt> as an integer. Returns <tt>null</tt> iff
		 * interpretation fails
		 * @param expr the JML expression
		 * @return interpret <tt>expr</tt> as an integer
		 */
		private Integer asInt(JExpression expr){
			ExecutionVisitor viz = new ExecutionVisitor(ppt, vt,known, mode, inOld);
			expr.accept(viz);
			
			if (!viz.type.equals(CoarseType.VAL)){
				return null;
			}
	
			try{
				return Integer.parseInt(viz.value.toString());
			}catch(NumberFormatException ex){
				return null;
			}
		}
		
		/**
		 * Update the lower bound for variable <tt>v</tt>
		 * @param lbs current lower bounds
		 * @param v the variable
		 * @param i the new lower bound
		 */
		private static void updateLowerBound(HashMap<String,Integer> lbs, String v, int i){
			if (!lbs.containsKey(v)){
				lbs.put(v, i);
			}else{
				lbs.put(v, Math.max(lbs.get(v), i));
			}
		}
		
		/**
		 * Update the upper bound for variable <tt>v</tt>
		 * @param ubs current upper bounds
		 * @param v the variable
		 * @param i the new upper bound
		 */
		private static void updateUpperBound(HashMap<String,Integer> ubs, String v, int i){
			if (!ubs.containsKey(v)){
				ubs.put(v, i);
			}else{
				ubs.put(v, Math.min(ubs.get(v), i));
			}
		}
		
		/**
		 * Returns the predicate for a quantified expression (either the explicit predicate, or the
		 * hypothesis of the implication)
		 * @param expr the quantified expression
		 * @return the predicate for a quantified expression 
		 */
		private static JExpression extractPred(JmlSpecQuantifiedExpression expr){
			assert expr.isForAll();
			
			if (expr.hasPredicate()){
				return expr.predicate().specExpression().expression().unParenthesize();
			}else{
				JExpression body = expr.specExpression().expression().unParenthesize();
				if (body instanceof JmlRelationalExpression){
					JmlRelationalExpression relational = (JmlRelationalExpression) body;
					
					if (relational.isImplication()){
						return relational.left().unParenthesize();
					}else if (relational.isBackwardImplication()){
						return relational.right().unParenthesize();
					}
				}
				throw new RuntimeException("Could not determine predicate in " + body);
			}
		}
		
		@Override
		public void visitJmlSpecQuantifiedExpression(JmlSpecQuantifiedExpression ex){
			if (ex.isForAll()){
			
				//assume everything is an int and get the variable names
				List<String> ns = Lists.newArrayList();
				for (JVariableDefinition v : ex.quantifiedVarDecls()){
					ns.add(v.ident());
				}
				
				HashMap<String,Integer> lb = Maps.newHashMap();//inclusive
				HashMap<String,Integer> ub = Maps.newHashMap();//exclusive
				
				JExpression predEx = extractPred(ex);
				
				//require basic predicates
				List<JExpression> xx = liftConjuncts(predEx);
			
				for (JVariableDefinition v : ex.quantifiedVarDecls()){
					for (JExpression e : xx){
						if (e instanceof JmlRelationalExpression){
							JmlRelationalExpression re = (JmlRelationalExpression) e;

							if (re.left().unParenthesize() instanceof JNameExpression 
									&& ((JNameExpression) re.left().unParenthesize()).getName().equals(v.ident())){
								
								Integer ii = asInt(re.right().unParenthesize());
								if (ii != null){
									switch (re.oper()){
									case JmlRelationalExpression.OPE_LE:
										updateUpperBound(ub, v.ident(), ii + 1);
										break;
									case JmlRelationalExpression.OPE_LT:
										updateUpperBound(ub, v.ident(), ii);
										break;
									case JmlRelationalExpression.OPE_GT:
										updateLowerBound(lb, v.ident(), ii - 1);
										break;
									case JmlRelationalExpression.OPE_GE:
										updateLowerBound(lb, v.ident(), ii);
										break;
									default:
										continue;
									}
								}
								
							}else if(re.right().unParenthesize() instanceof JNameExpression 
									&& ((JNameExpression) re.right().unParenthesize()).getName().equals(v.ident())){
								
								Integer ii = asInt(re.left().unParenthesize());
								if (ii != null){
									switch (re.oper()){
									case JmlRelationalExpression.OPE_GE:
										updateUpperBound(ub, v.ident(), ii + 1);
										break;
									case JmlRelationalExpression.OPE_GT:
										updateUpperBound(ub, v.ident(), ii);
										break;
									case JmlRelationalExpression.OPE_LT:
										updateLowerBound(lb, v.ident(), ii - 1);
										break;
									case JmlRelationalExpression.OPE_LE:
										updateLowerBound(lb, v.ident(), ii);
										break;
									default:
										continue;
									}
								}
								
							}

						}
					}
				}
				
				for (JVariableDefinition v : ex.quantifiedVarDecls()){
					if (!lb.containsKey(v.ident())){
						throw new RuntimeException("Could not establish lower bound for " + v.ident());
					}
					if (!ub.containsKey(v.ident())){
						throw new RuntimeException("Could not establish upper bound for " + v.ident());
					}
				}
				
				JExpression s = ex.specExpression().expression();
				
				if (ns.size() == 1){
					boolean all = true;
					
					for (int i1 = lb.get(ns.get(0)); i1 < ub.get(ns.get(0)); i1++){
						HashMap<String,Integer> kk = Maps.newHashMap();
						kk.put(ns.get(0),i1);
						
						//check predicate
						ExecutionVisitor checkPred = new ExecutionVisitor(ppt, vt,kk, mode,inOld);
						predEx.accept(checkPred);
						
						if (checkPred.getStatus()){
							ExecutionVisitor ee = new ExecutionVisitor(ppt, vt,kk, mode,inOld);
							s.accept(ee);
							
							if (!ee.type.equals(CoarseType.BOOL)){
								throw new RuntimeException("Unexpected result of forall");
							}
							all = all && ee.getStatus();
							if (!all){
								
								bf(s, ee, false);
								blameKnown.put(ns.get(0),i1);
								
								if (ex.hasPredicate()){
									fragment = new MultiFragment(new ValFragment[]{
											new SingleFragment("(\\forall int i; "),
											checkPred.fragment,
											new SingleFragment(";"),
											ee.fragment,
											new SingleFragment(")"),	
										}, "false");
								}else{
									fragment = new MultiFragment(new ValFragment[]{
											new SingleFragment("(\\forall int i;"),
											ee.fragment,
											new SingleFragment(")"),	
										}, "false");
								}
								
								break;
							}
						}
					}
					
					value = all;
					type = CoarseType.BOOL;
				
					return;
				}else if (ns.size() == 2){
					throw new RuntimeException("Foralls with 2 vars not supported yet");
				}else{
					throw new RuntimeException("Foralls with more than 2 vars not supported");
				}

			}else{
				throw new RuntimeException("Quanitified expression not supported " + ex); 
			}	
		}
		
		@Override
		public void visitArrayAccessExpression(JArrayAccessExpression expr){
			assert expr.prefix() instanceof JNameExpression;
			String array = qualifyName((JNameExpression) expr.prefix());
			
			String qr = inOld ? "orig(" + array + "[..])" : array + "[..]";
			VarInfo vi = findVar(qr);
	
			//try to add this (should be safe to at least try)
			if (vi == null && !array.startsWith("this.")){
				qr = inOld ? "orig(this." + array + "[..])" :  "this." + array + "[..]";
				vi = findVar(qr);
			}
			
			//Figure out index
			ExecutionVisitor ee = new ExecutionVisitor(ppt, vt,known, mode, inOld);
			expr.accessor().accept(ee);
			assert ee.type.equals(CoarseType.VAL);
			int idx = Integer.parseInt(ee.value.toString());
			
			if (vi.type.equals(ProglangType.rep_parse("java.lang.Object[]"))){
				type = CoarseType.REF;
			}else{
				type = CoarseType.VAL;
			}
			
			value = clean(Array.get(clean(vt.getValue(vi)), idx));
			fragment = new MultiFragment(
					new ValFragment[]{
							new SingleFragment(array + "["),
							ee.fragment,
							new SingleFragment("]")
			},extract());
		}
		
		
		@Override
		public void visitNameExpression(JNameExpression expr){
			if (known.containsKey(expr.getName())){
				value = known.get(expr.getName());
				type = CoarseType.VAL;
				fragment = new SingleFragment(expr.getName(),extract()); 
				return;
			}
			
			if (expr.getName().equals("length") && expr.getPrefix() instanceof JNameExpression){
				if (tryArrayLength((JNameExpression)expr.getPrefix())){
					return;
				}
			}

			String qn = qualifyName(expr);
			
			//always try array version first (should be safe)
			String qr = inOld ? "orig(" + qn + "[..])" : qn + "[..]";
			VarInfo vi = findVar(qr);
			
			if (vi == null){
				qr = inOld ? "orig(" + qn + ")" : qn;
				vi = findVar(qr);
			}

			//try to add this (should be safe to at least try)
			if (vi == null && !qn.startsWith("this.")){
				qr = inOld ? "orig(this." + qn + "[..])" :  "this." + qn + "[..]";
				vi = findVar(qr);
			}
			
			//try to add this (should be safe to at least try)
			if (vi == null && !qn.startsWith("this.")){
				qr = inOld ? "orig(this." + qn + ")" :  "this." + qn;
				vi = findVar(qr);
			}
			
			if (vi == null){
				throw new UnknownVariableException(qr);
			}
			
			type = (vi.is_reference() || vi.is_array() || vi.type.equals(ProglangType.OBJECT)) ? CoarseType.REF : CoarseType.VAL;
			value = clean(vt.getValue(vi));
			fragment = new SingleFragment(qn, extract()); 			
		}
		
		@Override
		public void visitUnaryExpression(JUnaryExpression x){
			
			if (x.oper() == Constants.OPE_MINUS){
				// TODO make this at least somewhat safe
				
				ExecutionVisitor ee = new ExecutionVisitor(ppt, vt, known, mode,inOld);
				x.expr().accept(ee);
				
				assert ee.type.equals(CoarseType.VAL);
			
				try{
					value = -1 * Integer.parseInt(ee.value.toString());
				}catch(NumberFormatException ex){
					value = -1 * Double.parseDouble(ee.value.toString());
				}
				type = CoarseType.VAL;
				
				fragment = new MultiFragment(new ValFragment[]{
						new SingleFragment("-"),
						ee.fragment,
				}, extract());
				
			}else if(x.oper() == Constants.OPE_LNOT){
				ExecutionVisitor ee = new ExecutionVisitor(ppt, vt,known,mode,inOld);
				x.expr().accept(ee);
				
				assert ee.type.equals(CoarseType.BOOL);
				value = ! ((Boolean) ee.value);
				
				if ((Boolean) value){
					bt(x.expr(),ee,true);
				}else{
					bf(x.expr(),ee,true);
				}
				
				type = CoarseType.BOOL;
				fragment = new MultiFragment(new ValFragment[]{
						new SingleFragment("!"),
						ee.fragment,
				},extract());
			}else{
				throw new UnsupportedOperationException("Unexpected unary expression " + x);
			}	
		}
		
		public static Object clean(Object o){
			if (o instanceof Long){
				return Integer.parseInt(o.toString());
			}else{
				return o;
			}
		}
		
		public String extract(){
			if (type.equals(CoarseType.REF)){
				if (value == null || value.toString().equals("0")){
					return "null";
				}else{
					return "ref@" + value.toString();	
				}
			}else{
				return value.toString();
			}
		}
		
		@Override
		public void visitOrdinalLiteral(JOrdinalLiteral expr){
			type = CoarseType.VAL;
			value = clean(expr.getValue());
			fragment = new SingleFragment(expr.getValue().toString(),extract());
		}
		
		@Override
		public void visitRealLiteral(JRealLiteral expr){
			type = CoarseType.VAL;
			value = clean(expr.getValue());
			fragment = new SingleFragment(expr.getValue().toString(),extract());
		}
		
		@Override
		public void visitNullLiteral(JNullLiteral expr){
			type = CoarseType.REF;
			value = null;
			fragment = new SingleFragment("null", "null");
		}
		
		@Override
		public void visitBooleanLiteral(JBooleanLiteral expr){
			type = CoarseType.BOOL;
			value = expr.booleanValue();
			fragment = new SingleFragment(Boolean.toString(expr.booleanValue()),Boolean.toString(expr.booleanValue()));
		}
		
		
		@Override
		public void visitAddExpression(JAddExpression expr){
			ExecutionVisitor eLeft = new ExecutionVisitor(ppt, vt,known,mode,inOld);
			ExecutionVisitor eRight = new ExecutionVisitor(ppt, vt,known, mode,inOld);
			
			expr.left().accept(eLeft);
			expr.right().accept(eRight);
			
			assert eLeft.type == eRight.type;
			assert eLeft.type.equals(CoarseType.VAL);
			
			interest.put(expr.left(), eLeft.value);
			interest.put(expr.right(), eRight.value);
			
			try{
				value = Integer.parseInt(eLeft.value.toString()) + Integer.parseInt(eRight.value.toString());
			}catch(NumberFormatException ex){
				value = Double.parseDouble(eLeft.value.toString()) + Double.parseDouble(eRight.value.toString());
			}
			type = CoarseType.VAL;
			fragment = new MultiFragment(new ValFragment[]{
					eLeft.fragment,
					new SingleFragment(" + "),
					eRight.fragment,
				}, extract());
		}
		
		@Override
		public void visitMinusExpression(JMinusExpression expr){
			ExecutionVisitor eLeft = new ExecutionVisitor(ppt, vt,known,mode,inOld);
			ExecutionVisitor eRight = new ExecutionVisitor(ppt, vt,known, mode,inOld);
			
			expr.left().accept(eLeft);
			expr.right().accept(eRight);
			
			assert eLeft.type == eRight.type;
			assert eLeft.type.equals(CoarseType.VAL);
			
			interest.put(expr.left(), eLeft.value);
			interest.put(expr.right(), eRight.value);
			
			try{
				value = Integer.parseInt(eLeft.value.toString()) - Integer.parseInt(eRight.value.toString());
			}catch(NumberFormatException ex){
				value = Double.parseDouble(eLeft.value.toString()) - Double.parseDouble(eRight.value.toString());
			}
			type = CoarseType.VAL;
			fragment = new MultiFragment(new ValFragment[]{
					eLeft.fragment,
					new SingleFragment(" - "),
					eRight.fragment,
				}, extract());
		}
		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public void visitJmlRelationalExpression(JmlRelationalExpression expr){
			ExecutionVisitor eLeft = new ExecutionVisitor(ppt, vt,known, mode,inOld);
			ExecutionVisitor eRight = new ExecutionVisitor(ppt, vt,known, mode,inOld);
			
			expr.left().accept(eLeft);
			expr.right().accept(eRight);
			
			String sign;
			
			assert eLeft.type == eRight.type;
			
			if (expr.isImplication()){
				assert eLeft.type == CoarseType.BOOL;
				boolean lhs = (Boolean) eLeft.value;
				boolean rhs = (Boolean) eRight.value;
				value = !lhs || (lhs && rhs);
				sign = " ==> ";
			}else if (expr.isBackwardImplication()){
				assert eLeft.type == CoarseType.BOOL;
				boolean lhs = (Boolean) eLeft.value;
				boolean rhs = (Boolean) eRight.value;
				value = !rhs || (rhs && lhs);
				sign = " <== ";
			}else if (expr.isNonEquivalence()){
				assert eLeft.type == CoarseType.BOOL;		
				boolean lhs = (Boolean) eLeft.value;
				boolean rhs = (Boolean) eRight.value;
				value = (rhs != lhs);	
				sign = " <=!=> ";
			}else if(expr.isEquivalence()){
				assert eLeft.type == CoarseType.BOOL;	
				boolean lhs = (Boolean) eLeft.value;
				boolean rhs = (Boolean) eRight.value;
				value = (rhs == lhs);
				sign = " <==> ";
			}else if (expr.oper() == JmlRelationalExpression.OPE_LT){
				Comparable lhs = (Comparable) eLeft.value;
				Comparable rhs = (Comparable) eRight.value;
				
				value = lhs.compareTo(rhs) < 0;
				sign = " < ";
			}else if (expr.oper() == JmlRelationalExpression.OPE_GT){
				Comparable lhs = (Comparable) eLeft.value;
				Comparable rhs = (Comparable) eRight.value;
				
				value = lhs.compareTo(rhs) > 0;
				sign = " > ";
			}else if (expr.oper() == JmlRelationalExpression.OPE_GE){
				Comparable lhs = (Comparable) eLeft.value;
				Comparable rhs = (Comparable) eRight.value;
				
				value = lhs.compareTo(rhs) >= 0;
				sign = " >= ";
			}else if (expr.oper() == JmlRelationalExpression.OPE_LE){
				Comparable lhs = (Comparable) eLeft.value;
				Comparable rhs = (Comparable) eRight.value;
				
				value = lhs.compareTo(rhs) <= 0;
				sign = " <= ";
			}else{
				throw new RuntimeException("Unexpected relation " + expr);
			}
			
			bt(expr.left(),eLeft,false);
			bt(expr.right(),eRight,false);
			bf(expr.left(),eLeft,false);
			bf(expr.right(),eRight,false);
			
			type = CoarseType.BOOL;
			
			fragment = new MultiFragment(new ValFragment[]{
					eLeft.fragment,
					new SingleFragment(sign),
					eRight.fragment,
				}, extract());
		}
		
		@Override
		public void visitParenthesedExpression(JParenthesedExpression e){
			ExecutionVisitor mid = new ExecutionVisitor(ppt, vt,known,mode,inOld);
			e.expr().accept(mid);
			
			value = mid.value;
			type = mid.type;
			fragment = new MultiFragment(new ValFragment[]{
					new SingleFragment("("),
					mid.fragment,
					new SingleFragment(")"),
				}, extract());
		}
		
		@Override
		public void visitJmlResultExpression(JmlResultExpression result){
			assert mode != Mode.PRE;
			VarInfo vi = findVar("return");
			assert vi != null;
			
			if (vi.type.equals(ProglangType.BOOLEAN)){
				type = CoarseType.BOOL;
				
				value = ((Long)vt.getValue(vi)) == 0 ? false : true;
		
			}else{
				type = (vi.is_reference() || vi.is_array()) ? CoarseType.REF : CoarseType.VAL;
				value = clean(vt.getValue(vi));
			}
			
			fragment = new SingleFragment("\\result", extract());
		}
		
		@Override
		public void visitJmlOldExpression(JmlOldExpression old){
			ExecutionVisitor mid = new ExecutionVisitor(ppt, vt,known,mode,true);
			old.specExpression().accept(mid);
			
			value = mid.value;
			type = mid.type;
			fragment = new MultiFragment(new ValFragment[]{
					new SingleFragment("\\old("),
					mid.fragment,
					new SingleFragment(")"),
				}, extract());
		}
		
		@Override
		public void visitJmlSpecExpression(JmlSpecExpression expr){
			expr.expression().accept(this);
		}
		
		/**
		 * <tt>true</tt> iff the value is a null reference
		 * @param v the finished visitor
		 * @return <tt>true</tt> iff the value is a null reference
		 */
		private static boolean representsNull(ExecutionVisitor v){
			if (!v.type.equals(CoarseType.REF)){
				throw new IllegalArgumentException("Visitor value must have type Reference");
			}
			return v.value == null || v.value.equals(0);
		}
		
		@Override
		public void visitEqualityExpression(JEqualityExpression expr) {
			boolean flip = expr.oper() == JEqualityExpression.OPE_NE;
			
			ExecutionVisitor eLeft = new ExecutionVisitor(ppt, vt,known,mode,inOld);
			ExecutionVisitor eRight = new ExecutionVisitor(ppt, vt, known,mode,inOld);
			
			expr.left().accept(eLeft);
			expr.right().accept(eRight);
			
			if (eLeft.type != eRight.type){
				throw new RuntimeException("Eq: incompatible types " + eLeft.type + " " + eRight.type);
			}
			
			if (eLeft.type == CoarseType.REF){
				value = (representsNull(eLeft) && representsNull(eRight)) || (eLeft.value != null && eLeft.value.equals(eRight.value));
			}else{
				value = eLeft.value.equals(eRight.value);
			}
			
			if (flip){
				value = ! (Boolean)value;
			}
			
			if ((Boolean) value){
				bt(expr.left(),eLeft,false);
				bt(expr.right(),eRight,false);
			}else{
				bf(expr.left(),eLeft,false);
				bf(expr.right(),eRight,false);
			}
			
			type = CoarseType.BOOL;
			fragment = new MultiFragment(new ValFragment[]{
					eLeft.fragment,
					new SingleFragment(flip ? " != " : " == "),
					eRight.fragment,
				}, extract());
		}
		
		@Override
		public void visitConditionalAndExpression(JConditionalAndExpression expr) {
			ExecutionVisitor eLeft = new ExecutionVisitor(ppt, vt, known, mode,inOld);
			ExecutionVisitor eRight = new ExecutionVisitor(ppt, vt,known, mode,inOld);
			
			expr.left().accept(eLeft);
			expr.right().accept(eRight);
			
			assert eLeft.type == CoarseType.BOOL;
			assert eRight.type == CoarseType.BOOL;
			
			value = ((Boolean) eLeft.value) && ((Boolean) eRight.value);
			
			if ((Boolean) value){
				bt(expr.left(),eLeft,false);
				bt(expr.right(),eRight,false);
			}else{
				if (!((Boolean) eLeft.value)){
					bf(expr.left(),eLeft,false);
				}
				if (!((Boolean) eRight.value)){
					bf(expr.right(),eRight,false);
				}
			}
			
			type = CoarseType.BOOL;
			fragment = new MultiFragment(new ValFragment[]{
					eLeft.fragment,
					new SingleFragment(" && "),
					eRight.fragment,
				}, extract());
		}

		@Override
		public void visitConditionalOrExpression(JConditionalOrExpression expr) {
			ExecutionVisitor eLeft = new ExecutionVisitor(ppt, vt, known, mode, inOld);
			ExecutionVisitor eRight = new ExecutionVisitor(ppt, vt,known, mode, inOld);
			
			expr.left().accept(eLeft);
			expr.right().accept(eRight);
			
			assert eLeft.type == CoarseType.BOOL;
			assert eRight.type == CoarseType.BOOL;
			
			if ((Boolean) value){
				if (((Boolean) eLeft.value)){
					bt(expr.left(),eLeft,false);
				}
				if (((Boolean) eRight.value)){
					bt(expr.right(),eRight,false);
				}
			}else{
				bf(expr.left(),eLeft,false);
				bf(expr.right(),eRight,false);
			}
		
			value = ((Boolean) eLeft.value) || ((Boolean) eRight.value) ;
			type = CoarseType.BOOL;
			fragment = new MultiFragment(new ValFragment[]{
					eLeft.fragment,
					new SingleFragment(" || "),
					eRight.fragment,
				}, extract());
		}
	}
}
