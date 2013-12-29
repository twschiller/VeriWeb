package com.schiller.veriasa.web.server.logging;

import java.util.HashMap;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.schiller.veriasa.web.server.SpecUtil;
import com.schiller.veriasa.web.server.callgraph.ProblemGraph;
import com.schiller.veriasa.web.server.callgraph.ProblemGraph.ProblemGraphNode;
import com.schiller.veriasa.web.shared.core.MethodContract;
import com.schiller.veriasa.web.shared.core.MethodSpecBuilder;
import com.schiller.veriasa.web.shared.core.ProjectSpecification;
import com.schiller.veriasa.web.shared.core.Clause;
import com.schiller.veriasa.web.shared.core.Clause.Status;
import com.schiller.veriasa.web.shared.core.TypeSpecification;
import com.schiller.veriasa.web.shared.core.TypeSpecBuilder;
import com.schiller.veriasa.web.shared.problems.WriteEnsuresProblem;
import com.schiller.veriasa.web.shared.problems.WriteExsuresProblem;

public abstract class LogFill {

	/**
	 * Add known specifications to <code>type</code> as {@link Status#PENDING}
	 * @param type the type specification
	 * @param problems problems with known specifications
	 * @return the type specification with known specifications added
	 */
	public static TypeSpecification fillKnown(TypeSpecification type, ProblemGraph problems){
		List<MethodContract> newMethods = Lists.newArrayList();
		
		for (MethodContract method : type.getMethods()){
			ProblemGraphNode ensuresProblems = problems.find(WriteEnsuresProblem.class, method.getSignature());
			ProblemGraphNode exsuresProblems = problems.find(WriteExsuresProblem.class, method.getSignature());
			
			MethodSpecBuilder builder = MethodSpecBuilder.builder(method);
			
			List<Clause> ensures = SpecUtil.extend(
					method.getEnsures(),
					Lists.transform(((WriteEnsuresProblem) ensuresProblems.getProblem()).getKnown(), new SpecUtil.ChangeStatus(Status.KNOWN_GOOD)));
					
			builder.setEnsures(ensures);
			
			if (exsuresProblems != null){
				HashMap<String,List<Clause>> allExsures = Maps.newHashMap(method.getExsures());
				
				List<Clause> exsures = SpecUtil.extend(
						allExsures.get("RuntimeException"), 
						Lists.transform(((WriteExsuresProblem) exsuresProblems.getProblem()).getKnown(),  new SpecUtil.ChangeStatus(Status.PENDING)));
				
				allExsures.put("RuntimeException", exsures);
				builder.setExsures(allExsures);
			}
			newMethods.add(builder.getSpec());
		}
	
		return TypeSpecBuilder.builder(type).setMethods(newMethods).getType();
	}
	
	/**
	 * Add known specifications to <code>type</code> as {@link Status#PENDING}
	 * @param type the project specification
	 * @param problems problems with known specifications
	 * @return the problem specification with known specifications added
	 */
	public static ProjectSpecification fillKnown(ProjectSpecification project, final ProblemGraph problems){
		return new ProjectSpecification(project.getName(),
				Lists.<TypeSpecification>newArrayList(Collections2.<TypeSpecification,TypeSpecification>transform(project.getTypeSpecs(), new Function<TypeSpecification,TypeSpecification>(){
					@Override
					public TypeSpecification apply(TypeSpecification type) {
						return fillKnown(type, problems);
					}
				})));
	
	}
}
