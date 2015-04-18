package com.schiller.veriasa.experiment;

import static com.google.common.collect.Maps.newHashMap;
import static com.schiller.veriasa.experiment.DataPoint.collapseBreaks;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.schiller.veriasa.distance.Distance;
import com.schiller.veriasa.distance.Distance.DistanceType;
import com.schiller.veriasa.distance.Normalize;
import com.schiller.veriasa.distance.TypeDistance;
import com.schiller.veriasa.distance.util.Pod;
import com.schiller.veriasa.distance.util.PodUtil;
import com.schiller.veriasa.util.ParseSource;
import com.schiller.veriasa.util.ParseSource.AlsoException;
import com.schiller.veriasa.util.ParseSource.BadCnt;
import com.schiller.veriasa.web.server.ObjectInvariants;
import com.schiller.veriasa.web.server.SpecUtil;
import com.schiller.veriasa.web.server.Util;
import com.schiller.veriasa.web.server.logging.LogEntry;
import com.schiller.veriasa.web.server.logging.UserAction;
import com.schiller.veriasa.web.shared.core.MethodContract;
import com.schiller.veriasa.web.shared.core.ProjectSpecification;
import com.schiller.veriasa.web.shared.core.Clause;
import com.schiller.veriasa.web.shared.core.TypeSpecification;

public class LogDistance {
	final static Logger log =  Logger.getLogger(LogDistance.class);
	
	public final static HashMap<LogEntry, ProjectSpecification> recorded = newHashMap();
	
	public final static PodUtil.MakePod mk = new PodUtil.MakePod(recorded);
	
	public enum Experiment { VWORKER_ECLIPSE, VWORKER_VERIWEB}
	
	public static final Predicate<String> typeFilter = new Predicate<String>(){
		@Override
		public boolean apply(String x) {
			return x.contains("\\type") || x.contains(".owner") ||  x.contains("\\elemtype") ;
		}
	};
	public static final Predicate<String> trueFilter = new Predicate<String>(){
		@Override
		public boolean apply(String x) {
			return x.equals("true");
		}
	};
	
	/**
	 * Contracts to ignore; currently predicates that reduce to "true" and type predicates
	 */
	public static final Predicate<String> IGNORE = Predicates.or(trueFilter,typeFilter);
	
	/**
	 * Check that every target specification in <code>specs</code> has a zero distance to the set itself
	 * @param targets the set of targets
	 */
	private static void sanityCheck(Collection<TypeSpecification> targets){
		assert Iterables.all(targets, new Predicate<TypeSpecification>(){
			@Override
			public boolean apply(TypeSpecification spec) {
				return Distance.distance(spec, "", spec, Predicates.<String>alwaysFalse(), DistanceType.ECLIPSE, 0).getDistance() == 0;
			}
		});
	}

	public static void printSpec(TypeSpecification t){
		for (Clause s : t.getInvariants()){
			System.out.println("//@invariant " + s.getClause());
		}
		
		for (MethodContract m : t.getMethods()){
			System.out.println("=========" + m.getSignature() + "----");
			for (Clause s : m.getRequires()){
				System.out.println("//@requires " + s.getClause());
			}	
			for (Clause s : m.getEnsures()){
				System.out.println("//@ensures " + s.getClause());
			}
			if (!m.getExsures().isEmpty()){
				for (Clause s : m.getExsures().get("RuntimeException")){
					System.out.println("//@exsures (RuntimeException)" + s.getClause());
				}	
			}
		}
	}

	public static DataForge<TypeDistance> payForge = new DataForge<TypeDistance>(new Function<DataPoint<TypeDistance>,Double>(){
		@Override
		public Double apply(DataPoint<TypeDistance> x) {
			return x.getPay();
		}
	}, ExperimentOptions.WORKER_PAY);
	
	public static DataForge<TypeDistance> performExperiment(
			String type, 
			Iterable<Pod> all, 
			HashMap<String,TypeSpecification> targets,
			final ExperimentOptions opt
	){		
		DataForge<TypeDistance> forge = new DataForge<TypeDistance>(new Function<DataPoint<TypeDistance>,Double>(){
			@Override
			public Double apply(DataPoint<TypeDistance> arg0) {
				return arg0.getX() * 1.;
			}
		}, ExperimentOptions.WORKER_PAY);
		
		for (final String id : PodUtil.users(all)){	
			if (!ExperimentOptions.PAY_RATE.containsKey(id)){
				log.debug("Skipping " + id);
				continue;
			}
			
			log.debug("Analyzing " + id);
			Iterable<Pod> forUser = PodUtil.forUser(all, id);
			
			Pod lastData = Iterables.getLast(forUser);
			
			List<DataPoint<TypeDistance>> rawTimes = Distance.distance(type, forUser, targets, IGNORE, opt.metric);
			
			DataPoint<TypeDistance> lastRaw = Iterables.getLast(rawTimes);
			
			List<DataPoint<TypeDistance>> data = DataPoint.elapsed(
					opt.collapseBreaks ?
							collapseBreaks(rawTimes,opt.breakThresholdMillis,opt.measurePeriodMillis)
							: rawTimes);
			
			data = DataPoint.scaleTime(data,opt.timeScale);
			
			if (opt.payRate.containsKey(id)){
				List<DataPoint<TypeDistance>> withPay = Lists.newArrayList(Iterables.transform(data, 
						new Function<DataPoint<TypeDistance>,DataPoint<TypeDistance>>(){
							@Override
							public DataPoint<TypeDistance> apply(DataPoint<TypeDistance> x) {
								return new DataPoint<TypeDistance>(x.getX(), x.getValue(), opt.payRate.get(id) * (x.getX() / 60.));
							}
						}
				));
				payForge.add(id, withPay);
			}
	
			forge.add(id, data);
			
			// Display the last specification
			//DataPoint<TypeDistance> last = Iterables.getLast(data);
			//new DiffView(last.getValue().getActive(), last.getValue().getGoal(),last.getValue()).show();
			//System.out.println(last.getValue().getDescriptor());
			//System.out.println(last.getValue().getDistance());
			//printSpec(last.getValue().getActive());
		}
		return forge;
	}
	
	private static void doEclipse(String type, HashMap<String,TypeSpecification> targets, File out,ExperimentOptions opt) throws IOException{

	    throw new RuntimeException("Eclipse experiments not supported. Can't find the source / binaries for jml-snoop");
	    // List<Pod> ps = JmlSnoopUtil.loadData(type, opt.input);
	    // log.info("Read " + ps.size() + " vworker Eclipse entries");	
	    // DataForge<TypeDistance> forge = performExperiment(type, ps, targets,ExperimentOptions.VWORKER_ECLIPSE);
	    // ExperimentUtil.output(out, forge, opt.interpolate);
	}
	
	private static void doVeriWeb(String type, HashMap<String,TypeSpecification> targets, File out, ExperimentOptions opt) throws Exception{
		List<Pod> entries = Lists.<Pod>newArrayList(
				Iterables.transform(
						Iterables.filter(ExperimentUtil.readLogs(new File(opt.input, type + ".vlog"),recorded, true), new Predicate<LogEntry>(){
							@Override
							public boolean apply(LogEntry e) {
								return e.getAction() instanceof UserAction;
							}
						})
						, mk));
		
		log.info("Read " + entries.size() + " vworker entries");		
		DataForge<TypeDistance> forge = performExperiment(type, entries, targets,ExperimentOptions.VWORKER_VERIWEB);
		ExperimentUtil.output(out, forge,opt.interpolate);
	}
	
	public static void main(String[] args) throws Exception {
		BasicConfigurator.configure();
		Logger.getLogger(Distance.class).setLevel(Level.DEBUG);
			
		String type = args[0];
		
		File targetDir = new File(args[1], type);
		
		File outputDir = new File(args[2]);
		
		//target generation
		assert targetDir.isDirectory();
		HashMap<String, TypeSpecification> targets = generateTargets(targetDir,type);
		sanityCheck(targets.values());
		
		Experiment ex = Experiment.VWORKER_ECLIPSE;
		switch (ex){
		case VWORKER_ECLIPSE:
			doEclipse(type, targets, new File(outputDir,"vworker." + type + ".eclipse.csv"), ExperimentOptions.VWORKER_ECLIPSE);
			ExperimentUtil.output(new File(outputDir,"vworker." + type + ".eclipse.bypay.csv"), payForge, true);
			break;
		case VWORKER_VERIWEB:
			doVeriWeb(type, targets, new File(outputDir,"vworker." + type + ".veriweb.csv"), ExperimentOptions.VWORKER_VERIWEB);
			ExperimentUtil.output(new File(outputDir,"vworker." + type + ".veriweb.bypay.csv"), payForge, true);
			break;
		}
	}
	
	public static HashMap<String, TypeSpecification> generateTargets(File targetDir, String type) 
		throws IOException, AlsoException, ClassNotFoundException{
		
		HashMap<String, TypeSpecification> targets = newHashMap();
		long numTargets = 0;
		
		for (String target : targetDir.list()){
			if (target.endsWith(".java")){
				File targetFile = new File(targetDir, target);
				log.info("Reading target:" + target);
						
				BadCnt<TypeSpecification> s = ParseSource.readSpec(targetFile);
				
				if (s.bad > 0){
					throw new RuntimeException("Malformed target spec " + target);
				}
				
				targets.put(target,Normalize.normalize(s.item));
				numTargets++;
				
				int cnt = 0;
				
				for (Set<Clause> inv : Sets.powerSet(Sets.newHashSet(s.item.getInvariants()))){
					if (!Iterables.any(Iterables.transform(inv,SpecUtil.INV), IGNORE)){
						targets.put(target + " invs " + (++cnt), Normalize.normalize(ObjectInvariants.pushObjectInvariants(s.item, inv, false)));
						numTargets++;
					}
				}
					
			}else if (target.endsWith(".spec")){
				File targetFile = new File(targetDir, target);
				log.info("Reading target:" + target);
				
				ObjectInputStream ois = new ObjectInputStream(new FileInputStream(targetFile));
				ProjectSpecification s = (ProjectSpecification) ois.readObject();
				targets.put(target,Normalize.normalize(Util.filterSpecs(s.forType(type),SpecUtil.ACCEPT_GOOD)));
				numTargets++;
				ois.close();
			}
		}
		log.info("Created " + numTargets + " targets");
		return targets;
	}
	
}
