package infodump;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.internal.ui.text.javadoc.JavadocContentAccess2;

import com.schiller.veriasa.web.shared.intelli.FieldEdge;
import com.schiller.veriasa.web.shared.intelli.IntelliEdge;
import com.schiller.veriasa.web.shared.intelli.IntelliMap;
import com.schiller.veriasa.web.shared.intelli.IntelliNode;
import com.schiller.veriasa.web.shared.intelli.MethodEdge;

@SuppressWarnings("restriction")
public class IntelliMapVisitor extends ASTVisitor{
	
	/**
	 * the underlying map
	 */
	private IntelliMap map;
	
	
	private static PrintWriter log = null; 
	
	/**
	 * Create a IntelliMapVisitor that populates the given map
	 * @param map the map to populate
	 */
	public IntelliMapVisitor(IntelliMap map){
		this.map = map;
		if (log == null){
			 try {
				log = new PrintWriter(new BufferedWriter(new FileWriter("/home/tws/a.log")));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Retrieve the documentation attached to an element
	 * @param the element
	 * @return the attached javadoc (or null)
	 */
	private static String getDocHtml(IJavaElement dest){
		if (dest instanceof IMember){
			IMember member = (IMember) dest;
			try {
				return JavadocContentAccess2.getHTMLContent(member, true);
			} catch (JavaModelException e) {
				return null;
			}
		}else{
			return null;
		}
	}
	
	
	
	public IntelliNode genNode(TypeDeclaration type){
		String fqn = type.getName().getFullyQualifiedName();
	
		
		
		if (map.isMapped(fqn)){
			return map.getIntelliNode(fqn);
		}
		
		if (log != null){
			log.println("create node for " + type.getName());
			log.flush();
		}
		
		//Add it to the mapping to avoid infinite recursion
		IntelliNode in = new IntelliNode(fqn,getDocHtml(type.resolveBinding().getJavaElement()));
		map.addMapping(fqn, in);
		
		Map<IntelliEdge, IntelliNode> children = new HashMap<IntelliEdge, IntelliNode> ();
		
		for (MethodDeclaration method : type.getMethods()){
			if ((Modifier.STATIC & method.getModifiers()) > 0){
				continue;
			}
			
		
			IJavaElement elt = method.resolveBinding().getJavaElement();
		
			if (method.getReturnType2() != null && method.getReturnType2().isWildcardType()){
				continue;
			}
			
			if (method.getReturnType2() != null && method.getReturnType2().resolveBinding().getName().startsWith("Reference")){
				continue;
			}
			
			IntelliNode child = doMethod(method);
		
			children.put(new MethodEdge(method.getName().getIdentifier(),
					method.parameters().size(),getDocHtml(elt)), child);
		}
		
		for (FieldDeclaration field : type.getFields()){
			if ((Modifier.STATIC & field.getModifiers()) > 0){
				continue;
			}
			
			if (field.getType().isWildcardType()){
				continue;
			}
		
			if (field.getType().resolveBinding().getName().startsWith("Reference")){
				continue;
			}
			
			IntelliNode child = doField(field);
			
			VariableDeclarationFragment f = (VariableDeclarationFragment) field.fragments().get(0);
			children.put(new FieldEdge(f.getName().getIdentifier(), null, field.getType().isArrayType()), child);
		}
		
		in.setChildren(children);
	
		return in;
	}
	
	public IntelliNode genNode(ITypeBinding type){
		if (type.isGenericType() || type.isParameterizedType()){
			log.println("erase " + type);
			type = type.getErasure();
			
		}
		
		String fqn = type.getQualifiedName();
		
		if (map.isMapped(fqn)){
			return map.getIntelliNode(fqn);
		}
		
		if (log != null){
			log.println("create node for " + type.getName());
			log.flush();
		}
		
		//Add it to the mapping to avoid infinite recursion
		IntelliNode in = new IntelliNode(fqn,getDocHtml(type.getJavaElement()));
		map.addMapping(fqn, in);
		
		Map<IntelliEdge, IntelliNode> children = new HashMap<IntelliEdge, IntelliNode> ();
		
		
		for (IMethodBinding method : type.getDeclaredMethods()){
			if ((Modifier.STATIC & method.getModifiers()) > 0){
				continue;
			}
			
			if (method.getReturnType().isWildcardType()){
				continue;
			}
			

			if (method.getReturnType().getName().startsWith("Reference")){
				continue;
			}
			
			IJavaElement elt = method.getJavaElement();
			IntelliNode child = doMethod(method);
			
			children.put(new MethodEdge(method.getName(),
					method.getParameterTypes().length,getDocHtml(elt)), child);
		
		}
		
		for (IVariableBinding field : type.getDeclaredFields()){
			if ((Modifier.STATIC & field.getModifiers()) > 0){
				continue;
			}
			
			if (field.getType().isWildcardType()){
				continue;
			}
			
			if (field.getType().getName().startsWith("Reference")){
				continue;
			}
			
			IJavaElement elt = field.getJavaElement();
			IntelliNode child = doField(field);
			
			children.put(new FieldEdge(field.getName(),getDocHtml(elt),field.getType().isArray()), child);
		}
		in.setChildren(children);
		return in;
	}
	
	public IntelliNode doField(FieldDeclaration field){
		
		
		if (field.getType() == null || field.getType().isPrimitiveType()){
			String name = field.getType() == null ? 
					"null" : 
					field.getType().resolveBinding().getQualifiedName();
			
			if (!map.isMapped(name)){
				IntelliNode in = new IntelliNode(name,"");
				map.addMapping(name, in);
				return in;	
			}else{
				return map.getIntelliNode(name);
			}
		}else{
			return genNode(field.getType().resolveBinding());
		}
	}
	
	
	public IntelliNode doField(IVariableBinding field){
		if (field.getType().isNullType() || field.getType().isPrimitive()){
			String name = field.getType().getQualifiedName();
			if (!map.isMapped(name)){
				IntelliNode in = new IntelliNode(name,"");
				map.addMapping(name, in);
				return in;	
			}else{
				return map.getIntelliNode(name);
			}
		}else{
			return genNode(field.getType());
		}
	}
	
	public IntelliNode doMethod(IMethodBinding method){
		if(method.getReturnType().isNullType() || method.getReturnType().isPrimitive()){
			String name = method.getReturnType().getQualifiedName();
			if (!map.isMapped(name)){
				IntelliNode in = new IntelliNode(name,"");
				map.addMapping(name, in);
				return in;	
			}else{
				return map.getIntelliNode(name);
			}
		}else{
			return genNode(method.getReturnType());
		}
	}

	
	public IntelliNode doMethod(MethodDeclaration method){
		if (method.getReturnType2() == null){
			if (!map.isMapped("null")){
				IntelliNode in = new IntelliNode("null","");
				map.addMapping("null", in);
				return in;
			}else{
				return map.getIntelliNode("null");
			}
		}else if(method.getReturnType2().isPrimitiveType()){
			String name = method.getReturnType2().resolveBinding().getQualifiedName();
			
			if (!map.isMapped(name)){
				IntelliNode in = new IntelliNode(name,"");
				map.addMapping(method.getReturnType2().resolveBinding().getQualifiedName(), in);
				return in;
			}else{
				return map.getIntelliNode(name);
			}
		}else{
			return genNode(method.getReturnType2().resolveBinding());
		}
	}
	
	
	@Override
	public boolean visit(TypeDeclaration node){
		if (log != null){
			log.println("visiting " + node.getName());
		}
		
		map.addMapping(node.getName().getFullyQualifiedName(), genNode(node));
		return true;
	}
	
	@Override
	public boolean visit(FieldDeclaration node){
		return false;
	}
	
	@Override
	public boolean visit(MethodDeclaration node){
		return false;
	}
	
}
