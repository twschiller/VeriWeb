package com.schiller.veriasa.web.server.slicing;

import org.jmlspecs.checker.JmlSpecExpression;

import antlr.RecognitionException;
import antlr.TokenStreamException;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.schiller.veriasa.executejml.DaikonUtil;
import com.schiller.veriasa.executejml.ExecuteJml;
import com.schiller.veriasa.executejml.ExecuteJml.ExecutionVisitor;
import com.schiller.veriasa.web.server.ProjectDescriptor;
import com.schiller.veriasa.web.server.SpecUtil;
import com.schiller.veriasa.web.shared.core.HasQualifiedSignature;
import com.schiller.veriasa.web.shared.feedback.DynamicFeedback;

import daikon.PptTopLevel;
import daikon.ValueTuple;

/**
 * Utility methods for dynamic feedback (slicing)
 * @author Todd Schiller
 */
public class DynamicFeedbackUtil {

	/**
	 * Filter over program points
	 * @author Todd Schiller
	 */
	public static class PptPredicate implements Predicate<PptTopLevel>{
		private final String qualifiedSignature;
		private final boolean methodEntrance;
		
		public PptPredicate(HasQualifiedSignature qualifiedSignature, boolean methodEntrace){
			this(qualifiedSignature.qualifiedSignature(), methodEntrace);
		}
		
		public PptPredicate(String qualifiedSignature, boolean methodEntrace){
			this.qualifiedSignature = qualifiedSignature;
			this.methodEntrance = methodEntrace;
		}
		
		@Override
		public boolean apply(PptTopLevel ppt) {
			String xx[] = ppt.name().split(":::");
			return xx[0].equals(qualifiedSignature) && ((!methodEntrance && !xx[1].startsWith("ENTER")) || methodEntrance);
		}
	}
	
	public static DynamicFeedback executeJmlPrecondition(String statement, HasQualifiedSignature method, ProjectDescriptor project) 
			throws RecognitionException, TokenStreamException{
		
		JmlSpecExpression expr = ExecuteJml.tryParse(SpecUtil.clean(statement));
		
		for (PptTopLevel ppt : Iterables.filter(project.getDynamicTrace().keySet(), new PptPredicate(method, true))){
			for(ValueTuple vt : project.getDynamicTrace().get(ppt)){
				ExecutionVisitor v = ExecuteJml.ExecutionVisitor.exec(expr, ppt, vt, ExecuteJml.ExecutionVisitor.Mode.PRE);
				if (!v.getStatus()){
					return new DynamicFeedback(DaikonUtil.buildVarTree(ppt, vt), v.fragment);
				}
			}
		}
		return null;
	}
	
	public static DynamicFeedback executeJmlPostcondition(String statement, HasQualifiedSignature method, ProjectDescriptor project) 
			throws RecognitionException, TokenStreamException{
		
		JmlSpecExpression expr = ExecuteJml.tryParse(SpecUtil.clean(statement));
		
		for (PptTopLevel ppt : Iterables.filter(project.getDynamicTrace().keySet(), new PptPredicate(method, false))){
			for(ValueTuple vt : project.getDynamicTrace().get(ppt)){
				ExecutionVisitor v = ExecuteJml.ExecutionVisitor.exec(expr, ppt, vt, ExecuteJml.ExecutionVisitor.Mode.POST);
				if (!v.getStatus()){
					return new DynamicFeedback(DaikonUtil.buildVarTree(ppt, vt), v.fragment);
				}
			}
		}
		return null;
	}
}
