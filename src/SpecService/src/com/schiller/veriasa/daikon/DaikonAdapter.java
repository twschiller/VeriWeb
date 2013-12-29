package com.schiller.veriasa.daikon;

import static com.google.common.collect.Iterables.any;
import static com.google.common.collect.Lists.newArrayList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jmlspecs.checker.JmlSpecExpression;

import antlr.RecognitionException;
import antlr.TokenStreamException;

import com.google.common.collect.Maps;
import com.schiller.veriasa.executejml.ExecuteJml;
import com.schiller.veriasa.web.server.SpecUtil;
import com.schiller.veriasa.web.shared.core.MethodContract;
import com.schiller.veriasa.web.shared.core.MethodSpecBuilder;
import com.schiller.veriasa.web.shared.core.ProjectSpecification;
import com.schiller.veriasa.web.shared.core.Clause;
import com.schiller.veriasa.web.shared.core.TypeSpecification;
import com.schiller.veriasa.web.shared.core.TypeSpecBuilder;

import daikon.PptTopLevel;
import daikon.ValueTuple;

public class DaikonAdapter {

	private static Logger log = Logger.getLogger(DaikonAdapter.class);
	
	public final static String DAIKON_PROVENANCE = "DAIKON";
	
	public static boolean isObjHeader(String line){
		return line.endsWith(":::OBJECT");
	}
	
	public static boolean isNextSeparator(BufferedReader in) throws IOException{
		assert(in.markSupported());
		
		in.mark(1024);
		
		String line = in.readLine();
		
		in.reset();
		
		return line == null || line.startsWith("===");	
	}
	
	public static Map<String, DaikonTypeSet> parseDaikonFile(File file) throws IOException{
		HashMap<String, DaikonTypeSet> invsForType = new HashMap<String, DaikonTypeSet>();
		
		BufferedReader in
		   = new BufferedReader(new FileReader(file));
		
		String line;
		
		while ((line = in.readLine()) != null){
			
			if (line.startsWith("===")){
				line = in.readLine();
				
				if (isObjHeader(line)){
					String s[] = line.split(":::");
					String type = s[0];
					
					if (!invsForType.containsKey(type)){
						invsForType.put(type, new DaikonTypeSet(type));
					}
					
					while (!isNextSeparator(in)){
						invsForType.get(type).addObjInvariant(in.readLine().trim());
					}
					
				}else{
					String s[] = line.split(":::");
					String function = s[0];
					
				
					
					String type = s[0].substring(0,s[0].lastIndexOf('.',s[0].indexOf('(')));
					
					String point = s[1];
				
					
					if (!invsForType.containsKey(type)){
						invsForType.put(type, new DaikonTypeSet(type));
					}
					
					if (invsForType.get(type).hasFunction(function))
					{
						while (!isNextSeparator(in)){
							invsForType.get(type).addFunctionInvariant(function,point, in.readLine().trim());
						}		
					}else{
						
						DaikonMethodSet fs = new DaikonMethodSet(function);
						while (!isNextSeparator(in)){
							fs.addInvariant(point, in.readLine().trim());
						}
						invsForType.get(type).addFunctionSet(fs);
					}
				}
				
				
			}else{
				throw new IllegalArgumentException("Malformed Daikon Output");
			}
		}
		
		
		in.close();
		
		return invsForType;
	}

	private static boolean checkOne(PptTopLevel ppt, Iterable<ValueTuple> vts, String s){
		JmlSpecExpression ex;
		try {
			ex = ExecuteJml.tryParse(s);
		} catch (RecognitionException e) {
			throw new IllegalArgumentException("Malformed contract " + s);
		} catch (TokenStreamException e) {
			throw new IllegalArgumentException("Malformed contract " + s);
		}

		for (ValueTuple vt : vts){
			ExecuteJml.ExecutionVisitor v = ExecuteJml.ExecutionVisitor.exec(ex, ppt, vt, ExecuteJml.ExecutionVisitor.Mode.PRE);
			if (!v.getStatus()){
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Add a specification to a collection iff it is not already in the collection
	 * @param base
	 * @param toAdd
	 */
	private static void extend(Collection<Clause> base, Clause toAdd){
		if (!any(base, SpecUtil.invEq(toAdd))){
			base.add(toAdd);
		}
	}
	
	private static void extend(Collection<Clause> base, Collection<Clause> toAdd){		
		if (base == null){
			throw new NullPointerException();
		}
		
		for (Clause statement : toAdd){
			extend(base, statement);
		}
	}
	
	/**
	 * Augment a base specification with a Daikon inferred specification
	 * @param inferredByType Daikon inferred specification
	 * @param project base project spec
	 * @param trace dynamic trace for filtering out bad preconditions
	 * @return populated project spec
	 */
	public static ProjectSpecification populateProject(
			Map<String, DaikonTypeSet> inferredByType,
			ProjectSpecification project,
			Map<PptTopLevel,List<ValueTuple>> trace){
		
		List<TypeSpecification> newTypes = newArrayList();
		
		for (TypeSpecification type : project.getTypeSpecs()){
			
			if (inferredByType.containsKey(type.getFullyQualifiedName())){
				
				DaikonTypeSet inferredType = inferredByType.get(type.getFullyQualifiedName());
				
				// The new invariants
				List<Clause> newObjectInvariants = newArrayList(type.getInvariants());
				for (String objectInvariant : inferredType.getInvariants()){
					newObjectInvariants.add(new Clause(objectInvariant, DAIKON_PROVENANCE));
				}
				
				List<MethodContract> newMethods = newArrayList();
				
				for (MethodContract method : type.getMethods()){
					DaikonMethodSet inferredMethod = inferredType.getInvsByFunction().get(method.getSignature());
					
					if (inferredMethod == null){
						newMethods.add(method);
						continue;
					}
					
					List<Clause> modifies = newArrayList(method.getModifies());
					List<Clause> requires = newArrayList(method.getRequires());
					List<Clause> ensures = newArrayList(method.getEnsures());
					Map<String,List<Clause>> exsures = Maps.newHashMap(method.getExsures());
					
					if (!method.getSignature().contains("." + type.getFullyQualifiedName() + "(")){
						// not a constructor
						extend(requires, newObjectInvariants);
					}
					
					extend(ensures, newObjectInvariants);
					
					for (String exception : exsures.keySet()){
						extend(exsures.get(exception), newObjectInvariants);
					}
					
					for (String point : inferredMethod.getInvsAtPoint().keySet()){
						if (point.equals("ENTER")){
							for (String inferred : inferredMethod.getInvsAtPoint().get(point)){
								//check to make sure specification holds in trace
								boolean ok = true;
								
								for (PptTopLevel ppt : trace.keySet()) {
									if (ppt.name.startsWith(method.getSignature())){
										try{
											ok = checkOne(ppt,trace.get(ppt), inferred + ";");
										}catch(Exception e){
											log.warn("Error executing statement " + inferred,e);
											ok = true;
										}
										if (!ok){
											log.info("Skipping inferred statement " + inferred);
											ok = false;
											break;
										}
									}
								}
								
								if (ok){
									extend(requires,new Clause(inferred, DAIKON_PROVENANCE));
								}
							}
							
						}else if(point.startsWith("EXIT")){
							for (String i : inferredMethod.getInvsAtPoint().get(point)){
								if (i.startsWith("Modified variables:")){
									String [] ms = i.substring(i.indexOf(':') + 1).trim().split(" ");
									for (String m : ms){
										modifies.add(new Clause(m,DAIKON_PROVENANCE));
									}
								}else{
									extend(ensures,new Clause(i,DAIKON_PROVENANCE));
								}
							}
						}else{
							throw new RuntimeException("Unexpected function point " + point);	
						}
					}	
					newMethods.add(MethodSpecBuilder.builder(method)
							.setModifies(modifies)
							.setRequires(requires)
							.setEnsures(ensures)
							.setExsures(exsures)
							.getSpec());
				}
				
				newTypes.add(TypeSpecBuilder.builder(type)
						.setInvariants(newObjectInvariants)
						.setMethods(newMethods)
						.getType());
			}else{
				newTypes.add(type);
			}
		}
		
		return new ProjectSpecification(project.getName(), newTypes);
	}
	
	
}
