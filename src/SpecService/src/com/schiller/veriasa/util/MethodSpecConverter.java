package com.schiller.veriasa.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jmlspecs.checker.JmlAssignableClause;
import org.jmlspecs.checker.JmlConstructorDeclaration;
import org.jmlspecs.checker.JmlEnsuresClause;
import org.jmlspecs.checker.JmlGeneralSpecCase;
import org.jmlspecs.checker.JmlGenericSpecBody;
import org.jmlspecs.checker.JmlGenericSpecCase;
import org.jmlspecs.checker.JmlMethodDeclaration;
import org.jmlspecs.checker.JmlRequiresClause;
import org.jmlspecs.checker.JmlSignalsClause;
import org.jmlspecs.checker.JmlSpecBodyClause;
import org.jmlspecs.checker.JmlSpecCase;
import org.jmlspecs.checker.JmlSpecification;
import org.jmlspecs.checker.JmlVisitorNI;
import org.multijava.mjc.JFormalParameter;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import com.schiller.veriasa.web.shared.core.MethodContract;
import com.schiller.veriasa.web.shared.core.Clause;

/**
 * Convert parsed JML method specifications to VeriWeb method specifications
 * @author Todd Schiller
 */
public class MethodSpecConverter extends JmlVisitorNI{
	
	private final List<String> requires = Lists.newArrayList();
	private final List<String> ensures = Lists.newArrayList();
	private final Map<String,List<String>> exsures = Maps.newHashMap();
	
	
	/**
	 * Convert parsed JML method specification to VeriWeb method specification
	 * @param qualifiedType qualified type containing the method specification
	 * @param method the JML method declaration
	 * @return the corresponding VeriWeb method specification
	 */
	public static MethodContract generate(String qualifiedType, JmlMethodDeclaration method){
		MethodSpecConverter converter = new MethodSpecConverter();
		method.accept(converter);
		
		HashMap<String,List<Clause>> exsures = Maps.newHashMap();

		if (converter.exsures != null && !converter.exsures.isEmpty() && converter.exsures.get("RuntimeException") != null ){
			exsures.put("RuntimeException", Convert.convert(converter.exsures.get("RuntimeException")));
		}

		String ps = Joiner.on(",").join(
				Iterables.transform(Lists.newArrayList(method.parameters()), new Function<JFormalParameter,String>(){
					@Override
					public String apply(JFormalParameter param) {
						return param.getType().toString().replace("Object", "java.lang.Object");
					}
				}));
		String signature = qualifiedType + "." + method.ident() + "(" + ps + ")";
		
		return new MethodContract(signature, new ArrayList<String>(), true, null,
				new ArrayList<Clause>(), 
				Convert.convert(converter.requires),
				Convert.convert(converter.ensures),
				exsures);
	}
	

	@Override
	public void visitJmlMethodDeclaration(JmlMethodDeclaration method) {
		if (method.hasSpecification()){
			method.methodSpecification().accept(this);
		}	
	}

	@Override
	public void visitJmlSpecification(JmlSpecification specification) {
		for (JmlSpecCase c : specification.specCases()){
			c.accept(this);
		}
	}

	@Override
	public void visitJmlGenericSpecCase(JmlGenericSpecCase specCase) {
		if (specCase.hasSpecHeader()){
			for (JmlRequiresClause r : specCase.specHeader()){
				requires.add(Convert.exprToString(r.predOrNot()));
			}
		}
		
		if (specCase.hasSpecBody()){
			specCase.genericSpecBody().accept(this);
		}
	}

	@Override
	public void visitJmlGenericSpecBody(JmlGenericSpecBody body) {
		if(body.isSpecCases()){
			for (JmlGeneralSpecCase c : body.specCases()){
				c.accept(this);
			}
		}else if (body.isSpecClauses()){
			for (JmlSpecBodyClause c : body.specClauses()){
				c.accept(this);
			}
		}else{
			throw new RuntimeException();
		}
	
	}

	@Override
	public void visitJmlEnsuresClause(JmlEnsuresClause ensuresClause) {
		ensures.add(Convert.exprToString(ensuresClause.predOrNot()));
	}

	@Override
	public void visitJmlSignalsClause(JmlSignalsClause signalsClause) {
		String exceptionType = signalsClause.type().toString();
		
		if (!exsures.containsKey(exceptionType)){
			exsures.put(exceptionType, Lists.<String>newArrayList());
		}
		exsures.get(exceptionType).add(Convert.exprToString(signalsClause.predOrNot()));
	}

	@Override
	public void visitJmlConstructorDeclaration(JmlConstructorDeclaration ctor) {
		ctor.methodSpecification().accept(this);
	}

	@Override
	public void visitJmlAssignableClause(JmlAssignableClause assignableClause) {
		//TODO: should do something with assignable clauses
	}
}