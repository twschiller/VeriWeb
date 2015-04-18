package com.schiller.veriasa.web.server.escj;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.common.base.Predicate;
import com.schiller.veriasa.web.shared.core.MethodContract;
import com.schiller.veriasa.web.shared.core.ProjectSpecification;
import com.schiller.veriasa.web.shared.core.Clause;
import com.schiller.veriasa.web.shared.core.TypeSpecification;
import com.schiller.veriasa.web.shared.escj.AnnotatedFile;

/**
 * Utility methods for annotating Java files with specifications
 * @author Todd Schiller
 */
public final class AnnotateFile {

	/**
	 * Annotate a file with a JML specification
	 * @param original the original file
	 * @param specification the JML specification
	 * @param filter clause inclusion predicate
	 * @return the annotated file
	 * @throws IOException
	 */
	public static AnnotatedFile annotateJavaFile(File original, ProjectSpecification specification, Predicate<Clause> filter) throws IOException{
		AnnotatedFile result = new AnnotatedFile(readFile(original), System.getProperty("line.separator"));
		annotateJavaFile(original.getName(), result, specification, filter);
		return result;
	}
	
	/**
	 * Add type specification to a file
	 * @param original the original file
	 * @param type the type specification
	 * @param filter clause inclusion predicate
	 */
	public static void addTypeAnnotations(
			AnnotatedFile original,
			TypeSpecification type,
			Predicate<Clause> filter){
				
		List<Clause> toAdd = new ArrayList<Clause>();
		List<String> prefixes = new ArrayList<String>();
		
		for (Clause invariant : type.getInvariants()){
			if (filter.apply(invariant)){
				toAdd.add(invariant);
				prefixes.add("invariant");
			}
		}
	
		int line = original.firstLine("{");
		original.insert(line+1, prefixes, toAdd);
	}
	
	/**
	 * Add method annotations to a file
	 * @param annotated the file, annotated with type specifications
	 * @param relevantMethods the method specifications 
	 * @param filter an inclusion predicate
	 */
	public static void addMethodAnnotations(
			AnnotatedFile annotated, 
			List<MethodContract> relevantMethods,
			Predicate<Clause> filter){
		
		//Sort in reverse order by location
		//Functions appearing at the end of the compilation unit come first
		Collections.sort(relevantMethods, new Comparator<MethodContract>(){
			@Override
			public int compare(MethodContract lhs, MethodContract rhs) {
				return -((Integer) lhs.getInfo().getLocation().getOffset())
					.compareTo(rhs.getInfo().getLocation().getOffset());
			}
		});
		
		for (MethodContract method : relevantMethods){
			List<Clause> toInsert = new ArrayList<Clause>();
			List<String> prefixes = new ArrayList<String>();
			
			for (Clause clause : method.getRequires()){
				if (filter.apply(clause)){
					toInsert.add(clause);
					prefixes.add("requires");
				}
			}
			for (Clause clause : method.getEnsures()){
				if (filter.apply(clause)){
					toInsert.add(clause);
					prefixes.add("ensures");
				}
			}
			
			//TODO: do we want to sort exceptions to ensure consistent orderings?
			for (String ex : method.getExsures().keySet()){
				for (Clause clause : method.getExsures().get(ex)){
					if (filter.apply(clause)){
						toInsert.add(clause);
						prefixes.add("exsures (" + ex + ")");
					}
					
				}	
			}
			
			int offset = method.getInfo().getLocation().getOffset();	
			
			int l = annotated.lineForOffset(offset);
			int e = annotated.endOfJavaDoc(l);
			
			annotated.insert(e, prefixes, toInsert);
		}
	}
	
	/**
	 * Read a file to an array
	 * @param file the file to read
	 * @return lines in the file
	 * @throws IOException
	 */
	public static ArrayList<String> readFile(File file) throws IOException{
		BufferedReader in = new BufferedReader(new FileReader(file));
		ArrayList<String> lines = new ArrayList<String>();
		String line = null;
		
		while ((line = in.readLine()) != null){
			lines.add(line);
		}
		return lines;
	}
	
	/**
	 * Annotate a compilation with a JML specification
	 * @param compilationUnit the compilation unit name
	 * @param original the original (unannotated) file
	 * @param specification the specification
	 * @param filter JML clause inclusion predicate
	 */
	public static void annotateJavaFile(
			String compilationUnit, 
			AnnotatedFile original,
			ProjectSpecification specification,
			Predicate<Clause> filter){
		
		List<TypeSpecification> relevantTypes = new ArrayList<TypeSpecification>();
		List<MethodContract> relevantMethods = new ArrayList<MethodContract>();
		
		for (TypeSpecification type : specification.getTypeSpecs()){
			
			if (type.getLocation().getCompilationUnit().equals(compilationUnit)){
				relevantTypes.add(type);
			}
			
			for (MethodContract method : type.getMethods()){
				if (method.getInfo().getLocation().getCompilationUnit().equals(compilationUnit)){
					relevantMethods.add(method);
				}
			}
		}
		
		if (relevantTypes.size() > 1){
			// TODO Support inner classes
			throw new UnsupportedOperationException("Inner classes are not supported");
		}
		
		//assume that fields come before methods
		// TODO support classes where a method comes before a field
		
		addMethodAnnotations(original, relevantMethods, filter);
		addTypeAnnotations(original, relevantTypes.get(0), filter);
	}	
}
