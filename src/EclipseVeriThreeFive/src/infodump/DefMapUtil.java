package infodump;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.JavaModelException;

import com.schiller.veriasa.web.shared.SrcLoc;


public class DefMapUtil {

	public static SrcLoc GetLoc(ICompilationUnit cu, ISourceReference r){
		try {
			return new SrcLoc(cu.getElementName(),r.getSourceRange().getOffset(), r.getSourceRange().getLength());
		} catch (JavaModelException e) {
			throw new RuntimeException("Internal Error: could not get source location");
		}
	}	
}
