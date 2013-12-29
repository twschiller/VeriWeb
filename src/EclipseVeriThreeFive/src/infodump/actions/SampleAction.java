package infodump.actions;

import infodump.DefMapUtil;

import java.io.FileNotFoundException;
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
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import com.schiller.veriasa.web.shared.ElementInfo;
import com.schiller.veriasa.web.shared.FieldSpec;
import com.schiller.veriasa.web.shared.FunctionDoc;
import com.schiller.veriasa.web.shared.FunctionSpec;
import com.schiller.veriasa.web.shared.ProjectSpec;
import com.schiller.veriasa.web.shared.Spec;
import com.schiller.veriasa.web.shared.SrcLoc;
import com.schiller.veriasa.web.shared.TypeSpec;
import com.schiller.veriasa.web.shared.ElementDoc.FormatType;
import com.schiller.veriasa.web.shared.ElementInfo.LanguageType;

/**
 * Our sample action implements workbench action delegate.
 * The action proxy will be created by the workbench and
 * shown in the UI. When the user tries to use the action,
 * this delegate will be created and execution will be 
 * delegated to it.
 * @see IWorkbenchWindowActionDelegate
 */
public class SampleAction implements IWorkbenchWindowActionDelegate {
	private IWorkbenchWindow window;
	/**
	 * The constructor.
	 */
	public SampleAction() {
	}

	private List<TypeSpec> createProblems(IJavaProject javaProject) throws JavaModelException{
		List<TypeSpec> ts = new ArrayList<TypeSpec>();
		
		for (IPackageFragment f : javaProject.getPackageFragments()){
			for (ICompilationUnit cu : f.getCompilationUnits()){
				for (IType t : cu.getTypes()){
					ts.add(createProblem(t));
				}
			}
		}
		return ts;
	}
	
	
	private static String qualifyName(String name){
		
		if (name.equals("Object")){
			return "java.lang.Object";
		}else{
			return name;
		}
	}
	
	
	private TypeSpec createProblem(IType t) throws JavaModelException{
		
		List<FunctionSpec> functions = new ArrayList<FunctionSpec>();
		List<FieldSpec> fields = new ArrayList<FieldSpec>();
		
		for (IField f : t.getFields()){
			
			SrcLoc loc = new SrcLoc(t.getCompilationUnit().getElementName(),
					f.getSourceRange().getLength(),
					f.getSourceRange().getOffset());
			
			fields.add(new FieldSpec(f.getElementName(), loc, new LinkedList<Spec>()));
			
		}
		
		for (IMethod m : t.getMethods()){
			ElementInfo info;
			
			String doc = null;
			ISourceRange dr = m.getJavadocRange();
			
			
			if (dr != null){
				doc = m.getCompilationUnit().getSource().substring(dr.getOffset(),dr.getOffset() + dr.getLength());
			}
			
			try {
				info = new ElementInfo(
						DefMapUtil.GetLoc(t.getCompilationUnit(), m),
						new FunctionDoc(doc, FormatType.JAVADOC),
						m.getSource(),
						LanguageType.JAVA);
			} catch (ParseException e) {
				//TODO: Handle this properly
				throw new RuntimeException("Invalid javadoc for method " + m.getElementName());
			} 
						
			
			String sig = m.getDeclaringType().getFullyQualifiedName() + "." + m.getElementName() + "(";
			
			String comma = "";
			
			String[] parameterTypes = m.getParameterTypes();
            for (int i=0; i<m.getParameterTypes().length; ++i) {
            	 sig += comma;
            	 //TODO: Need to resolve the name to get the real qualified name
            	 sig += qualifyName(Signature.toString(parameterTypes[i]));
            	 comma = ", ";
            }
			sig += ")";
			Signature.toString(m.getSignature());
			
			//Get the simple names for the declared exceptions
			String [] exTypesRaw = m.getExceptionTypes();
			List<String> exTypes = new LinkedList<String>();
			for (String raw : exTypesRaw){
				exTypes.add(Signature.getSignatureSimpleName(raw));
			}
			
			//TODO: should not assume that all methods are public
			functions.add(new FunctionSpec(sig,Arrays.asList(m.getParameterNames()),true,info,exTypes));		   		
		}
		
		
		SrcLoc tloc = new SrcLoc(t.getCompilationUnit().getElementName(),
				t.getSourceRange().getOffset(),
				t.getSourceRange().getLength());
		
		return new TypeSpec(t.getFullyQualifiedName(), tloc, new LinkedList<Spec>(), fields, functions);
	}
	
	

	/**
	 * The action has been activated. The argument of the
	 * method represents the 'real' action sitting
	 * in the workbench UI.
	 * @see IWorkbenchWindowActionDelegate#run
	 */
	public void run(IAction action) {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
	
		for (IProject p : root.getProjects()){
			MessageDialog.openInformation(
					window.getShell(),
					"Veri ASA",
					"Creating Veri ASA Project for " + p.getName());
			
		
			IJavaProject javaProject = JavaCore.create(p);
			
			List<TypeSpec> ts = new ArrayList<TypeSpec>();
			
			try {
				ts = createProblems(javaProject);
			} catch (JavaModelException e) {
				System.err.println("Internal Error: failed to create sub-problems");
			}
			
			ProjectSpec pSpec = new ProjectSpec(p.getName(),ts);
			
			try {
				
				
				ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("/home/tws/" + p.getName() + ".asa"));
				oos.writeObject(pSpec);
				oos.close();
				
				
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
			MessageDialog.openInformation(
					window.getShell(),
					"Veri ASA",
					"Finished creating Veri ASA Project for " + p.getName());
		}
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