package edu.washington.cs.plse.verieclipse;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.internal.ui.text.javadoc.JavadocContentAccess2;

import com.schiller.veriasa.web.shared.core.DefinitionMap;
import com.schiller.veriasa.web.shared.core.DefinitionMap.ElementDefinition;
import com.schiller.veriasa.web.shared.core.DefinitionMap.SourceElement;
import com.schiller.veriasa.web.shared.core.SourceLocation;


/**
 * Creates a map of Java source code connections by walking the AST
 * @author Todd Schiller
 */
@SuppressWarnings("restriction")
public class DefinitionMapVisitor extends ASTVisitor {

	private ICompilationUnit cu;
	private String source;
	private DefinitionMap map;
	
	public DefinitionMapVisitor(ICompilationUnit compilationUnit, DefinitionMap definitionMap){
		super(false);
		this.cu = compilationUnit;
		this.map = definitionMap;
	
		try {
			this.source = compilationUnit.getSource();
		} catch (JavaModelException e) {
			throw new RuntimeException("Error retrieving source for compilation unit " + compilationUnit.getElementName(), e);
		}
	}
	
	/**
	 * Add a mapping to the map
	 * @param compilationUnit the compilation unit containing the source element
	 * @param element the source element
	 * @param bindingLocation the location of the binding definition
	 * @param javaDocHtml the JavaDoc for the binding
	 */
	private void addMapping(ICompilationUnit compilationUnit, SourceElement element, SourceLocation bindingLocation, String javaDocHtml){
		map.addMapping(compilationUnit.getElementName(), element, new ElementDefinition(bindingLocation, javaDocHtml));	
	}
		
	/**
	 * Get the compilation unit associate with the given Java element, or null
	 * iff the element has no associated compilation unit
	 * @param element the Java element
	 * @return the compilation unit associated with the element
	 */
	private ICompilationUnit getCompilationUnit(IJavaElement element){
		IJavaElement search = element;
		do{
			if (search.getElementType() == IJavaElement.COMPILATION_UNIT){
				return (ICompilationUnit) search;	
			}
			search = search.getParent();
		}while(search != null);
		return null;
	}
	
	/**
	 * Get the Java element's location in the source, or null if the element has
	 * no associated source location
	 * @param element the Java element
	 * @return the element's location in the source
	 */
	private SourceLocation locationForElement(IJavaElement element){
		if (element instanceof ISourceReference){
			ICompilationUnit cu = getCompilationUnit(element);
			
			if (cu == null){
				return null;
			}
			
			ISourceReference sr = (ISourceReference) element;
		
			try {
				ISourceRange range = sr.getSourceRange();
				return range == null ? null : new SourceLocation(cu.getElementName(), range.getOffset(), range.getLength());	
			} catch (JavaModelException e) {
				return null;
			}
		}
		return null;
	}
	
	/**
	 * Get the JavaDoc associated with the element, or <code>null</code> if no JavaDoc is associated
	 * @param element the Java element
	 * @return the JavaDoc associated with the element, or <code>null</code> if no JavaDoc is associated
	 */
	private String getJavaDocHtml(IJavaElement element){
		if (element instanceof IMember){
			IMember member = (IMember) element;
			
			try {
				return JavadocContentAccess2.getHTMLContent(member, true);
			} catch (JavaModelException e) {
				return null;
			}
		}else{
			return null;
		}
	}
	
	@Override
	public boolean visit(QualifiedType node){
		final SourceLocation location = new SourceLocation(cu.getElementName(), node.getStartPosition(), node.getLength());
		
		SourceElement element = new SourceElement(
					source.substring(node.getStartPosition(),node.getStartPosition() + node.getLength()),
					location);
		
		IJavaElement binding = node.resolveBinding().getJavaElement();
		addMapping(cu, element, locationForElement(binding), getJavaDocHtml(binding));
		
		return false;
	}
	
	@Override
	public boolean visit(QualifiedName node){
		final SourceLocation location = new SourceLocation(cu.getElementName(), node.getStartPosition(), node.getLength());

		SourceElement element = new SourceElement(
					source.substring(node.getStartPosition(),node.getStartPosition() + node.getLength()),
					location);
		
		IJavaElement binding = node.resolveBinding().getJavaElement();
		
		addMapping(cu, element, locationForElement(binding), getJavaDocHtml(binding));	
		return false;
	}
	
	@Override
	public boolean visit(SimpleName node){
		if (!(node.isDeclaration() 
			|| node.getParent() instanceof LabeledStatement 
			|| node.getParent() instanceof ContinueStatement)){
			
			final SourceLocation location = new SourceLocation(cu.getElementName(), node.getStartPosition(), node.getLength());
			
			SourceElement element = new SourceElement(
					source.substring(node.getStartPosition(),node.getStartPosition() + node.getLength()),
					location);

			IJavaElement binding = node.resolveBinding().getJavaElement();
			
			if (node.getParent().getParent() instanceof ClassInstanceCreation
				&& node.getParent() instanceof Type
				&& binding instanceof IType){
				
				ClassInstanceCreation ctor = (ClassInstanceCreation) node.getParent().getParent();	
				
				IMethod[] methods = null;
				try{
					methods = ((IType) binding).getMethods();
				}catch (JavaModelException e){
					throw new RuntimeException("Error retrieving methods for type " + binding.getElementName(), e);
				}
				
				//try to find the method by name and number of arguments (why the # of args?)
				IMethod ctorForNode = null;
				for (IMethod method : methods){
					if (method.getElementName().equals(node.getIdentifier()) && method.getNumberOfParameters() == ctor.arguments().size()){
						ctorForNode = method;
						addMapping(cu, element, locationForElement(binding), getJavaDocHtml(ctorForNode));
						return true;
					}
				}
				
				if (binding.getElementName().equals(node.getIdentifier()) && ctor.arguments().size() == 0){
					// is default ctor
				}else{
					throw new RuntimeException("Could not locate method with name " + node.getIdentifier() + " in type " + binding.getElementName());			
				}
				
			}else{
				addMapping(cu, element, locationForElement(binding), getJavaDocHtml(binding));
			}
		}
		return false;
	}

}
