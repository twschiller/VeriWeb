package edu.washington.cs.plse.verieclipse.actions;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
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
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import com.schiller.veriasa.web.shared.core.DefinitionMap;
import com.schiller.veriasa.web.shared.intelli.IntelliMap;

import edu.washington.cs.plse.verieclipse.Activator;
import edu.washington.cs.plse.verieclipse.DefinitionMapVisitor;
import edu.washington.cs.plse.verieclipse.IntelliMapVisitor;


public class DumpInformationAction implements IWorkbenchWindowActionDelegate {
	private IWorkbenchWindow window;

	public DumpInformationAction() {
	}

	private void walk(ICompilationUnit icu, DefinitionMap map) throws JavaModelException{
		ASTParser parser = ASTParser.newParser(AST.JLS3); 
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(icu); // set source
		parser.setResolveBindings(true);
		
		CompilationUnit cu = (CompilationUnit) parser.createAST(null /* IProgressMonitor */);
		
		DefinitionMapVisitor v = new DefinitionMapVisitor(icu, map);
		
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
	
	
	public DefinitionMap generateDefinitionMap(IJavaProject project, IProgressMonitor monitor) throws JavaModelException{
		DefinitionMap map = new DefinitionMap();
		
		int totalWork = 0;
		for (IPackageFragment pack : project.getPackageFragments()){
			for (@SuppressWarnings("unused") ICompilationUnit cu : pack.getCompilationUnits()){
				totalWork++;
			}
		}
		
		monitor.beginTask("Generating definition map for " + project.getProject().getName(), totalWork);
			
		for (IPackageFragment pack : project.getPackageFragments()){
			for (ICompilationUnit cu : pack.getCompilationUnits()){
				if (monitor.isCanceled()){
					return null;
				}
				
				walk(cu, map);
				monitor.worked(1);
			}
		}
		
		return map;
	}
	
	public IntelliMap doProjectIntelli(IJavaProject project, IProgressMonitor monitor) throws JavaModelException{
		IntelliMap map = new IntelliMap();
		
		int totalWork = 0;
		for (IPackageFragment pack : project.getPackageFragments()){
			for (@SuppressWarnings("unused") ICompilationUnit cu : pack.getCompilationUnits()){
				totalWork++;
			}
		}
		
		monitor.beginTask("Creating IntelliSense map for " + project.getProject().getName(), totalWork);
		
		for (IPackageFragment pack : project.getPackageFragments()){
			for (ICompilationUnit cu : pack.getCompilationUnits()){
				if (monitor.isCanceled()){
					return null;
				}
				
				walkIntelli(cu,map);
				monitor.worked(1);
			}
		}
		
		return map;
	}
	
	
	private void generateDefinitionMapUx(final IProject project){	
		try {
			ProgressMonitorDialog dialog = new ProgressMonitorDialog(window.getShell()); 
			dialog.run(true, true, new IRunnableWithProgress(){ 
			    public void run(IProgressMonitor monitor) { 
			    	try {
			    		DefinitionMap map = generateDefinitionMap(JavaCore.create(project), monitor);
			    		
			    		if (!monitor.isCanceled()){
			    			ObjectOutputStream oos = 
									new ObjectOutputStream(new FileOutputStream(new File(Activator.makeOutputDirectory(),project.getName() + ".def")));
							oos.writeObject(map);
							oos.close();
			    		}
					} catch (JavaModelException e) {
						throw new RuntimeException(e);
					} catch (IOException e){
						throw new RuntimeException(e);		
					}
			    	finally{
						monitor.done();
					}
			    } 
			});
		} catch (Exception e) {
			MessageDialog.openError(window.getShell(), 
					"Veri ASA", 
					"Error GENERATING definition map for " + project.getName() + "." + (Activator.innerMessage(e) == null ? "" : " Message: " + Activator.innerMessage(e)));
			return;
		}
	}
	
	private void generateIntelliMapUx(final IProject project){
		
		try {
			ProgressMonitorDialog dialog = new ProgressMonitorDialog(window.getShell()); 
			dialog.run(true, true, new IRunnableWithProgress(){ 
			    public void run(IProgressMonitor monitor) { 	
			    	try {
			    		IntelliMap intelliMap = doProjectIntelli(JavaCore.create(project), monitor);
						
			    		if (!monitor.isCanceled()){
			    			ObjectOutputStream oos = 
									new ObjectOutputStream(new FileOutputStream(new File(Activator.makeOutputDirectory(),project.getName() + ".intelli")));
							oos.writeObject(intelliMap);
							oos.close();
			    		}
					} catch (JavaModelException e) {
						throw new RuntimeException(e);
					} catch (IOException e){
						throw new RuntimeException(e);
					}
			    	finally{
						monitor.done();
					}
			    } 
			});
		} catch (Exception e) {
			MessageDialog.openError(window.getShell(), 
					"Veri ASA", 
					"Error GENERATING intellisense map for " + project.getName() + "." + (Activator.innerMessage(e) == null ? "" : " Message: " + Activator.innerMessage(e)));
			return;
		}
	}
	

	public void run(IAction action) {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
	
		for (IProject project : root.getProjects()){
			generateDefinitionMapUx(project);
			generateIntelliMapUx(project);
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}

	public void dispose() {
	}

	public void init(IWorkbenchWindow window) {
		this.window = window;
	}
}