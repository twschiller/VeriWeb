package com.schiller.veriasa.web.shared.dnd;

import java.io.Serializable;
import java.util.List;

import com.schiller.veriasa.web.shared.dnd.InvElement.RefType;

public class InvRef implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private InvElement value;
	private RefType type;
	private String tooltip;
	
	@SuppressWarnings("unused")
	private InvRef(){
		this(RefType.Expression);
	}

	public InvRef(RefType type, String tooltip){
		if (type == null){
			throw new IllegalArgumentException();
		}
		
		this.value = null;
		this.type = type;
		this.tooltip = tooltip;
	}
	
	public InvRef(RefType type){
		this(type, null);
	}
	
	public InvRef(InvElement value){
		if (value == null){
			throw new IllegalArgumentException();
		}
		
		this.value = value;
		this.type = value.getRefType();
	}
	
	public String toString(){
		return value == null ?  "<hole>" : value.toString();
	}
	
	protected InvRef duplicate(){
		InvRef n = new InvRef(type);
		if (value != null){
			n.value = value.duplicate();
		}
		return n;
	}
	
	
	private boolean isMaybeHole(){
		if (this.value.getSubElements().size() == 1){
			InvRef r = this.value.getSubElements().get(0);
			if (r.getValue() instanceof Maybe && !((Maybe<?>) r.getValue()).hasValue()){
				return true;
			}
		}
		return false;
	}

	public boolean needConfirmation(InvElement value){
		if (isHole() || value == null || isMaybeHole()){
			return false;
		}else{
			List<InvRef> old = this.value.getNotFixed();
			
			for (int i = value.numNotFixed(true); i < old.size(); i++){
				if (!old.get(i).isHole()){
					return true;
				}
			}
		}
		return false;
	}
	

	public boolean hasTooltip(){
		return tooltip != null && !tooltip.equals("");
	}
	
	/**
	 * @return the tooltip
	 */
	public String getTooltip() {
		return tooltip;
	}

	public void setValue(InvElement value, boolean force){
		
		if (value != null && !value.getRefType().equals(type)){
			throw new IllegalArgumentException();
		}
		
		if (needConfirmation(value) && !force){
			throw new IllegalArgumentException();
		}
		
		if (isHole() || value == null || isMaybeHole()){
			this.value = value;
		}else{
			List<InvRef> old = this.value.getNotFixed();
			
			this.value = value;
			
			for (int i = 0; i < value.numNotFixed(true) && i < old.size(); i++){
				if (!old.get(i).isHole()){
					this.value.getNotFixed().get(i).setValue(old.get(i).getValue(), true);
				}
				
			}
			
		}
	}
	
	public boolean isHole(){
		return value == null;
	}
	public InvElement getValue(){
		return value;
	}
	public RefType getRefType(){
		return type;
	}
}
