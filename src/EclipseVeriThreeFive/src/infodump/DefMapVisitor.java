package infodump;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.internal.ui.text.javadoc.JavadocContentAccess2;

import com.schiller.veriasa.web.shared.DefMap;
import com.schiller.veriasa.web.shared.SrcLoc;
import com.schiller.veriasa.web.shared.DefMap.DestElement;
import com.schiller.veriasa.web.shared.DefMap.SrcElement;

@SuppressWarnings("restriction")
public class DefMapVisitor extends ASTVisitor {

	private ICompilationUnit cu;
	private DefMap map;
	
	public DefMapVisitor(ICompilationUnit cu, DefMap map){
		super(false);
		this.cu = cu;
		this.map = map;
	}
	
	private ICompilationUnit getCompilationUnit(IJavaElement elt){
		IJavaElement search = elt;
		
		do{
			if (search.getElementType() == IJavaElement.COMPILATION_UNIT){
				return (ICompilationUnit) search;	
			}
			search = search.getParent();
		}while(search != null);
		
		return null;
	}
	
	private SrcLoc getDestLoc(IJavaElement dest){
		if (dest instanceof ISourceReference){
			ICompilationUnit pu = getCompilationUnit(dest);
			
			if (pu == null){
				return null;
			}
			
			ISourceReference sr = (ISourceReference) dest;
			
			ISourceRange rng = null;
			
			try {
				rng = sr.getSourceRange();
			} catch (JavaModelException e) {
				return null;
			}
			
			if (rng == null){
				return null;
			}else{
				return new SrcLoc(pu.getElementName(),
					rng.getOffset(),
					rng.getLength());
			}
		}
		return null;
	}
	
	
	private String getDocHtml(IJavaElement dest){
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
	
	@Override
	public boolean visit(QualifiedType node){
		final SrcLoc loc = new SrcLoc(cu.getElementName(), node.getStartPosition(), node.getLength());
		SrcElement srcElt = null;
		try {
			srcElt = new SrcElement(
					cu.getSource().substring(node.getStartPosition(),node.getStartPosition() + node.getLength()),
					loc);
		} catch (JavaModelException e) {
			throw new RuntimeException("Error getting qualified type");
		}
		
		IJavaElement dest = node.resolveBinding().getJavaElement();
		String docHtml = getDocHtml(dest);
		if (docHtml != null){
			map.addMapping(cu.getElementName(), srcElt, new DestElement(getDestLoc(dest),docHtml));	
		}
		return false;
	}
	
	@Override
	public boolean visit(QualifiedName node){
		final SrcLoc loc = new SrcLoc(cu.getElementName(), node.getStartPosition(), node.getLength());
		SrcElement srcElt = null;
		try {
			srcElt = new SrcElement(
					cu.getSource().substring(node.getStartPosition(),node.getStartPosition() + node.getLength()),
					loc);
		} catch (JavaModelException e) {
			throw new RuntimeException("Error getting qualified type");
		}
		
		IJavaElement dest = node.resolveBinding().getJavaElement();
		String docHtml = getDocHtml(dest);
		if (docHtml != null){
			map.addMapping(cu.getElementName(), srcElt, new DestElement(getDestLoc(dest),docHtml));	
		}
		return false;
	}
	
	@Override
	public boolean visit(SimpleName node){
		if (!node.isDeclaration()){
			
			final SrcLoc loc = new SrcLoc(cu.getElementName(), node.getStartPosition(), node.getLength());
			SrcElement srcElt = null;
			try {
				srcElt = new SrcElement(
					cu.getSource().substring(node.getStartPosition(),node.getStartPosition() + node.getLength()),
					loc);
			} catch (JavaModelException e) {
				throw new RuntimeException("Error getting qualified type");
			}
		
			IJavaElement dest = node.resolveBinding().getJavaElement();
			String docHtml = getDocHtml(dest);
			if (docHtml != null){
				map.addMapping(cu.getElementName(), srcElt, new DestElement(getDestLoc(dest),docHtml));	
			}
		}
		return false;
	}
	
	
	
}
