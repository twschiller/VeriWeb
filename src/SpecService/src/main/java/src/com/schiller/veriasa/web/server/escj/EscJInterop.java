package com.schiller.veriasa.web.server.escj;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.*;

import com.google.common.base.Predicate;
import com.google.common.io.Files;
import com.schiller.veriasa.web.shared.core.ProjectSpecification;
import com.schiller.veriasa.web.shared.core.Clause;
import com.schiller.veriasa.web.shared.escj.AnnotatedFile;
import com.schiller.veriasa.web.shared.escj.ProjectResult;

/**
 * Methods for interacting with ESC/Java2 via the command line
 * @author Todd Schiller
 */
public class EscJInterop {
	public static String LINE_SEPARATOR = System.getProperty("line.separator");
	
	public static class EscjOptions{
		private String esctools;
		private String simplify;
		private String jdk;
		private Set<String> observed;
		private File logDir;
				
		public EscjOptions(String esctools, String simplify, String jdk, File logDir, Set<String> observed) {
			super();
			this.esctools = esctools;
			this.simplify = simplify;
			this.jdk = jdk;
			this.logDir = logDir;
			this.observed = observed;
		}
		public File getLogDir(){
			return logDir;
		}

		public String getEscjDir(){
			return esctools;
		}
		
		public Set<String> getObserved(){
			return Collections.unmodifiableSet(observed);
		}
		
		public String[] getEnvP(){
			return new String[]{
					"ESCTOOLS_RELEASE=" + esctools,
					"ESCTOOLS_ROOT=" + esctools,
					"SIMPLIFY=" + simplify,
					"JDKDIR=" + jdk,
					"JAVA_HOME=" + jdk,
					"PATH=" + jdk + "/bin" + ":/usr/local/bin:/usr/bin:/bin:/usr/local/sbin",
				};
		}
	}
	
	private static void writeToFile(File file, String str) throws IOException{
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file)));
		out.print(str);
		out.close();
	}
	
	public static ProjectResult annotateAndRun(
			ProjectSpecification spec,
			File projectDir, 
			EscjOptions opt, 
			Predicate<Clause> filter) throws IOException, ParseException{
		
		File tmpDir = Files.createTempDir();
		
		List<File> annotatedFiles = new LinkedList<File>();
		
		HashMap<String,AnnotatedFile> lineMaps =
			new HashMap<String, AnnotatedFile>();
		
		StringBuilder observedBodies = new StringBuilder();
		
		for (String cu : spec.getCompilationUnits()){
			File cuFile = new File(projectDir, cu);
			
			AnnotatedFile annotated = AnnotateFile.annotateJavaFile(cuFile, spec, filter);
			
			String body = annotated.getAnnotatedBody();
			
			if (opt.getObserved().contains(cu)){
				observedBodies.append(body).append(LINE_SEPARATOR);
			}
			
			File annFile = new File(tmpDir, cu);
			writeToFile(annFile, body);
			
			lineMaps.put(cu, annotated);
			
			annotatedFiles.add(annFile);
		}
		
		String output = run(annotatedFiles.toArray(new File[]{}),opt);
	
		ProjectResult res = EscJParser.parse(output,lineMaps);
		
		if (opt.getLogDir() != null && opt.getLogDir().exists() && opt.getLogDir().isDirectory()){
			File logProjDir = new File(opt.getLogDir(), spec.getName());
			
			if (!logProjDir.exists()){
				logProjDir.mkdir();
			}
			
			File logFile = new File(logProjDir, spec.getName() + "-" + res.getId() + ".log");

			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(logFile)));
			out.println(observedBodies.toString());
			out.println(output);
			out.close();
		}
		
		return res;
	}
	
	public static String run(File[] files, EscjOptions opt) throws IOException{
		Runtime r = Runtime.getRuntime();
		
		List<String> cmd = new ArrayList<String>();
		
		File escj = new File(opt.getEscjDir(),"escj");
		
		if (!escj.exists() || !escj.isFile()){
			throw new RuntimeException("File " + escj.getAbsolutePath() + " does not exist");
		}
		
		cmd.add(escj.getAbsolutePath());
		
		for (File f : files){
			cmd.add(f.getPath());
		}
		
		Process p = r.exec(cmd.toArray(new String[]{}), opt.getEnvP());

		BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
		
		BufferedReader err = new BufferedReader(new InputStreamReader(p.getErrorStream()));
		
		String line;
		
		while ( (line = err.readLine()) != null){
			System.err.println(line);
		}
		
		StringBuilder sb = new StringBuilder();
		while ( (line = br.readLine()) != null){
			sb.append(line).append(LINE_SEPARATOR);
		}
		return sb.toString();
	}
	
}
