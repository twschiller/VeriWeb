package com.schiller.veriasa.experiment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.schiller.veriasa.distance.Normalize;
import com.schiller.veriasa.distance.util.JmlSnoopUtil;
import com.schiller.veriasa.distance.util.Pod;
import com.schiller.veriasa.distance.util.PodUtil;
import com.schiller.veriasa.executejml.ExecuteJml;
import com.schiller.veriasa.executejml.ExecuteJml.ExecutionVisitor.Mode;
import com.schiller.veriasa.util.ParseSource;
import com.schiller.veriasa.web.server.ProjectDescriptor;
import com.schiller.veriasa.web.server.logging.LogEntry;
import com.schiller.veriasa.web.server.logging.TrySpecsAction;
import com.schiller.veriasa.web.server.logging.UserAction;
import com.schiller.veriasa.web.server.slicing.DynamicFeedbackUtil;
import com.schiller.veriasa.web.shared.core.MethodContract;
import com.schiller.veriasa.web.shared.core.ProjectSpecification;
import com.schiller.veriasa.web.shared.core.Clause;
import com.schiller.veriasa.web.shared.core.TypeSpecification;
import com.schiller.veriasa.web.shared.feedback.DynamicFeedback;
import com.schiller.veriasa.web.shared.problems.MethodProblem;
import com.schiller.veriasa.web.shared.problems.WriteEnsuresProblem;
import com.schiller.veriasa.web.shared.problems.WriteRequiresProblem;

/**
 * Identify number of times the user entered an invalid contract
 * @author Todd Schiller
 */
public class IdentifyViolations {
	private static File WORKSPACE_DIR = new File(System.getProperty("user.home"),"/asa/projs");
	private final static String CALL_GRAPH_SUFFIX = ".graph";
	private final static String BASE_SUFFIX =  ".asa";
	private final static String INFERRED_SUFFIX = ".inf.asa";
	private final static String DEF_SUFFIX = ".def";
	private final static String DTRACE_SUFFIX = ".dtrace";
	private final static String INTELLI_SUFFIX = ".intelli";

	private final static HashMap<String, ProjectDescriptor> desc = Maps.newHashMap();

	private final static HashMap<String, HashSet<ViolationKey>> cache = Maps.newHashMap();
	
	private final static HashMap<String, ProjectSpecification> baseSpec = Maps.newHashMap();
	private final static HashMap<String, Set<ViolationKey>> baseViolated = Maps.newHashMap();

	
	public final static class ViolationKey{
		private final String method;
		private final ExecuteJml.ExecutionVisitor.Mode mode;
		private final String condition;
		
		public ViolationKey(String method, Mode mode, String condition) {
			super();
			this.method = method;
			this.mode = mode;
			this.condition = condition;
		}
	
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((condition == null) ? 0 : condition.hashCode());
			result = prime * result
					+ ((method == null) ? 0 : method.hashCode());
			result = prime * result + ((mode == null) ? 0 : mode.hashCode());
			return result;
		}
	
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ViolationKey other = (ViolationKey) obj;
			if (condition == null) {
				if (other.condition != null)
					return false;
			} else if (!condition.equals(other.condition))
				return false;
			if (method == null) {
				if (other.method != null)
					return false;
			} else if (!method.equals(other.method))
				return false;
			if (mode != other.mode)
				return false;
			return true;
		}
		
		@Override
		public String toString() {
			return "ViolationKey [method=" + method + ", mode=" + mode
					+ ", condition=" + condition + "]";
		}
	}
	
	
	public static Set<ViolationKey> calculateViolations(ProjectSpecification proj){
		HashSet<ViolationKey> projCache = cache.get(proj.getName());
		
		Set<ViolationKey> vs = Sets.newHashSet();
		for (TypeSpecification t : proj.getTypeSpecs()){
			for (MethodContract m : t.getMethods()){
				
				for (Clause r : m.getRequires()){
	
					ViolationKey k = new ViolationKey(m.getSignature(), Mode.PRE, Normalize.normalize(r.getClause()));
					
					if (projCache.contains(k)){
						vs.add(k);
					}else{
						DynamicFeedback res = null;
						try {
							res = DynamicFeedbackUtil.executeJmlPrecondition(r.getClause(), m, desc.get(proj.getName()));
						} catch (Exception e){
							//oh well
						}
						
						if (res != null){
							projCache.add(k);
							vs.add(k);
						}
					}
				
				}
				for (Clause e : m.getEnsures()){
					ViolationKey k = new ViolationKey(m.getSignature(), Mode.POST,Normalize.normalize(e.getClause()));
					if (projCache.contains(k)){
						vs.add(k);
					}else{
						DynamicFeedback res = null;
						
						try {
							res = DynamicFeedbackUtil.executeJmlPostcondition(e.getClause(), m, desc.get(proj.getName()));
						}catch (Exception e1){
							//oh well
						}
						
						if (res != null){
							vs.add(k);
							projCache.add(k);
						}
					}
				}
			}
		}
		return vs;
	}
	
	public static HashMap<ViolationKey, List<Pod>> calculateViolations(Iterable<Pod> forUser){
		HashMap<ViolationKey, List<Pod>> vv = Maps.newHashMap();
		for (Pod pod : forUser){
			try{
				
				for (ViolationKey k : calculateViolations(pod.getSpec()) ){
					if (!vv.containsKey(k)){
						vv.put(k, new ArrayList<Pod>());
					}
					vv.get(k).add(pod);
				}
				
			}catch(Exception ex){
				System.err.println("Skipping datapoint " + ex.getMessage());
			}
			
		
		}
		System.out.println("Tested " + Iterables.size(forUser) + " pods, found " + vv.size() + " false conditions");
		
		return vv;
	}
	
	
	
	public static void countAttemptedViolations(ProjectSpecification proj, File file) throws FileNotFoundException, IOException, ClassNotFoundException{
		HashMap<String,Set<ViolationKey>> forUser = Maps.newHashMap();
		
		List<LogEntry> entries = Lists.newArrayList(Iterables.filter(ExperimentUtil.readLogs(file,LogDistance.recorded, true), new Predicate<LogEntry>(){
			@Override
			public boolean apply(LogEntry e) {
				return e.getAction() instanceof UserAction;
			}
		}));
		HashSet<ViolationKey> projCache = cache.get(proj.getName());
		for (LogEntry e : entries){
			if (e.getAction() instanceof TrySpecsAction){
				TrySpecsAction ta = (TrySpecsAction ) e.getAction();
				String user = 	ta.getUser().getWebId();
				
				if (!forUser.containsKey(user)){
					forUser.put(user,new HashSet<ViolationKey>());
				}
			
				MethodContract m = ((MethodProblem)ta.getProblem()).getFunction();
				
				if (ta.getProblem() instanceof WriteRequiresProblem){
					for (Clause s : ta.getSpecs()){
						ViolationKey k = null;
						try{
							k = new ViolationKey(m.getSignature(), Mode.PRE,Normalize.normalize(s.getClause()));	
						}catch(Exception foo){
							System.err.println(user + " bad " + s.getClause());
							continue;
						}
						
						if (projCache.contains(k)){
							forUser.get(user).add(k);
						}else{
							DynamicFeedback res = null;
							try {
								res = DynamicFeedbackUtil.executeJmlPrecondition(s.getClause(), m, desc.get(proj.getName()));
							} catch (Exception e1){
								//oh well
							}
							
							if (res != null){
								forUser.get(user).add(k);
								projCache.add(k);
							}
						}
					}
				}else if (ta.getProblem() instanceof WriteEnsuresProblem){
					for (Clause s : ta.getSpecs()){
						ViolationKey k = null;
						try{
							k = new ViolationKey(m.getSignature(), Mode.POST,Normalize.normalize(s.getClause()));	
						}catch(Exception foo){
							System.err.println(user + " bad " + s.getClause());
							continue;
						}
						
						if (projCache.contains(k)){
							forUser.get(user).add(k);
						}else{
							DynamicFeedback res = null;
							try {
								res = DynamicFeedbackUtil.executeJmlPostcondition(s.getClause(), m, desc.get(proj.getName()));
							} catch (Exception e1){
								//oh well
							}
							
							if (res != null){
								forUser.get(user).add(k);
								projCache.add(k);
							}
						}
					}
				}
			}
		}
		
		for (String user : forUser.keySet()){
			System.out.println(user + " has " + forUser.get(user).size() + " violations");
			
			for (ViolationKey v : forUser.get(user)){
				if (!baseViolated.get("StackAr").contains(v)){
					System.out.println(v);
				}	
			}	
		}
	}

	public static void loadProject(String name) throws Exception{
		File projDir = new File(WORKSPACE_DIR, name);
		File projMetaDir = new File(projDir, "veriasa");
		cache.put(name, new HashSet<ViolationKey>());
		ProjectDescriptor d = ProjectDescriptor.create(
				name,
				WORKSPACE_DIR,
				new File(projMetaDir, name + DEF_SUFFIX), 
				new File(projMetaDir, name + CALL_GRAPH_SUFFIX), 
				new File(projMetaDir, name + BASE_SUFFIX), 
				new File(projMetaDir, name + INFERRED_SUFFIX),
				new File(projMetaDir, name + INTELLI_SUFFIX),
				new File(projMetaDir, name + DTRACE_SUFFIX)
		);
		
		File base = new File("/home/tws/projects/asa/veriweb-paper/study/results/vworker/base-spec", name + ".java");
	
		TypeSpecification t = ParseSource.readSpec(base).item;	
		
		baseSpec.put(name, new ProjectSpecification(name, Lists.newArrayList(t)));
		
		desc.put(name, d);
		
		Set<ViolationKey> orig =  calculateViolations(baseSpec.get(name));
		
		System.out.println("Provided spec contains " + orig.size() + " false conditions");
		for (ViolationKey k : orig){
			System.out.println(k);
		}
		baseViolated.put(name,orig);
		
	}
	
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
		for (String name : Lists.newArrayList("StackAr")){
			loadProject(name);
		}
		
		String proj = "StackAr";
		
		
		//File vlog = new File("/home/tws/results/StackAr.20110331.vlog");
		//countAttemptedViolations(desc.get(proj).getBaseSpec(),vlog);
		//System.exit(0);
		
		List<Pod> ps = JmlSnoopUtil.loadData("StackAr", Common.VWORKER_ECLIPSE_RESULTS);
		
		for (String user : PodUtil.users(ps)){
			
			HashMap<ViolationKey, List<Pod>> vs = calculateViolations(PodUtil.forUser(ps, user));
			
			System.out.println(user + " has " + vs.size() + " violations");
			
			int pre = 0;
			int post = 0;
			
			for (ViolationKey v : vs.keySet()){
				
				
				if (!baseViolated.get(proj).contains(v)){
					System.out.println(v);
				
					if (v.mode ==  ExecuteJml.ExecutionVisitor.Mode.PRE){
						pre++;
					}
					if (v.mode ==  ExecuteJml.ExecutionVisitor.Mode.POST){
						post++;
					}
				}	
				
			}	
			
			System.out.println("PRE: " + pre + " POST: " + post);
			
		}	
	}
}
