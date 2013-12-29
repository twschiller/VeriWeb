package com.schiller.veriasa.util;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.jmlspecs.checker.JmlClassDeclaration;
import org.jmlspecs.checker.JmlCompilationUnit;
import org.jmlspecs.checker.JmlLexer;
import org.jmlspecs.checker.JmlMLLexer;
import org.jmlspecs.checker.JmlMethodDeclaration;
import org.jmlspecs.checker.JmlParser;
import org.jmlspecs.checker.JmlSLLexer;
import org.jmlspecs.checker.Main;
import org.jmlspecs.checker.TokenStreamSelector;
import org.multijava.mjc.JCompilationUnitType;
import org.multijava.mjc.JTypeDeclarationType;
import org.multijava.mjc.JavadocLexer;
import org.multijava.mjc.ParsingController;
import org.multijava.mjc.ParsingController.ConfigurationException;
import org.multijava.mjc.ParsingController.KeyException;

import antlr.RecognitionException;
import antlr.TokenStreamException;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.schiller.veriasa.experiment.LogDistance;
import com.schiller.veriasa.web.shared.core.FieldSpec;
import com.schiller.veriasa.web.shared.core.MethodContract;
import com.schiller.veriasa.web.shared.core.Clause;
import com.schiller.veriasa.web.shared.core.TypeSpecification;

/**
 * Utility methods for parsing project source files
 * @author Todd Schiller
 */
public abstract class ParseSource {
	private static Logger log = Logger.getLogger(ParseSource.class);
	
	public static class BadCnt<T>{
		public T item;
		public int bad;
		
		public BadCnt(T item, int bad) {
			super();
			this.item = item;
			this.bad = bad;
		}
	}
	
	/**
	 * Thrown if the specification includes an unsupported <code>@also</code> clause
	 * @author Todd Schiller
	 */
	public static class AlsoException extends Exception{
		private static final long serialVersionUID = 1L;
	}

	private static JmlCompilationUnit parseCompilationUnit(Reader reader) throws RecognitionException, TokenStreamException{
		Main compiler = new Main();
	
		ParsingController parsingController = new ParsingController( reader, null );
		
		TokenStreamSelector lexingController = new TokenStreamSelector();
		boolean allowUniverses = false; // WMD
		JmlLexer jmlLexer = new JmlLexer( parsingController, lexingController, 
				true, true, allowUniverses, compiler );
		JavadocLexer docLexer = new JavadocLexer( parsingController );
		JmlMLLexer jmlMLLexer = new JmlMLLexer( parsingController, lexingController, 
				true, true, allowUniverses, compiler );
		JmlSLLexer jmlSLLexer = new JmlSLLexer( parsingController, lexingController, 
				true, true, allowUniverses, compiler );
		
		try{
			lexingController.addInputStream( jmlLexer, "jmlTop" );
			lexingController.addInputStream( jmlMLLexer, "jmlML" );
			lexingController.addInputStream( jmlSLLexer, "jmlSL" );
			lexingController.addInputStream( docLexer, "javadoc" );
			lexingController.select( "jmlTop" );
			parsingController.addInputStream( lexingController, "jml" );
			parsingController.addInputStream( docLexer, "javadoc" );
			parsingController.selectInitial( "jml" );

			final boolean ACCEPT_MULTIJAVA = true;
			final boolean ACCEPT_RELAXEDMULTIJAVA = true;
			JmlParser parser = 
				new JmlParser( compiler, 
						parsingController.initialOutputStream(),
						parsingController,
						true,
						ACCEPT_MULTIJAVA, 
						ACCEPT_RELAXEDMULTIJAVA,
						allowUniverses );
			lexingController.push( "jmlML" );
			return (JmlCompilationUnit) parser.jCompilationUnit();
		}catch(KeyException kex){
			throw new RuntimeException("JML parser error");
		}catch(ConfigurationException kex){
			throw new RuntimeException("JML parser error");
		}
	}
	

	/**
	 * Moves preconditions (requires clauses) to before postconditions
	 * @param oldLines lines
	 * @return
	 */
	private static String [] fixPreconditionOrdering(String [] oldLines){
		for (int i = 1; i < oldLines.length; i++){
			if (isRequiresClause(oldLines[i]) && isPostconditionClause(oldLines[i-1])){
				//correct the inversion
				String tmp = oldLines[i-1];
				oldLines[i-1] = oldLines[i];
				oldLines[i] = tmp;
				
				//start checking from beginning again
				i = 0;
			}
		}
		return oldLines;
	}
	
	/**
	 * Moves regular postconditions (and modifies clauses) to before exceptional postconditions
	 * Assumes fixPreOrder has already been called
	 * @param oldLines the original lines
	 */
	private static String [] fixPostconditionOrdering(String [] oldLines){
		for (int i = 1; i < oldLines.length; i++){
			if ((isEnsuresClause(oldLines[i]) || isModifiesClause(oldLines[i])) && isExsuresClause(oldLines[i-1])){
				//correct th inversion
				String tmp = oldLines[i-1];
				oldLines[i-1] = oldLines[i];
				oldLines[i] = tmp;
				
				//start checking from beginning again
				i = 0;
			}
		}
		return oldLines;
	}
	
	/**
	 * Remove lines that match the given predicate
	 * @param orig original lines
	 * @param toRemove predicate indicating which lines to remove
	 * @return lines with lines matching <code>toRemove</code> removed, and the number of lines removed
	 */
	private static BadCnt<String[]> removeLines(String orig[], Predicate<String> toRemove){
		int cnt = 0;
		List<String> xs = Lists.newArrayList();
		for (String l : orig){
			String tt = l.trim();
			if (toRemove.apply(tt)){
				log.trace("remove line:" + tt);
				cnt++;			
			}else{
				xs.add(l);
			}	
		}
		return new BadCnt<String[]>(xs.toArray(new String[]{}), cnt);
	}
	
	/**
	 * true iff a line is a malformed exsures clause
	 */
	private static final Predicate<String> malformedExsuresClause = new Predicate<String>(){
		@Override
		public boolean apply(String l) {
			return l.startsWith("//@exsures") && 
				!l.matches("//@exsures\\s+\\(RuntimeException\\s*?\\S*?\\s*?\\).*;");
		}
	};
	
	/**
	 * true if the line is a non-JML single-line comment
	 */
	private static final Predicate<String> isNonJmlComment = new Predicate<String>(){
		@Override
		public boolean apply(String l) {
			return (l.startsWith("//") && !l.startsWith("//@"))
				   || (l.startsWith("/*") && l.endsWith("*/") && !l.startsWith("/*@"));
		}
	};
	
	private static final Predicate<String> extraSlash = new Predicate<String>(){
		@Override
		public boolean apply(String l) {
			return isJmlStatement(l) && l.indexOf('/', 3) >= 0;
		}
	};
	
	private static final Predicate<String> isModifies = new Predicate<String>(){
		@Override
		public boolean apply(String l) {
			return isModifiesClause(l);
		}
	};
	
	private static final Predicate<String> isEmptyLine = new Predicate<String>(){
		@Override
		public boolean apply(String l) {
			return l.trim().isEmpty();
		}
	};
	
	private static final Predicate<String> isEmptyJmlStatement = new Predicate<String>(){
		@Override
		public boolean apply(String l) {
			return l.matches("//@requires\\s+;") || l.matches("//@exsures\\s+;") || l.matches("//@ensures\\s+;");
		}
	};
	
	public static boolean isJmlStatement(String line){
		return isRequiresClause(line) || isPostconditionClause(line);
	}
	
	public static boolean isRequiresClause(String line){
		return line.trim().startsWith("//@requires");
	}
	
	public static boolean isModifiesClause(String line){
		String t = line.trim();
		return t.startsWith("//@modifies") || t.startsWith("//@assignable");
	}

	public static boolean isPostconditionClause(String line){
		return isModifiesClause(line) || isEnsuresClause(line) || isExsuresClause(line);
	}
	
	public static boolean isEnsuresClause(String line){
		return line.trim().startsWith("//@ensures");
	}
	
	public static boolean isExsuresClause(String line){
		return line.trim().startsWith("//@exsures");
	}
	
	/**
	 * true iff the JML statements must be relocated (not all of the statements appear between
	 * the method signature, and the method's opening brace)
	 * @param lines source lines
	 * @return true iff the JML statements must be relocated using {@link ParseSource#moveJmlStatements(String[])}
	 */
	private static boolean moveJmlStatementsRequired(String [] oldLines){
		int methodCount = 0;
		
		boolean before = false; //statements appear before method signature
		boolean after = false; // statements appear after method signature, before brace
		
		boolean inMethod = false;
	
		for (int i = 0; i < oldLines.length; i++){
			String line = oldLines[i];
			
			if (isJmlStatement(line)){
				before |= !inMethod;
				after |= inMethod;
			}else if (line.matches(".*public.*?(\\S+)\\(.*?\\).*")){
				inMethod = true;
				methodCount++;
			}else if (line.contains("{")){
				inMethod = false;
			}
		}
		
		if (before && after){
			throw new RuntimeException("Inconsistent JML statements locations");
		}else if (!before && !after){
			throw new RuntimeException("No evidence of JML locations");
		}else if (methodCount == 0){
			throw new RuntimeException("Parsing error: no methods found");
		}
		
		return before;
	}
	
	/**
	 * Relocate JML statements so that a method's specifications appear directly below
	 * the method signature, before the opening method brace
	 * @param oldLines original lines
	 */
	public static String [] moveJmlStatements(String [] oldLines){
		String currentMethodName = null;
		List<String> currentJmlStatements = Lists.newArrayList();
		
		HashMap<String, List<String>> methodJmlStatements = Maps.newHashMap();
		
		List<String> nonJmlLines = Lists.newArrayList();
		
		int blockLevel = 0;
		
		Pattern methodSignaturePattern = Pattern.compile(".*public.*?(\\S+)\\(.*?\\).*");
		
		for (int i = 0; i < oldLines.length; i++){
			String line = oldLines[i];
			
			if (isJmlStatement(line)){
				currentJmlStatements.add(line);
			}else{
				nonJmlLines.add(line);
		
				Matcher m = methodSignaturePattern.matcher(line);
				if (m.matches()){
					currentMethodName = m.group(1);
				}
			}
		
			if (line.contains("{")){
				blockLevel++;
			}else if (line.contains("}")){
				blockLevel--;
				
				//close method brace
				if (blockLevel == 1){
					methodJmlStatements.put(currentMethodName, currentJmlStatements);
					currentJmlStatements = Lists.newArrayList();
					currentMethodName = null;
				}	
			}
		}
		
		List<String> result = Lists.newArrayList();
		
		for (String line : nonJmlLines){
			result.add(line);
			
			//re-add JML specifications in the correct location
			Matcher m = methodSignaturePattern.matcher(line);
			if (m.matches() && methodJmlStatements.containsKey(m.group(1))){
				result.addAll(methodJmlStatements.get(m.group(1)));
			}
		}
		
		return result.toArray(new String[]{});
	}
	
	public static String [] addMissingExsuresStatements(String [] old){
		List<String> xs = Lists.newArrayList();	
		for (int i = 0; i < old.length; i++){
			String l = old[i];	
			xs.add(l);
			if (l.trim().contains("throws")){
				xs.add("//@exsures (RuntimeException) true;");
			}
		}
		return xs.toArray(new String[]{});
	}
	
	
	public static String [] addMissingRequiresStatements(String [] oldLines){
		List<String> result = Lists.newArrayList();
		
		int blockLevel = 0;
		
		//true if the current method is missing a requires statement
		boolean missingRequires = true;
		
		for (int i = 0; i < oldLines.length; i++){
			String line = oldLines[i];
			
			if (isRequiresClause(line)){
				missingRequires = false;
			}else if(missingRequires && (isPostconditionClause(line))){
				result.add("//@requires true;");
				missingRequires = false;
			}
			
			result.add(oldLines[i]);
			
			if (line.contains("{")){
				blockLevel++;
			}else if (line.contains("}")){
				blockLevel--;
				
				//close method brace
				if (blockLevel == 1){
					missingRequires = true;
				}	
			}
		}
		return result.toArray(new String[]{});
	}
	
	public static boolean usesAlso(String oldLines[]){
		return Iterables.any(Lists.newArrayList(oldLines), new Predicate<String>(){
			@Override
			public boolean apply(String line) {
				return line.trim().equals("also");
			}
		});
	}

	/**
	 * Remove all Javadoc comments from a compilation unit
	 */
	public static String removeJavadoc(String compilationUnit){
		return compilationUnit.replaceAll("/\\*\\*(?:.|[\\n\\r])*?\\*/", Matcher.quoteReplacement("\n"));
	}
	
	/**
	 * Split JML statements contained in mutli-line comment blocks into 
	 * multiple single line statements.
	 */
	private static String splitJmlStatements(String before) throws AlsoException{
		Pattern p = Pattern.compile("/\\*@\\s*?(\\S+?)\\s+((?:.|[\\n\\r])+?)\\*/");
		Matcher m = p.matcher(before);
		
		StringBuffer sb = new StringBuffer();
		while (m.find()){
			
			if (m.group().contains("also")){
				throw new AlsoException();
			}
			
			if (m.groupCount() > 1){
				String uni = m.group(2).replaceAll("\\n"," ").trim();
				
				String fix = "//@" + m.group(1) + " " +  (uni.endsWith(";") ? uni : (uni + ";"));
				fix = fix.replace((CharSequence) "\\", "\\\\");
				m.appendReplacement(sb, fix);
			}else{
				m.appendReplacement(sb, m.group().replace((CharSequence) "\\", "\\\\"));
			}
		}
		m.appendTail(sb);
		return sb.toString();
	}
	
	/**
	 * Normalize the formatting of JML statement lines
	 * @param oldLines the old lines
	 */
	private static String[] normalizeJmlStatements(String [] oldLines){
		Pattern p = Pattern.compile(".*?//@\\s*?(\\S+?)\\s+?(.+)");
		for (int i = 0; i < oldLines.length; i++){
			Matcher m = p.matcher(oldLines[i]);
			if (m.matches()){
				oldLines[i] = "//@" + m.group(1) + " " + m.group(2).trim() + (m.group(2).trim().endsWith(";") ? "" : ";");
			}
		}
		return oldLines;
	}
	

	public static String removeUnsupportedJmlAnnotations(String source){
		return source
			.replaceAll("/\\*@.*?spec_public.*?\\*/", "")
			.replaceAll("/\\*@.*?modifies.*?\\*/", "")
			.replaceAll("/\\*@.*?signals.*?\\*/", "")
			.replaceAll("/\\*@.*?set.*?\\*/", "")
			.replaceAll("/\\*@.*?pure.*?\\*/", "")
			.replaceAll(".*?//.*?pure","\n");
	}
	
	private static BadCnt<String> patch(String full, String lineSeparator) throws AlsoException{
		String own = full.replaceAll("\\{", "\n{\n").replaceAll("\\}", "\n}\n");
		
		String a0 = removeJavadoc(own);
		String a05 = removeUnsupportedJmlAnnotations(a0);
		String a1 = splitJmlStatements(a05);
	
		String a2 = removeMethodBodies(a1);
		
		String a3[] = a2.split(lineSeparator);
		String a35[] = normalizeJmlStatements(a3);
		String a4[] = removeLines(a35, isNonJmlComment).item;
		String a5[] = moveJmlStatementsRequired(a4) ? moveJmlStatements(a4) : a4;
		       
		String a6[] = addMissingExsuresStatements(a5);
		String a7[] = addMissingRequiresStatements(a6);
		
		String a8[] = removeLines(a7, isEmptyJmlStatement).item;
	
		String a85[] = removeLines(a8, isEmptyLine).item;
		
		String a9[] = fixPreconditionOrdering(a85);
		String a10[] = fixPostconditionOrdering(a9);
		
		BadCnt<String[]> xx = removeLines(a10, malformedExsuresClause);
		BadCnt<String[]> yy = removeLines(xx.item, extraSlash);
		String patched[] = removeLines(yy.item, isModifies).item;
		return new BadCnt<String>(Joiner.on("\n").join(patched), xx.bad + yy.bad);
	}
	
	/**
	 * Delete the method bodies in compilation unit method <code>withBodies</code>
	 * @param withBodies compilation unit source with method bodies
	 * @return compilation unit source without method bodies
	 */
	private static String removeMethodBodies(String withBodies){
		List<String> nls = Lists.newArrayList();
		
		int level = 0;
		for (String line : withBodies.split("\n")){
			if (level < 2){
				nls.add(line);
			}
			if (line.contains("{")){
				level++;
			}else if (line.contains("}")){
				level--;

				//closing brace for method
				if (level == 1){
					nls.add(line);
				}	
			}
		}
		return Joiner.on("\n").join(nls);
	}
	
	
	public static BadCnt<TypeSpecification> readSpec(File file) throws IOException, AlsoException{
		List<String> lines = Files.readLines(file, Charset.defaultCharset());
		String cuTxt = Joiner.on('\n').join(lines);
		return readSpec(cuTxt, "\n");
	}
	
	public static BadCnt<TypeSpecification> readSpec(String cuTxt, String lineSeparator) throws AlsoException{
		BadCnt<String> patched = patch(cuTxt, lineSeparator);
		String patchedCuTxt = patched.item;
		
		JCompilationUnitType cu;
		try {
			cu = ParseSource.parseCompilationUnit(new StringReader(patchedCuTxt));
		} catch (Exception e) {
			throw new RuntimeException("Invalid source");
		} 

		JTypeDeclarationType td;

		if (cu.typeDeclarations().length == 1){
			td = cu.typeDeclarations()[0];
		}else{
			throw new RuntimeException("Only one type declaration per file is supported");
		}

		final JmlClassDeclaration c = (JmlClassDeclaration) td;

		List<Clause> invs = Convert.convert(c.invariants());
		
		@SuppressWarnings("unchecked")
		List<MethodContract> methodSpecs = Lists.newArrayList(Iterables.transform((List<MethodContract>)c.methods(), new Function<Object, MethodContract>(){
			@Override
			public MethodContract apply(Object arg0) {
				return MethodSpecConverter.generate(c.ident(), (JmlMethodDeclaration) arg0);
			}
		}));
		
		return new BadCnt<TypeSpecification>(new TypeSpecification(td.ident(), null,invs, new ArrayList<FieldSpec>(),methodSpecs),patched.bad);
	}
	
	public static void main(String[] args) throws IOException, AlsoException{
		// test that specifications are being parsed correctly
		File file = new File("/home/tws/projects/asa/veriweb-paper/study/results/vworker/targets/StackAr/StackAr.alexey007.2.java");
		
		BadCnt<TypeSpecification> spec = readSpec(file);
		
		LogDistance.printSpec(spec.item);
	}
}
