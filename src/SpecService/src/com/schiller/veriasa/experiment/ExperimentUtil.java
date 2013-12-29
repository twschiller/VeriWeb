package com.schiller.veriasa.experiment;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.schiller.veriasa.distance.TypeDistance;
import com.schiller.veriasa.web.server.logging.LogEntry;
import com.schiller.veriasa.web.server.logging.LogFill;
import com.schiller.veriasa.web.shared.core.ProjectSpecification;

public final class ExperimentUtil {
	final static Logger log = Logger.getLogger(ExperimentUtil.class);
	
	private ExperimentUtil(){}
	
	/**
	 * Output the data to a CSV file
	 * @param file
	 * @param forge
	 * @param interpolate
	 * @throws FileNotFoundException
	 */
	public static void output(File file, DataForge<TypeDistance> forge, boolean interpolate) throws FileNotFoundException{
		FileOutputStream out = new FileOutputStream(file);
		PrintStream ps = new PrintStream(out);
		forge.csv(ps, interpolate, new Function<TypeDistance,String>(){
			@Override
			public String apply(TypeDistance arg0) {
				return arg0 == null ? "" : "" + arg0.getDistance();
			}
		});
		ps.close();
	}
	
	public static List<LogEntry> readLogs(File vlog, HashMap<LogEntry, ProjectSpecification> recorded, boolean fillKnown) 
		throws FileNotFoundException, IOException, ClassNotFoundException{
		
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(vlog));
		ArrayList<LogEntry> entries = Lists.newArrayList();
		LogEntry o;
		try{
			while ((o = (LogEntry) ois.readObject()) != null){
				if (o instanceof LogEntry){
					LogEntry e = (LogEntry) o;
					ProjectSpecification s = (ProjectSpecification) ois.readObject();
					
					if (fillKnown){
						s = LogFill.fillKnown(s,e.getContext().getProblems());
					}
				
					recorded.put(e, s);
					entries.add(e);
				}
			}	
		}catch(EOFException e){
			log.warn("Unexpected end of file");
		}
		return entries;
	}
	
	
	public static <T> T max(Iterable<T> xs, Comparator<T> cmp){
		T t = null;
		for (T x : xs){
			if (t == null || cmp.compare(t,x) < 0){
				t = x;
			}
		}
		return t;
	}
	
	public static <T extends Comparable<T>> T  max(Iterable<T> xs){
		T t = null;
		for (T x : xs){
			if (t == null || t.compareTo(x) < 0){
				t = x;
			}
		}
		return t;
	}

}
