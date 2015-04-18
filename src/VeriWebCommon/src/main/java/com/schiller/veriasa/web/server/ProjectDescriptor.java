package com.schiller.veriasa.web.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.common.base.*;
import com.google.common.collect.*;
import com.schiller.veriasa.callgraph.CallGraph;
import com.schiller.veriasa.callgraph.CallGraphNode;
import com.schiller.veriasa.executejml.CollectDataProcessor;
import com.schiller.veriasa.web.server.escj.EscJClient;
import com.schiller.veriasa.web.shared.core.DefinitionMap;
import com.schiller.veriasa.web.shared.core.MethodContract;
import com.schiller.veriasa.web.shared.core.ProjectSpecification;
import com.schiller.veriasa.web.shared.escj.ProjectResult;
import com.schiller.veriasa.web.shared.escj.VerificationRequest;
import com.schiller.veriasa.web.shared.intelli.IntelliMap;
import com.schiller.veriasa.web.shared.config.SharedConfig;

import daikon.FileIO;
import daikon.PptMap;
import daikon.PptTopLevel;
import daikon.ValueTuple;

/**
 * Eclipse project descriptor
 * @author Todd Schiller
 */
public class ProjectDescriptor implements Serializable {
	private static final long serialVersionUID = 2L;

	private String projectName;
	
	/**
	 * The spec without inferred invariants (i.e., using only specs hard-coded
	 * into the .java files) 
	 */
	private ProjectSpecification baseSpec;
	
	/**
	 * The spec with inferred invariants; specs that are hard-coded into the
	 * java files will also be used
	 */
	private ProjectSpecification inferredSpec;

	private ProjectResult baseResult;
	
	private transient DefinitionMap defs;

	private transient CallGraph callGraph;
	
	private transient IntelliMap intelli;
	
	private transient Map<PptTopLevel,List<ValueTuple>> dynamicTrace;
	
	/**
	 * Compilation Unit -> ( Line Number -> Offset)
	 */
	private Map<String,ArrayList<Integer>> lineOffsets;
	
	private List<String> order = new ArrayList<String>();
	
	private List<String> signatures = new LinkedList<String>();
	
	private ProjectDescriptor(){	
	}
	
	public static ProjectDescriptor create(
			String name,
			File workspaceDir,
			File defFile, 
			File callGraphFile, 
			File baseSpecFile, 
			File inferredSpecFile,
			File intelliFile,
			File dtraceFile) throws IOException, ClassNotFoundException{
		
		ProjectDescriptor result = new ProjectDescriptor();
	
		result.projectName = name;
		
		CallGraph g = CallGraph.readFromFile(callGraphFile,false);
		result.callGraph = CallGraph.readFromFile(callGraphFile,false);
		
		for (CallGraphNode n : g.topologicalSort()){
			result.order.add(n.getQualifiedMethodName());
		}
		
		result.baseSpec = FileUtil.<ProjectSpecification>readObject(baseSpecFile);
		
		result.lineOffsets = populateLineOffsets(workspaceDir, result.baseSpec);
				
		EscJClient client = new EscJClient(SharedConfig.ESCJ_SERVER_HOST, SharedConfig.ESCJ_SERVER_PORT);
		result.baseResult = client.tryProjectSpec(new VerificationRequest(result.baseSpec));
		
		result.signatures = new ArrayList<String>(Collections2.transform(Util.allMethods(result.baseSpec), new Function<MethodContract,String>(){
			@Override
			public String apply(MethodContract a) {
				return a.getSignature();
			}
		}));
		
		result.inferredSpec = FileUtil.<ProjectSpecification>readObject(inferredSpecFile);
		
		result.defs = FileUtil.<DefinitionMap>readObject(defFile);
		result.intelli = FileUtil.<IntelliMap>readObject(intelliFile);
		
		if (dtraceFile.exists()){
			PptMap ppts = new PptMap();
			CollectDataProcessor processor = new CollectDataProcessor();
			try {
				FileIO.read_data_trace_files (Lists.newArrayList(dtraceFile.getAbsolutePath()), ppts, processor, false);
			} catch (Exception e) {
				throw new Error(e);
			}
			result.dynamicTrace = processor.samples;
		}else{
			result.dynamicTrace = Maps.newHashMap();
		}

		return result;
	}
	
	public void hydrate(ProjectDescriptor rhs){
		this.defs = rhs.defs;
		this.callGraph = rhs.callGraph;
		this.intelli = rhs.intelli;
		this.dynamicTrace = rhs.dynamicTrace;
	}
	
	public List<String> getSignatures(){
		return Collections.unmodifiableList(signatures);
	}
	
	public String getProjectName(){
		return projectName;
	}
	
	public Map<String,ArrayList<Integer>> getLineOffsets(){
		return lineOffsets;
	}
	
	public IntelliMap getIntelliMap(){
		return intelli;
	}
	
	public DefinitionMap getDefMap(){
		return defs;
	}
	
	public ProjectSpecification getBaseSpec(){
		return baseSpec;
	}
	
	public ProjectSpecification getInferredSpec(){
		return inferredSpec;
	}
	
	public ProjectResult getBaseResult(){
		return baseResult;
	}
	
	public Map<PptTopLevel,List<ValueTuple>> getDynamicTrace(){
		return dynamicTrace;
	}
	
	public List<String> getOrder(){
		return Collections.unmodifiableList(order);
	}
	
	public CallGraph getCallGraph(){
		return callGraph;
	}

	private static HashMap<String,ArrayList<Integer>> populateLineOffsets(File workspaceDir, ProjectSpecification spec) throws IOException{
		File projDir = new File(workspaceDir, spec.getName());
		
		HashMap<String,ArrayList<Integer>> lineOffsets = Maps.newHashMap();
		
		for (String cu : spec.getCompilationUnits()){
			
			BufferedReader in = new BufferedReader(new FileReader(new File(projDir, cu)));
			
			ArrayList<Integer> offsets = new ArrayList<Integer>();
			
			int offset = 0;
			
			offsets.add(0);
			
			while (in.ready()){
				int ch = in.read();
				offset++;
				if (ch == Util.LINE_SEPARATOR.charAt(0)){
					offsets.add(offset);
				}
			}
			
			in.close();
			lineOffsets.put(cu, offsets);
		}
		
		return lineOffsets;
	}
}
