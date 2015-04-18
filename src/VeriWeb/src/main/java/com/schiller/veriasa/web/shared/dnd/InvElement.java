package com.schiller.veriasa.web.shared.dnd;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class InvElement implements Serializable{
	private static final long serialVersionUID = 1L;

	public enum RefType { Expression, Type, Id, BoilerPlate}

	private RefType type;
	private ArrayList<InvRef> subElements;
	
	@SuppressWarnings("unused")
	private InvElement(){
		this(new InvRef[]{}, RefType.Expression);
	}

	public InvElement(InvRef[] subElements, RefType type){
		this.subElements = new ArrayList<InvRef>();
		this.type = type;
		
		for (InvRef subElement : subElements){
			this.subElements.add(subElement);
		}
	}	
	
	public InvElement duplicate() {
		List<InvRef> elts = new LinkedList<InvRef>();
		
		for (InvRef r : subElements){
			elts.add(r.duplicate());
		}
		
		return new InvElement(elts.toArray(new InvRef[] {}), type);	
	}

	public List<InvRef> getNotFixed(){
		LinkedList<InvRef> res =  new LinkedList<InvRef>();

		for (InvRef r : subElements){
			if (r.getRefType().equals(RefType.Expression)){
				if (!r.isHole() && r.getValue() instanceof InvFixed){
					continue;
				}else if (!r.isHole() && r.getValue() instanceof InvLocal && !((InvLocal) r.getValue()).hasHole()){
					continue;
				}
				
				res.add(r);
			}
		}

		return res;
	}
	
	public boolean isBinary(){
		return numNotFixed(true) == 2;
	}
	
	public void swap(){
		if (!isBinary()){
			throw new RuntimeException("Element must be binary");
		}
		
		List<InvRef> elts = new LinkedList<InvRef>();
		for (InvRef r : subElements){
			if (r.getRefType().equals(RefType.Expression)){
				elts.add(r);
			}
		}
		
		if (elts.get(0).isHole() && elts.get(1).isHole()){
			
		}else if (elts.get(0).isHole()){
			elts.get(0).setValue(elts.get(1).getValue(),true);
			elts.get(1).setValue(null,true);
		}else if (elts.get(1).isHole()){
			elts.get(1).setValue(elts.get(0).getValue(),true);
			elts.get(0).setValue(null,true);
		}else{
			InvElement tmp = elts.get(0).getValue();
			elts.get(0).setValue(elts.get(1).getValue(),true);
			elts.get(1).setValue(tmp,true);
		}
		
		
		
	}
	
	public int numNotFixed(boolean countHoles){
		int cnt = 0;
		
		for (InvRef r : subElements){
			if (r.getRefType().equals(RefType.Expression)){
				if (r.isHole() && !countHoles){
					continue;
				}else if(!r.isHole() && r.getValue() instanceof InvFixed){
					continue;
				}else if (!r.isHole() && r.getValue() instanceof InvLocal && !((InvLocal) r.getValue()).hasHole()){
					continue;
				}
				
				cnt++;
			}
		}
		return cnt;
	}
	
	/**
	 * @return <code>true</code> if any of this element's subelements is an unfilled hole, or contains a unfilled hole.
	 */
	public boolean hasHole(){
		for (InvRef subElement : subElements){
			if (subElement.isHole() || subElement.getValue().hasHole()){
				return true;
			}
		}
		return false;
	}
	
	public List<InvRef> getSubElements(){
		return subElements;
	}
	
	public RefType getRefType(){
		return type;
	}
	
	
	@Override
	public String toString(){
		return getValue();
	}
	
	public String getValue(){
		List<String> x = new ArrayList<String>();
		
		for (InvRef e : subElements){
			x.add(e.toString());
		}
		
		String qq = x.get(0);
		
		for (int i = 1; i < x.size(); i++){
			qq += " " + x.get(i);
		}
		
		return qq;
	}
}