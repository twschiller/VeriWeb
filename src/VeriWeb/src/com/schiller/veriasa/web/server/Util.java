package com.schiller.veriasa.web.server;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.transform;
import static com.schiller.veriasa.web.shared.core.MethodSpecBuilder.builder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import antlr.RecognitionException;
import antlr.TokenStreamException;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.schiller.veriasa.executejml.ExecuteJml;
import com.schiller.veriasa.executejml.SpanParser;
import com.schiller.veriasa.web.server.escj.EscJUtil;
import com.schiller.veriasa.web.shared.config.JmlParseException;
import com.schiller.veriasa.web.shared.core.DefinitionMap;
import com.schiller.veriasa.web.shared.core.DefinitionMap.ElementDefinition;
import com.schiller.veriasa.web.shared.core.DefinitionMap.SourceElement;
import com.schiller.veriasa.web.shared.core.HasQualifiedSignature;
import com.schiller.veriasa.web.shared.core.MethodContract;
import com.schiller.veriasa.web.shared.core.ProjectSpecification;
import com.schiller.veriasa.web.shared.core.Clause;
import com.schiller.veriasa.web.shared.core.TypeSpecification;
import com.schiller.veriasa.web.shared.core.TypeSpecBuilder;
import com.schiller.veriasa.web.shared.escj.AnnotatedFile;
import com.schiller.veriasa.web.shared.escj.Chunk;
import com.schiller.veriasa.web.shared.escj.MethodResult;
import com.schiller.veriasa.web.shared.escj.ProjectResult;
import com.schiller.veriasa.web.shared.escj.TypeResult;
import com.schiller.veriasa.web.shared.messaging.UserMessage;
import com.schiller.veriasa.web.shared.parsejml.JmlSpan;

public abstract class Util {

	public final static String LINE_SEPARATOR = "\n";
	
	public final static String QUALIFIED_OBJECT_ARRAY = "java.lang.Object[]";
	public final static String OBJECT_ARRAY_REGEXP = "Object\\[\\]";
	
	/**
	 * the next unique span id to use when modifying HTML
	 */
	private static long nextSpanId = 42;
	
	//TODO: do these have to be synchronized hashmaps instead?
	private static Map<JavadocKey, Integer> docToId = Maps.newConcurrentMap();
	protected static Map<Integer, String> idToDoc = Maps.newConcurrentMap();
	protected static Map<Integer, String> idToSig = Maps.newConcurrentMap();
	
	protected static Map<String, Integer> warningToId = Maps.newConcurrentMap();
	protected static Map<Integer,String> idToWarning = Maps.newConcurrentMap();
	protected static Map<Integer, Set<Chunk>> idToWarningChunks = Maps.newConcurrentMap();
	

	public static class SameSignature implements Predicate<HasQualifiedSignature>{
		private final String query;

		public SameSignature(String query) {
			super();
			this.query = query;
		}
		
		public SameSignature(HasQualifiedSignature query){
			this(query.qualifiedSignature());
		}
		
		@Override
		public boolean apply(HasQualifiedSignature signature) {
			String qualified = signature.qualifiedSignature();
			
			String normalizedSignature = qualified.contains(QUALIFIED_OBJECT_ARRAY) ? 
					qualified :
					qualified.replaceAll(OBJECT_ARRAY_REGEXP, QUALIFIED_OBJECT_ARRAY);
			
			normalizedSignature = TextUtil.removeWhitespace(normalizedSignature);
				
			String normalizedQuery = query.replaceAll(OBJECT_ARRAY_REGEXP, QUALIFIED_OBJECT_ARRAY);
			normalizedQuery = TextUtil.removeWhitespace(normalizedQuery);
			
			return normalizedSignature.equals(normalizedQuery);
		}
	}
	
	protected static final Comparator<UserMessage> messageContentEq = new Comparator<UserMessage>(){
		@Override
		public int compare(UserMessage lhs, UserMessage rhs) {
			if (lhs.getUserId() == rhs.getUserId() 
				&& lhs.getSourceMethod().equals(rhs.getSourceMethod()) 
				&& lhs.getComment().equals(rhs.getComment())){
				
				return 0;
			}
			else return -1;
		}
	};
	
	/**
	 * Create a new project specification by replacing the old type specification in
	 * <code>project</code> with <code>newType</code>.
	 * @param project the project specification
	 * @param newType the new type specification
	 * @return a new (modified) project specification
	 */
	public static ProjectSpecification modifySpec(ProjectSpecification project, TypeSpecification newType){
		List<TypeSpecification> newTypes = Lists.newArrayList();
		
		for (TypeSpecification type : project.getTypeSpecs()){
			if (type.getFullyQualifiedName().equals(newType.getFullyQualifiedName())){
				newTypes.add(newType);
			}else{
				newTypes.add(type);
			}
		}
		
		return new ProjectSpecification(project.getName(), newTypes);
	}
	
	/**
	 * Create a new project specification by replacing the old method specification in
	 * <code>project</code> with <code>newMethod</code>.
	 * @param project the project specification
	 * @param newType the new method specification
	 * @return a new (modified) project specification
	 */
	public static ProjectSpecification modifySpec(ProjectSpecification project, MethodContract newMethod){
		List<TypeSpecification> newTypes = Lists.newArrayList();
		
		for (TypeSpecification type : project.getTypeSpecs()){
			if (newMethod.getInfo().getLocation().getCompilationUnit().equals(type.getLocation().getCompilationUnit())){
				List<MethodContract> newMethods = Lists.newLinkedList();
				
				for (MethodContract method : type.getMethods()){
					if (method.getSignature().equals(newMethod.getSignature())){
						newMethods.add(newMethod);
					}else{
						newMethods.add(method);
					}
				}
				
				newTypes.add(TypeSpecBuilder.builder(type)
						.setMethods(newMethods)
						.getType());
			}else{
				newTypes.add(type);
			}
		}
		
		return new ProjectSpecification(project.getName(), newTypes);
	}
	
	/**
	 * Get the compilation unit for <code>method</code>
	 * @param method the method
	 * @return the compilation unit for <code>method</code>
	 */
	public static String getCompilationUnit(MethodContract method){
		return method.getInfo().getLocation().getCompilationUnit();
	}
	
	/**
	 * Returns the type result for <code>compilationUnit</code>, or <code>null</code>
	 * iff no result exists in <code>result</code>
	 * @param result the project result
	 * @param compilationUnit query
	 * @return the type result for <code>compilationUnit</code>, or <code>null</code>
	 * iff no result exists in <code>result</code>
	 */
	public static TypeResult getTypeResult(ProjectResult result, String compilationUnit){
		for (TypeResult type : result.getTypeResults()){
			//TODO: this assumption is incorrect for inner classes
			String typeCu = type.getName() + ".java";
			
			if (typeCu.equals(compilationUnit)){
				return type;
			}
		}
		return null;
	}
	
	/**
	 * Returns all method specifications in <code>project</code>
	 * @param project the project
	 * @return all method specifications in <code>project</code>
	 */
	public static List<MethodContract> allMethods(ProjectSpecification project){
		List<MethodContract> result = Lists.newArrayList();
		for (TypeSpecification type : project.getTypeSpecs()){
			result.addAll(type.getMethods());
		}
		return result;
	}
	
	/**
	 * Remove specifications from <code>type</code> that do not meet inclusion predicate <code>predicate</code>
	 * @param type the type specification
	 * @param predicate the inclusion predicate
	 * @return the filtered type specification
	 */
	public static TypeSpecification filterSpecs(TypeSpecification type, Predicate<Clause> predicate){
		List<MethodContract> methods = Lists.newArrayList();
		
		for (MethodContract method : type.getMethods()){
			
			Map<String,List<Clause>> newExsures = Maps.newHashMap();
			for (String exception : method.getExsures().keySet()){
				newExsures.put(exception, newArrayList(filter(method.getExsures().get(exception), predicate)));
			}
			
			methods.add(builder(method)
					.setRequires(newArrayList(filter(method.getRequires(), predicate)))
					.setEnsures(newArrayList(filter(method.getEnsures(), predicate)))
					.setExsures(newExsures)
					.getSpec());
		}
		
		return new TypeSpecification(
				type.getFullyQualifiedName(),
				type.getLocation(),
				newArrayList(filter(type.getInvariants(), predicate)),
				type.getFields(),
				methods);
	}
	
	/**
	 * Remove specifications from <code>project</code> that do not meet inclusion predicate <code>predicate</code>
	 * @param project the project specification
	 * @param predicate the inclusion predicate
	 * @return the filtered project specification
	 */
	public static ProjectSpecification filterSpecs(ProjectSpecification project, final Predicate<Clause> predicate){
		Iterable<TypeSpecification> types = transform(project.getTypeSpecs(), new Function<TypeSpecification,TypeSpecification>(){
			@Override
			public TypeSpecification apply(TypeSpecification type) {
				return filterSpecs(type,predicate);
			}
		});
		return new ProjectSpecification(project.getName(), newArrayList(types));
	}
	
	protected static int relativeOffset(MethodContract loc, int absOffset){
		return absOffset - loc.getInfo().getLocation().getOffset();
	}
	
	/**
	 * Return the type containing <code>method</code>, or null iff it cannot be found
	 * @param project the project
	 * @param method query method
	 * @return the type containing <code>method</code>, or null iff it cannot be found
	 */
	protected static TypeSpecification findType(ProjectSpecification project, MethodContract method){
		for (TypeSpecification type : project.getTypeSpecs()){
			if (method.getInfo().getLocation().getCompilationUnit().equals(type.getLocation().getCompilationUnit())){
				return type;
			}
		}
		return null;
	}

	/**
	 * Return the type with the given <code>qualifiedName</code>
	 * @param project the project
	 * @param qualifiedName the query query 
	 * @return the type with the given <code>qualifiedName</code>
	 */
	public static TypeSpecification findType(ProjectSpecification project, final String qualifiedName){
		return Iterables.find(project.getTypeSpecs(), new Predicate<TypeSpecification>(){
			@Override
			public boolean apply(TypeSpecification type) {
				return type.getFullyQualifiedName().equals(qualifiedName);
			}
		});
	}
		
	/**
	 * For each line in the method, determine the method calls that have occurred 
	 * prior to that line
	 * @param bodyWithDoc the documented method body
	 * @return the method calls that occur before each line
	 */
	private static ArrayList<Set<String>> priorMethodCalls(String bodyWithDoc){
		ArrayList<Set<String>> result = Lists.newArrayList();
		
		String [] lines = bodyWithDoc.split(LINE_SEPARATOR);
		HashSet<String> accumulator = Sets.newHashSet();
		
		Pattern callPattern = Pattern.compile("\\[\\[DOC.. onmouseover\\=\"showDoc\\((\\d+?)\\)\".*?DOC\\]\\]");
		
		for (String line : lines){
			//skip the ctor signature
			if (line.trim().startsWith("public")){
				continue;
			}
			
			// add the methods that occur on the line
			Matcher callMatcher = callPattern.matcher(line);
			while (callMatcher.find()){
				int id = Integer.parseInt(callMatcher.group(1));
				String signature = idToSig.get(id);
				
				if (signature != null){
					accumulator.add(signature);
				}
			}
			
			result.add(Sets.newHashSet(accumulator));
		}
		return result;
	}

	
	protected static String addSlots(String body){
		//get rid warnings in postcondition problems
		String [] lines = body.replaceAll("\\[WARNINGID\\]", "-1").split(LINE_SEPARATOR);
		
		StringBuilder result = new StringBuilder();
		
		Pattern p = Pattern.compile("\\[\\[DOC.*?toggleDoc\\((.*?),(.*?),(.*?),(.*?)\\)\"\\s+id\\=\\S+ DOC\\]\\]");
		
		result.append(lines[0]).append(LINE_SEPARATOR);
		
		for (int i = 1; i < lines.length; i++){
			String line = lines[i];
		
			if (line.trim().startsWith("public")){
				result.append(line).append(LINE_SEPARATOR);
				continue;
			}else if (line.trim().isEmpty()){
				continue;
			}
			
			String ident = line.substring(0, TextUtil.indent(line));
			
			Matcher m = p.matcher(line);
			
			List<String> toAppend = Lists.newArrayList();
			
			while (m.find()){
				int id = Integer.parseInt(m.group(1));
				int pre = Integer.parseInt(m.group(2));
				int post = Integer.parseInt(m.group(3));
				String signature = idToSig.get(id);
				
				if (signature != null){
					result.append(ident).append("[[PRE id=\"pre" + pre + "\" PRE]][[PRE]]").append(LINE_SEPARATOR);
				}
				
				toAppend.add("[[POST id=\"post" + post + "\" POST]][[POST]]");
			}
			
			result.append(line).append(LINE_SEPARATOR);
			Collections.reverse(toAppend);
			for (String s : toAppend ){
				result.append(ident).append(s).append(LINE_SEPARATOR);
			}
			
		}
		return result.toString();
	}
		
	/**
	 * Add ERR annotations to a method body
	 * @param warnings the method errors and warnings
	 * @param lineMap map lines in the annotated file to lines in the unannotated file
	 * @param startLine the 0-based starting line of the method in the unannotated file
	 * @param defBody the method body
	 * @return the annotated method body
	 */
	protected static String markMethodWarnings(
			List<Chunk> warnings, 
			AnnotatedFile lineMap, 
			int startLine, 
			String body, 
			Predicate<Chunk> predicate, 
			ProjectSpecification project){
		
		ArrayList<Set<String>> priorMethodCalls = priorMethodCalls(body);
		
		String [] lines = body.split(LINE_SEPARATOR);

		HashMap<Integer, StringBuilder> lineWarnings = Maps.newHashMap();
		HashMap<Integer, Set<Chunk> > lineChunks = Maps.newHashMap();
		
		for (int i = 0; i < warnings.size(); i++){
			Chunk warning = warnings.get(i);
			
			if (predicate.apply(warning)){
				//zero-based
				int cuLine = lineMap.originalLine(warning.getLine() - 1);
				
				if (warning.getMessage().trim().endsWith("(Exception)") && warning.hasTrace()){
					cuLine = lineMap.originalLine(warning.getTrace().get(warning.getTrace().size() - 1).getLine() - 1);
				}
				
				int bodyLine = cuLine - startLine;
						
				//TODO: each warning should have a header
				StringBuilder warningHtml;
				Set<Chunk> chunksForLine;
				if (lineWarnings.containsKey(bodyLine)){
					warningHtml = lineWarnings.get(bodyLine);
					chunksForLine = lineChunks.get(bodyLine);
				}else{
					warningHtml = new StringBuilder();
					chunksForLine = Sets.newHashSet();
					
					lineWarnings.put(bodyLine, warningHtml);
					lineChunks.put(bodyLine, chunksForLine);
				}
				
				chunksForLine.add(warning);
				
				warningHtml.append("<p><span class=\"warning-head\">");
				
				warningHtml.append(warning.getMessage().substring(0,warning.getMessage().lastIndexOf("("))).append("</span></p><p>");
				
				int ident = TextUtil.indent(warning.getBadLine());
				
				warningHtml.append("<span class=\"warning-code\">")
					.append(warning.getBadLine().trim()).append("<br>");
			
				//TODO: put the caret back in
				StringBuilder cc  = new StringBuilder();
				cc.append(warning.getBadLine()).append("\n");
				for (int j =0 ; j < warning.getBadLineOffset() - ident; j++){
					warningHtml.append("&nbsp;");
					cc.append(" ");
				}
				cc.append("^");
				warningHtml.append("^");
				warningHtml.append("<br>");
				warningHtml.append("</span>");
				
				if (warning.getAssociatedDeclaration() != null){
					//TODO: remove leading slashes
					//TODO: we need a header for the associated declaration
					warningHtml.append(warning.getAssociatedDeclaration().getContents());
				}
				
				warningHtml.append("</p>");
			}
		}
		
		for (Integer line : lineWarnings.keySet()){
			if (line < 0 || line >= priorMethodCalls.size()){
				throw new RuntimeException("Warning line # is outside of method body");
			}
			
			if (!priorMethodCalls.get(line).isEmpty()){
				
				StringBuilder build = lineWarnings.get(line);
				
				// Report methods with missing postconditions
				
				Set<String> methodsMissingPost = Sets.newHashSet();
				for (String priorMethodCall : priorMethodCalls.get(line)){
					MethodContract contract = Util.lookupMethod(project, priorMethodCall);
					if (contract.getEnsures().isEmpty()){
						methodsMissingPost.add(priorMethodCall);
						break;
					}
					for (List<Clause> ex : contract.getExsures().values()){
						if (ex.isEmpty()){
							methodsMissingPost.add(priorMethodCall);
							break;
						}
					}					
				}
				
				if (!methodsMissingPost.isEmpty()){
					build.append("<p><b>HINT: the following methods are missing postconditions</b>. Fix this by opening " +
							"the documentation for the method and clicking the \"Add\" button.");
					build.append("<ul>");
					for (String methodMissingPost : methodsMissingPost){
						build.append("<li>" + methodMissingPost + "</li>");
					}
					build.append("</ul></p>");
				}
					
				// Report other methods that are called before the warning
				
				Set<String> otherPriorMethods = Sets.newHashSet(priorMethodCalls.get(line));
				otherPriorMethods.removeAll(methodsMissingPost);
				
				if (!otherPriorMethods.isEmpty()){
					build.append("<p><b>HINT: Check the postconditions for these methods:</b>");
					build.append("<ul>");
					for (String sig : otherPriorMethods){
						build.append("<li>" + sig + "</li>");
					}
					build.append("</ul></p>");
				}
			}
			
			String warning = lineWarnings.get(line).toString();
			
			int warningId = -1; 
			
			if (warningToId.containsKey(warning)){
				warningId = warningToId.get(warning);
			}else{
				warningId = warningToId.size() + 1;
				warningToId.put(warning, warningId);
				idToWarning.put(warningId,warning);
				idToWarningChunks.put(warningId, lineChunks.get(line));
			}
			
			int leadingWhitespace = lines[line].length() - lines[line].replaceAll("^\\s+", "").length();
			
			String start = "[[ERR onmouseover=\"showWarning(".concat(Integer.toString(warningId)).concat(")\"")
				.concat(" onmouseout=\"cancelWarning(").concat(Integer.toString(warningId)).concat(")\" ERR]]");
			
			lines[line] = lines[line].substring(0, leadingWhitespace)
				.concat(start)
				.concat(lines[line].substring(leadingWhitespace))
				.concat("[[ERR]]");
		
			int idx = 0;
			while ( (idx = lines[line].indexOf("[[DOCS",idx)) >= 0){
				lines[line] = TextUtil.insert(lines[line], idx, "[[ERR]]");
				idx+= "[[ERR]]".length() + 1;
			}
			
			idx = 0;
			while ( (idx = lines[line].indexOf("[[DOC]]",idx)) >= 0){
				lines[line] = TextUtil.insert(lines[line], idx + "[[DOC]]".length(), start);
				idx+= start.length() + 1;
			}
			
			lines[line] = lines[line].replaceAll("\\[WARNINGID\\]", String.valueOf(warningId));
			
		}
		
		String errorBody = Joiner.on(LINE_SEPARATOR).join(lines);
		errorBody = errorBody.replaceAll("\\[WARNINGID\\]", "-1");
		return errorBody;
	}
	
	/**
	 * Predicate accepting methods with exsures specifications (even empty specifications).
	 */
	public static final Predicate<MethodContract> HAS_EXSURES = new Predicate<MethodContract>(){
		@Override
		public boolean apply(MethodContract method) {
			return !method.getExsures().isEmpty();
		}
	};
	
	/**
	 * Find method with signature <code>query</code> in <code>project</code>, or return null
	 * @param project the project
	 * @param query the query signature
	 * @return the method with signature <code>query</code> in <code>project</code>, or return null
	 */
	public static MethodContract lookupMethod(ProjectSpecification project, HasQualifiedSignature query){
		return lookupMethod(project, query.qualifiedSignature());
	}

	/**
	 * Find method with signature <code>query</code> in <code>project</code>, or return null
	 * @param project the project
	 * @param query the query signature
	 * @return the method with signature <code>query</code> in <code>project</code>, or return null
	 */
	public static MethodContract lookupMethod(ProjectSpecification project, String query){	
		for (MethodContract method : allMethods(project)){
			if (method.getSignature().equals(query)){
				return method;
			}
		}
		return null;
	}
	
	/**
	 * Find method in <code>type</code> with simple name <code>simpleName</code>, or return null
	 * @param type the type
	 * @param simpleName the query
	 * @return the method in <code>type</code> with simple name <code>simpleName</code>, or return null
	 */
	public static MethodContract lookupMethod(TypeSpecification type, String simpleName){
		for (MethodContract method : type.getMethods()){
			if (method.getSignature().contains(simpleName + "(")){
				return method;
			}
		}
		return null;
	}
	
	/**
	 * Find the method result in <code>typeResult</code> for <code>method</code>
	 * @param typeResult the type result to search
	 * @param method the query method
	 * @return the method result in <code>typeResult</code> for <code>method</code>
	 * @throws NoSuchElementException
	 */
	public static MethodResult lookupMethodResult(TypeResult typeResult, MethodContract method){
		String query = method.getSignature().replaceAll(OBJECT_ARRAY_REGEXP, QUALIFIED_OBJECT_ARRAY);
		
		for (MethodResult result : typeResult.getMethodResults()){
			String qualifiedResultSig = (result.getType() + "." + result.getSignature());
			if (qualifiedResultSig.equals(query)){
				return result;
			}
		}
		throw new NoSuchElementException("Cannot find method " + query + " in result for type " + typeResult.getName());
	}
		
	
	/**
	 * Markup the method body with documentation tags
	 * @param bindings map of Java element bindings
	 * @param method method definition
	 * @param allMethods all method definitions
	 * @return the annotated method body
	 */
	protected static String addDocumentationTags(DefinitionMap bindings, MethodContract method, List<MethodContract> allMethods){
		String cu = Util.getCompilationUnit(method);
		Map<SourceElement, ElementDefinition> bindingMap = bindings.getMap(cu);
		
		String annotatedBody = method.getInfo().getBody();
		
		// Find interesting source locations in the bindings map
		List<SourceElement> relevant = Lists.newArrayList();
		if (bindingMap != null){
			for (SourceElement element : bindingMap.keySet()){
				if (TextUtil.contains(method.getInfo().getLocation(), element.getLocation())){
					relevant.add(element);
				}
			}
		}
		
		//Sort in REVERSE order by offset
		Collections.sort(relevant, new Comparator<SourceElement>(){
			@Override
			public int compare(SourceElement lhs, SourceElement rhs) {
				int lhsOffset = lhs.getLocation().getOffset();
				int rhsOffset = rhs.getLocation().getOffset();
				return - Integer.valueOf(lhsOffset).compareTo(rhsOffset);
			}
		});
		
		// Iterate in reverse order so that inserts preserve location
		for (final SourceElement element : relevant){
			
			// try to find the method definition
			final ElementDefinition binding = bindingMap.get(element);
			MethodContract methodBinding = null;
			if (binding.getLocation() != null){
				methodBinding = Iterables.find(allMethods, new Predicate<MethodContract>(){
					@Override
					public boolean apply(MethodContract otherMethod) {
						return otherMethod.getInfo().getLocation().equals(binding.getLocation());
					}
				} , null);
			}
			
			if (methodBinding != null && method.qualifiedSignature().equals(methodBinding.qualifiedSignature())){
				// don't tag the constructor itself when annotating the constructor
				continue;
			}
			
			String javaDoc = binding.getDocHtml() != null 
					? binding.getDocHtml() 
					: (methodBinding != null ? methodBinding.qualifiedSignature() : null); //Use signature for methods without documentation


			if (javaDoc != null){
				//Add [[DOC]] tag to end of region
				annotatedBody = TextUtil.insert(annotatedBody, relativeOffset(method, TextUtil.getEnd(element.getLocation())), "[[DOC]]");
				
				int docId = getJavaDocId(javaDoc, methodBinding);

				String tag = null;
				if (idToSig.containsKey(docId) // documentation is for a method
					&& !idToSig.get(docId).contains("Check.")){ // not a client "check" method

					// add a toggle & tag with documentation
					long preId = nextSpanId++;
					long postId = nextSpanId++;

					String toggle = "onclick=\"toggleDoc(" + docId + "," + preId + "," + postId + ",[WARNINGID])\"";

					//use postId because it is unique
					tag = "[[DOCST onmouseover=\"showDoc(" + docId + ")\" onmouseout=\"cancelDoc(" + docId + ")\" " + toggle + "  id=\"doc" + postId + "\" DOC]]";
				}else{
					tag = "[[DOCSR onmouseover=\"showDoc(" + docId + ")\" onmouseout=\"cancelDoc(" + docId + ")\" DOC]]";
				}
				
				annotatedBody = TextUtil.insert(annotatedBody, relativeOffset(method, element.getLocation().getOffset()), tag);
				//TODO: the function name should be handled by CodeView	
			}
		}
		
		return annotatedBody;
	}
	
	/**
	 * Get the id for documentation <code>javaDoc</code> bound to method <code>methodBinding</code>.
	 * Update the caches accordingly.
	 * @param javaDoc the JavaDoc HTML
	 * @param methodBinding method binding, or null, if the JavaDoc does not correspond to a method
	 * @return the id for documentation <code>javaDoc</code> bound to method <code>methodBinding</code>
	 */
	private static int getJavaDocId(String javaDoc, MethodContract methodBinding){
		JavadocKey key = new JavadocKey(javaDoc, methodBinding);
		
		if (docToId.containsKey(key)){
			return docToId.get(key);
		}else{
			int docId = docToId.size() + 1;
			docToId.put(key, docId);
			idToDoc.put(docId, javaDoc);
			
			if (methodBinding != null){
				idToSig.put(docId, methodBinding.getSignature());
			}
			return docId;
		}
	}
	
	/**
	 * Add documentation, errors, and slots to the method source
	 * @param descriptor project descriptor
	 * @param project project specification
	 * @param method the method
	 * @param result the project ESC/Java2 result
	 * @param typeResult the type ESC/Java2 result
	 * @return annotated method source
	 */
	protected static String annotateBody(
			ProjectDescriptor descriptor, 
			ProjectSpecification project, 
			MethodContract method, 
			ProjectResult result, 
			TypeResult typeResult){
	
		String cu = Util.getCompilationUnit(method);
		MethodResult methodResult = Util.lookupMethodResult(typeResult, method);
		
		String documentedBody = Util.addDocumentationTags(descriptor.getDefMap(), method, Util.allMethods(project));
		
		String errBody = Util.markMethodWarnings(
				methodResult.getWarnings(), 
				result.getLineMaps().get(cu),
				TextUtil.lineForOffset(descriptor.getLineOffsets().get(cu), method.getInfo().getLocation().getOffset()), 
				documentedBody,
				EscJUtil.requiresExFilter,
				project);
		
		String slottedBody = Util.addSlots(errBody);
		
		return slottedBody;
	}
	
	/**
	 * true iff any of <code>signatures</code> is a constructor signature
	 * @param signatures method signatures
	 * @return true iff any of <code>signatures</code> is a constructor signature
	 */
	protected static boolean anyCtorSignature(String... signatures){
		for (String signature : signatures){
			if (isCtor(signature)){
				return true;
			}
		}
		
		return false;
	}

	/**
	 * true iff <code>method</code> is a constructor
	 * @param method the method
	 * @return true iff <code>method</code> is a constructor
	 */
	public static boolean isCtor(HasQualifiedSignature method){
		return isCtor(method.qualifiedSignature());
	}
	
	public static JmlSpan parseSpan(String jmlStatement) throws JmlParseException{
		// XXX document method
		try {
			return SpanParser.build(ExecuteJml.tryParse(SpecUtil.clean(jmlStatement)));
		} catch (RecognitionException e) {
			throw new JmlParseException(e);
		} catch (TokenStreamException e) {
			throw new JmlParseException(e);
		}
	}
	
	/**
	 * Returns <code>true</code> iff <code>signature</code> is a constructor signature
	 * @param signature the method signature
	 * @return <code>true</code> iff <code>signature</code> is a constructor signature
	 */
	private static boolean isCtor(String signature){
		String qualifiedName = signature.substring(0, signature.indexOf('('));
		
		int simpleNameSeparator = qualifiedName.lastIndexOf('.');
		String simpleName = qualifiedName.substring(simpleNameSeparator + 1);
		
		int typeNameSeparator = qualifiedName.lastIndexOf('.', simpleNameSeparator - 1);
		String typeName = qualifiedName.substring(typeNameSeparator + 1, simpleNameSeparator);
		
		return simpleName.equals(typeName);
	}
	
	/**
	 * Returns <code>true</code> iff <code>method</code> is pure
	 * @param method the method
	 * @return <code>true</code> iff <code>method</code> is pure
	 * @deprecated purity should be recorded in the method specification
	 */
	public static boolean isPure(HasQualifiedSignature method){
		List<String> pureMethods = Lists.newArrayList(
				"StackAr.isEmpty()", "StackAr.isFull()", "StackAr.top()", 
				"FixedSizeSet.contains(int)", "FixedSizeSet.similar(FixedSizeSet)",
				"Order.isSell()", "Order.getNumShares()", "Order.getCusip()", 
				"OrderBook.getCusip()", 
				"OrderQueue.isEmpty()", "OrderQueue.isFull()", "OrderQueue.getFront()",
				"Transaction.getTid()", "Transaction.getBuyOrder()", "Transaction.getSellOrder()",
				"QueueAr.isEmpty()", "QueueAr.isFull()", "OrderQueue.getFront()"
				);
		
		return pureMethods.contains(method.qualifiedSignature());
	}
	
	
	private static class JavadocKey{
		private String javadoc;
		private String signature;
		
		private JavadocKey(String javadoc, HasQualifiedSignature signature) {
			super();
			this.javadoc = javadoc;
			this.signature = signature != null ? signature.qualifiedSignature() : null;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((javadoc == null) ? 0 : javadoc.hashCode());
			result = prime * result
					+ ((signature == null) ? 0 : signature.hashCode());
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
			JavadocKey other = (JavadocKey) obj;
			if (javadoc == null) {
				if (other.javadoc != null)
					return false;
			} else if (!javadoc.equals(other.javadoc))
				return false;
			if (signature == null) {
				if (other.signature != null)
					return false;
			} else if (!signature.equals(other.signature))
				return false;
			return true;
		}
	}
	
	
}
