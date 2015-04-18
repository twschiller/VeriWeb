package com.schiller.veriasa.daikon;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Map;

import com.schiller.veriasa.executejml.CollectDataProcessor;
import com.schiller.veriasa.web.shared.core.ProjectSpecification;

import daikon.FileIO;
import daikon.PptMap;

public class Populate {

	private static ProjectSpecification readSpec(File file) throws IOException, ClassNotFoundException{
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
		ProjectSpecification project = (ProjectSpecification) ois.readObject();
		ois.close();
		return project;	
	}
	
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		if (args.length < 4){
			System.err.println("usage: PopulateSpec original invariant_file dynamic_trace outfile");
			System.exit(1);
		}
		
		File specFile = new File(args[0]);
		File invFile = new File(args[1]);
		File dynFile = new File(args[2]);
		File outFile = new File(args[3]);

		ProjectSpecification original = readSpec(specFile);
		
		Map<String, DaikonTypeSet> map = 
			DaikonAdapter.parseDaikonFile(invFile);
		
		CollectDataProcessor processor = new CollectDataProcessor();
		PptMap ppts = new PptMap();
		FileIO.read_data_trace_files (Arrays.asList(dynFile.getAbsolutePath()), ppts, processor, false);
		
		ProjectSpecification populated = DaikonAdapter.populateProject(map, original,processor.samples);
		
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(outFile));
		oos.writeObject(populated);
		oos.close();
	}

}
