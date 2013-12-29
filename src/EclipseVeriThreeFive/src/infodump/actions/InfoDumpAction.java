package infodump.actions;

import infodump.DefMapVisitor;
import infodump.IntelliMapVisitor;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import com.schiller.veriasa.web.shared.DefMap;
import com.schiller.veriasa.web.shared.intelli.IntelliMap;

/**
 * Our sample action implements workbench action delegate.
 * The action proxy will be created by the workbench and
 * shown in the UI. When the user tries to use the action,
 * this delegate will be created and execution will be 
 * delegated to it.
 * @see IWorkbenchWindowActionDelegate
 */
public class InfoDumpAction implements IWorkbenchWindowActionDelegate {
	private IWorkbenchWindow window;
	/**
	 * The constructor.
	 */
	public InfoDumpAction() {
	}

	
	private void walk(ICompilationUnit icu, DefMap map) throws JavaModelException{
		
		
		ASTParser parser = ASTParser.newParser(AST.JLS3); 
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(icu); // set source
		parser.setResolveBindings(true);
		
		CompilationUnit cu = (CompilationUnit) parser.createAST(null /* IProgressMonitor */);
		
		DefMapVisitor v = new DefMapVisitor(icu, map);
		
		cu.accept(v);
		
	}

	private void walkIntelli(ICompilationUnit icu, IntelliMap map) throws JavaModelException{
		ASTParser parser = ASTParser.newParser(AST.JLS3); 
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(icu); // set source
		parser.setResolveBindings(true);
		
		CompilationUnit cu = (CompilationUnit) parser.createAST(null /* IProgressMonitor */);
		
		IntelliMapVisitor v = new IntelliMapVisitor(map);
		
		cu.accept(v);
		
	}
	
	
	public DefMap doProject(IJavaProject project) throws JavaModelException{
		DefMap map = new DefMap();
		
		for (IPackageFragment f : project.getPackageFragments()){
			for (ICompilationUnit cu : f.getCompilationUnits()){
				walk(cu, map);
			}
		}
		
		return map;
	}
	
	public IntelliMap doProjectIntelli(IJavaProject project) throws JavaModelException{
		IntelliMap map = new IntelliMap();
		
		for (IPackageFragment f : project.getPackageFragments()){
			for (ICompilationUnit cu : f.getCompilationUnits()){
				walkIntelli(cu,map);
			}
		}
		
		return map;
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
					"Dumping documentation for " + p.getName());
			
			IJavaProject javaProject = JavaCore.create(p);
		
			DefMap map = null;
			IntelliMap intelliMap = null;
			boolean ok = true;
			
			try {
				map = doProject(javaProject);
			} catch (JavaModelException e) {
				e.printStackTrace();
				ok = false;
			}
			try {
				intelliMap = doProjectIntelli(javaProject);
			} catch (JavaModelException e) {
				e.printStackTrace();
				ok = false;
			}
			
			if (ok){
				try{
					ObjectOutputStream oos = 
						new ObjectOutputStream(new FileOutputStream("/home/tws/" + p.getName() + ".def"));
					oos.writeObject(map);
					oos.close();
				}catch(IOException e){
					e.printStackTrace();
					MessageDialog.openError(window.getShell(), 
							"Veri ASA", 
							"Error WRITING references for " + p.getName());
				}
			}else{
				MessageDialog.openError(window.getShell(), 
						"Veri ASA", 
						"Error CREATING references for " + p.getName());
			}
			
			
			if (ok){
				try{
					ObjectOutputStream oos = 
						new ObjectOutputStream(new FileOutputStream("/home/tws/" + p.getName() + ".intelli"));
					oos.writeObject(intelliMap);
					oos.close();
				}catch(IOException e){
					e.printStackTrace();
					MessageDialog.openError(window.getShell(), 
							"Veri ASA", 
							"Error WRITING intelli map for " + p.getName());
				}
			}else{
				MessageDialog.openError(window.getShell(), 
						"Veri ASA", 
						"Error CREATING references for " + p.getName());
			}
			
			MessageDialog.openInformation(
					window.getShell(),
					"Veri ASA",
					"Done writing references for " + p.getName());
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