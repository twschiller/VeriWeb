package edu.washington.cs.plse.verieclipse.actions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import com.schiller.veriasa.web.shared.core.Clause;
import com.schiller.veriasa.web.shared.core.ElementDocumentation.FormatType;
import com.schiller.veriasa.web.shared.core.FieldSpec;
import com.schiller.veriasa.web.shared.core.MethodContract;
import com.schiller.veriasa.web.shared.core.MethodDocumentation;
import com.schiller.veriasa.web.shared.core.ProjectSpecification;
import com.schiller.veriasa.web.shared.core.SourceElement;
import com.schiller.veriasa.web.shared.core.SourceElement.LanguageType;
import com.schiller.veriasa.web.shared.core.SourceLocation;
import com.schiller.veriasa.web.shared.core.TypeSpecification;

import edu.washington.cs.plse.verieclipse.Activator;

public class GenerateSubproblemsAction implements IWorkbenchWindowActionDelegate {
	private IWorkbenchWindow window;

	public GenerateSubproblemsAction() {
	}
	
	public static SourceLocation location(ICompilationUnit cu, ISourceReference ref){
		try {
			return new SourceLocation(cu.getElementName(), ref.getSourceRange().getOffset(), ref.getSourceRange().getLength());
		} catch (JavaModelException e) {
			throw new RuntimeException("Error retrieving source location from compilation unit " + cu.getElementName(), e);
		}
	}	

	private List<TypeSpecification> createProblems(IJavaProject javaProject, IProgressMonitor monitor) throws JavaModelException{
		List<TypeSpecification> types = new ArrayList<TypeSpecification>();
		
		int totalWork = 0;
		for (IPackageFragment pack : javaProject.getPackageFragments()){
			for (ICompilationUnit cu : pack.getCompilationUnits()){
				for (@SuppressWarnings("unused") IType type : cu.getTypes()){
					totalWork++;
				}
			}
		}
		
		monitor.beginTask("Creating sub-problems for project " + javaProject.getProject().getName(), totalWork);
		
		for (IPackageFragment pack : javaProject.getPackageFragments()){
			for (ICompilationUnit cu : pack.getCompilationUnits()){
				for (IType type : cu.getTypes()){
					types.add(createProblem(type));
					monitor.worked(1);
				}
			}
		}
		
		return types;
	}
	
	private static String qualifyName(String name){
		return name.equals("Object") ? "java.lang.Object" : name;
	}
	
	private TypeSpecification createProblem(IType type) throws JavaModelException{
		
		List<MethodContract> methods = new ArrayList<MethodContract>();
		List<FieldSpec> fields = new ArrayList<FieldSpec>();
		
		for (IField field : type.getFields()){
			SourceLocation loc = new SourceLocation(
					type.getCompilationUnit().getElementName(),
					field.getSourceRange().getLength(),
					field.getSourceRange().getOffset());
			
			fields.add(new FieldSpec(field.getElementName(), loc));
		}
		
		for (IMethod method : type.getMethods()){
			SourceElement info;
			
			String doc = null;
			ISourceRange dr = method.getJavadocRange();
			
			if (dr != null){
				doc = method.getCompilationUnit().getSource().substring(dr.getOffset(), dr.getOffset() + dr.getLength());
			}
			
			try {
				info = new SourceElement(
						location(type.getCompilationUnit(), method),
						new MethodDocumentation(doc, FormatType.JAVADOC),
						method.getSource(),
						LanguageType.JAVA);
			} catch (ParseException e) {
				// TODO Handle this properly
				throw new RuntimeException("Invalid javadoc for method " + method.getElementName(), e);
			} 
						
			String sig = method.getDeclaringType().getFullyQualifiedName() + "." + method.getElementName() + "(";
			
			String comma = "";
			
			String[] parameterTypes = method.getParameterTypes();
            for (int i=0; i<method.getParameterTypes().length; ++i) {
            	 sig += comma;
            	 //TODO: Need to resolve the name to get the real qualified name
            	 sig += qualifyName(Signature.toString(parameterTypes[i]));
            	 comma = ", ";
            }
			sig += ")";
			Signature.toString(method.getSignature());
			
			//Get the simple names for the declared exceptions
			String [] exTypesRaw = method.getExceptionTypes();
			List<String> exTypes = new LinkedList<String>();
			for (String raw : exTypesRaw){
				exTypes.add(Signature.getSignatureSimpleName(raw));
			}
			
			//TODO: should not assume that all methods are public
			methods.add(new MethodContract(sig,Arrays.asList(method.getParameterNames()), true, info,exTypes));		   		
		}
		
		SourceLocation tloc = new SourceLocation(
				type.getCompilationUnit().getElementName(),
				type.getSourceRange().getOffset(),
				type.getSourceRange().getLength());
		
		return new TypeSpecification(type.getFullyQualifiedName(), tloc, new LinkedList<Clause>(), fields, methods);
	}
	
	

	/**
	 * The action has been activated. The argument of the
	 * method represents the 'real' action sitting
	 * in the workbench UI.
	 * @see IWorkbenchWindowActionDelegate#run
	 */
	public void run(IAction action) {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
	
		for (final IProject project : root.getProjects()){
			
			ProgressMonitorDialog dialog = new ProgressMonitorDialog(window.getShell()); 
			try {
				dialog.run(true, true, new IRunnableWithProgress(){ 
				    public void run(IProgressMonitor monitor) { 
				        try{
				        	createProjectSpecification(project, monitor);
				        }catch(Exception e){
				        	throw new RuntimeException(e);
				        }finally{
				        	monitor.done();
				        }
				    } 
				});
			} catch (Exception e){ 
				MessageDialog.openError(
						window.getShell(), 
						"VeriEclipse Error", 
						"Error creating sub-problems for " + project.getName() + "." + (Activator.innerMessage(e) == null ? "" : " Message: " + Activator.innerMessage(e)));
			}
		}
	}

	private void createProjectSpecification(IProject project, IProgressMonitor monitor) throws JavaModelException, IOException{
		IJavaProject javaProject = JavaCore.create(project);
		List<TypeSpecification> ts = new ArrayList<TypeSpecification>();
		
		ts = createProblems(javaProject, monitor);
	
		ProjectSpecification pSpec = new ProjectSpecification(project.getName(),ts);
		
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(Activator.makeOutputDirectory(),project.getName() + ".asa")));
		oos.writeObject(pSpec);
		oos.close();	
	}
	
	
	/**
	 * Selection in the workbench has been changed. We 
	 * can change the state of the 'real' action here
	 * if we want, but this can only happen after 
	 * the delegate has been created.
	 * @see IWorkbenchWindowActionDelegate#selectionChanged
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}

	/**
	 * We can use this method to dispose of any system
	 * resources we previously allocated.
	 * @see IWorkbenchWindowActionDelegate#dispose
	 */
	public void dispose() {
	}

	/**
	 * We will cache window object in order to
	 * be able to provide parent shell for the message dialog.
	 * @see IWorkbenchWindowActionDelegate#init
	 */
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}
}