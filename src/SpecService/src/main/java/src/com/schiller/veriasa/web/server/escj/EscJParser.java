package com.schiller.veriasa.web.server.escj;

import java.io.File;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.schiller.veriasa.web.shared.escj.AnnotatedFile;
import com.schiller.veriasa.web.shared.escj.Chunk;
import com.schiller.veriasa.web.shared.escj.Chunk.AssociatedDeclaration;
import com.schiller.veriasa.web.shared.escj.Chunk.TraceEntry;
import com.schiller.veriasa.web.shared.escj.MethodResult;
import com.schiller.veriasa.web.shared.escj.ProjectResult;
import com.schiller.veriasa.web.shared.escj.TypeResult;

public class EscJParser {

	private static Integer curId = 42;
	
	@SuppressWarnings("unused")
	private static Pattern methodPassed = Pattern.compile("^(\\S*?): (\\S*?) \\.\\.\\.$.?^.*?passed$", Pattern.MULTILINE);

	private static Pattern typePattern = Pattern.compile("^(\\S*?) \\.\\.\\.$", Pattern.MULTILINE);
	private static Pattern methodPattern = Pattern.compile("^(\\S*?): (.*?) \\.\\.\\.$", Pattern.MULTILINE);
	private static Pattern chunkHeadPattern = Pattern.compile("^\\S*?\\.java:([0-9]*?): (\\S*?): (.*?)$", Pattern.MULTILINE);
	private static Pattern assocDecPattern = Pattern.compile("^Associated declaration is \"(.*?)\", line ([0-9]*?), col ([0-9]*?):" , Pattern.MULTILINE);
	private static Pattern typeAbortedPattern = Pattern.compile("^Caution: Turning off extended static checking due to type error\\(s\\)", Pattern.MULTILINE);
	private static Pattern specErrorPattern = Pattern.compile("^(.*?):([0-9]*?): (.*?): (.*?)$", Pattern.MULTILINE);
	
	private static Pattern traceEntryPattern = Pattern.compile("^\\s*?Executed (.*?) in \"(.*?)\", line ([0-9]*?), col ([0-9]*?)\\.",Pattern.MULTILINE);
	private static Pattern traceEntryPattern2 = Pattern.compile("^\\s*?Routine (.*?) in \"(.*?)\", line ([0-9]*?), col ([0-9]*?)\\.",Pattern.MULTILINE);
	
	private static enum ParserState { START, TYPE, METHOD, METHOD_WARNING }
	
	
	public  static ProjectResult parse(String output, Map<String,AnnotatedFile> lineMaps) throws ParseException
	{
		List<Chunk> projErrs = new LinkedList<Chunk>();
		List<TypeResult> typeResults = new LinkedList<TypeResult>();
		String[] lines = output.split(EscJInterop.LINE_SEPARATOR);
		int len = lines.length;
		
		ParserState state = ParserState.START;
		
		
		TypeResult curType = null;
		MethodResult curMethod = null;
		Chunk curChunk = null;
		
		for (int i = 0; i < len; i++){
			Matcher typeMatcher = typePattern.matcher(lines[i]);
			Matcher methodMatcher = methodPattern.matcher(lines[i]);
			
			
			//This is the start of a type result
			if (typeMatcher.matches()){
				
				if (curType != null){
					if (curMethod != null){
						//save accumulated method information
						curType.addMethodResult(curMethod);
						curMethod = null;
					}
					//save accumulated type information
					typeResults.add(curType);
					curType = null;
				}
				
				state = ParserState.TYPE;
				curType = new TypeResult(typeMatcher.group(1));
			}
			
			//This is the start of a method result
			if (methodMatcher.matches()){
				String type = methodMatcher.group(1);
				
				if (!state.equals(ParserState.TYPE) && !state.equals(ParserState.METHOD)){
					throw new ParseException("Unexpected method",i);
				}
				
				if (!curType.getName().equals(type)){
					throw new ParseException("Unexpected method for type " + type,i);
				}
	
				if (curMethod != null){
					//save accumulated method information
					curType.addMethodResult(curMethod);
				}
				
				curMethod = new MethodResult(type,methodMatcher.group(2));
				state = ParserState.METHOD;
			}
			
		
			
			switch(state){
				case START:
					Matcher specErrorMatcher2 = specErrorPattern.matcher(lines[i]);
				
					if (specErrorMatcher2.matches()){
						Chunk c = new Chunk(
								specErrorMatcher2.group(1),
								Integer.parseInt(specErrorMatcher2.group(2)),
								specErrorMatcher2.group(3),
								specErrorMatcher2.group(4),
								lines[i+1],
								lines[i+2].indexOf('^'));
						
						projErrs.add(c);
					}
					
					break;
				case TYPE:
					Matcher specErrorMatcher = specErrorPattern.matcher(lines[i]);
					Matcher typeAbortedMatcher = typeAbortedPattern.matcher(lines[i]);
					
					if (specErrorMatcher.matches()){
						Chunk c = new Chunk(
								specErrorMatcher.group(1),
								Integer.parseInt(specErrorMatcher.group(2)),
								specErrorMatcher.group(3),
								specErrorMatcher.group(4),
								lines[i+1],
								lines[i+2].indexOf('^'));
						curType.addWarning(c);
					}else if (typeAbortedMatcher.matches()){
						curType.setAborted(true);
					}
					
					break;
				case METHOD:
					if (lines[i].startsWith("-----")){
						state = ParserState.METHOD_WARNING;	
					}
					break;
				case METHOD_WARNING:
				
					Matcher chunkHeadMatcher = chunkHeadPattern.matcher(lines[i]);
					Matcher assocDecMatcher = assocDecPattern.matcher(lines[i]);
					Matcher traceEntryMatcher = traceEntryPattern.matcher(lines[i]);
					Matcher traceEntryMatcher2 = traceEntryPattern2.matcher(lines[i]);
				
					if (lines[i].startsWith("-----")){
						curMethod.addWarning(curChunk);
						curChunk = null;

						Matcher chunkHeadMatcher2 = chunkHeadPattern.matcher(lines[i+1]);
						if (chunkHeadMatcher2.matches()){
							//new chunk!
							state = ParserState.METHOD_WARNING;
						}else{
							//not another chunk
							state = ParserState.METHOD;
						}
						break;
					}else if(chunkHeadMatcher.matches()){
						assert (curChunk == null);
						curChunk = new Chunk(
								Integer.parseInt(chunkHeadMatcher.group(1)),
								chunkHeadMatcher.group(2),
								chunkHeadMatcher.group(3),
								lines[i+1],
								lines[i+2].indexOf('^'));
					}else if(assocDecMatcher.matches()){
						AssociatedDeclaration assocDec = new AssociatedDeclaration(
								assocDecMatcher.group(1),
								Integer.parseInt(assocDecMatcher.group(2)),
								Integer.parseInt(assocDecMatcher.group(3)),
								lines[i+1],
								lines[i+2].indexOf('^'));
						curChunk.setAssociatedDeclaration(assocDec);
					}else if(traceEntryMatcher.matches()){
						TraceEntry e = new TraceEntry(
								traceEntryMatcher.group(1),
								traceEntryMatcher.group(2),
								Integer.parseInt(traceEntryMatcher.group(3)),
								Integer.parseInt(traceEntryMatcher.group(4)));
						curChunk.addTraceEntry(e);
					}else if(traceEntryMatcher2.matches()){
						TraceEntry e = new TraceEntry(
								traceEntryMatcher2.group(1),
								traceEntryMatcher2.group(2),
								Integer.parseInt(traceEntryMatcher2.group(3)),
								Integer.parseInt(traceEntryMatcher2.group(4)));
						curChunk.addTraceEntry(e);
					}

					break;

			}
		
		}
		
		if (curType != null){
			if (curMethod != null){
				//save accumulated method information
				curType.addMethodResult(curMethod);
				curMethod = null;
			}
			//save accumulated type information
			typeResults.add(curType);
			curType = null;
		}
		
		synchronized(curId){
			return new ProjectResult(++curId,typeResults, projErrs, lineMaps);
		}
		
		
	}
	
	/**
	 * Summarizes the outputs of ESC/Java2
	 * @param args the path to the 
	 */
	public static void main(final String[] args){
		if (args.length < 1){
			System.err.println("usage: EscjParser /path/to/escjoutput [[package.prefix.to.ignore] [...]]");
			System.exit(-1);
		}
		
		File escjOut = new File(args[0]);
		
		if (!escjOut.exists()){
			System.err.println("File " + escjOut + " does not exist");
			System.exit(-1);
		}
		
		ProjectResult result = null;
		
		HashMap<String, List<TypeResult>> packageResults = Maps.newHashMap();
		
		try {
			result = parse(
					Joiner.on(EscJInterop.LINE_SEPARATOR).join(Files.readLines(escjOut, Charset.defaultCharset())), 
					new HashMap<String,AnnotatedFile>());
		} catch (Exception e) {
			System.err.println("Error parsing ESC Java2 output");
			e.printStackTrace();
			System.exit(-1);
		} 
		
		final Function<TypeResult, String> packageName = new Function<TypeResult,String>(){
			@Override
			public String apply(TypeResult arg0) {
				String name = arg0.getName();
				int lastPeriod = name.lastIndexOf(".");
				
				return lastPeriod > 0 ? name.substring(0, name.lastIndexOf(".")) : "(default)";
			}
		};
		
		for (TypeResult tr : result.getTypeResults()){
			String pk = packageName.apply(tr);
			
			if (!packageResults.containsKey(pk)){
				packageResults.put(pk, new ArrayList<TypeResult>());
				packageResults.get(pk).add(tr);
			}else{
				packageResults.get(pk).add(tr);
			}
		}
		
		final Function<TypeResult,Integer> numMethodErr = new Function<TypeResult,Integer>(){
			@Override
			public Integer apply(TypeResult tr) {
				int cnt = 0;
				for (MethodResult mr : tr.getMethodResults()){
					cnt += mr.getWarnings().size();
				}
				return cnt;
			}
		};
		
		for (String packageResult : packageResults.keySet()){
			int cnt = 0;
			for (TypeResult r : packageResults.get(packageResult)){
				cnt += numMethodErr.apply(r);
			}
			
			System.out.println("package " + packageResult + ": # Method Warnings " + cnt);
		}
		
		
		final Predicate<TypeResult> filter = new Predicate<TypeResult>(){
			@Override
			public boolean apply(TypeResult arg0) {
				for (int i = 1; i < args.length; i++){
					if (arg0.getName().startsWith(args[i])){
						return false;
					}
				}
				return true;
			}
		};
		
		List<TypeResult> trs = Lists.newArrayList(result.getTypeResults());
		
		Collections.sort(trs, new Comparator<TypeResult>(){
			@Override
			public int compare(TypeResult lhs, TypeResult rhs) {
				if (packageName.apply(lhs).equals(packageName.apply(rhs))){
					int lhsErrs = lhs.getWarnings().size() + numMethodErr.apply(lhs);
					int rhsErrs = rhs.getWarnings().size() + numMethodErr.apply(rhs);
					
					return Integer.valueOf(lhsErrs).compareTo(rhsErrs);
				}else{
					return packageName.apply(lhs).compareTo(packageName.apply(rhs));
				}
			}
		});
		
		for (TypeResult r : Iterables.filter(trs, filter)){
			System.out.println(r.getName() + ": Method Warnings " + numMethodErr.apply(r) + " (" + r.getMethodResults().size() + " methods)" );
		}
	}
}
