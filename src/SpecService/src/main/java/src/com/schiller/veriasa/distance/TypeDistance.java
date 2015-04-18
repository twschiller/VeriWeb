package com.schiller.veriasa.distance;

import java.util.HashMap;

import com.schiller.veriasa.web.shared.core.TypeSpecification;

/**
 * Distance between two type specifications
 * @author Todd Schiller
 */
public class TypeDistance{
	private final String goalDescriptor;
	private final TypeSpecification goal;
	private final TypeSpecification active;
	private final SpecDiff invariants;
	private final HashMap<String, MethodDistance> methods;
	private final int penalty;
	
	public TypeDistance(
			String goalDescriptor, 
			TypeSpecification goal, 
			TypeSpecification active, 
			SpecDiff objectInvariants, 
			HashMap<String, MethodDistance> methods, 
			int penalty) {
		
		this.goalDescriptor = goalDescriptor;
		this.goal = goal;
		this.active = active;
		this.invariants = objectInvariants == null ? new SpecDiff() : objectInvariants;
		this.methods = methods;
		this.penalty = penalty;
	}
	
	/**
	 * Returns the total distance of the object invariants, methods, and {@link TypeDistance#getPenalty()}.
	 * @return the total distance of the object invariants, methods, and {@link TypeDistance#getPenalty()}.
	 */
	public int getDistance(){
		int sum = 0;
		for (MethodDistance d : methods.values()){
			sum += d.getDistance();
		}
		return invariants.getDistance() + sum + penalty;
	}
	
	public void print(){
		for (String s : methods.keySet()){
			if (methods.get(s).getDistance() > 0){
				System.out.println(s);
				methods.get(s).print();		
			}
		}
	}
	
	@Override
	public String toString() {
		return "" +getDistance() + "," + goalDescriptor;
	}
	
	public String getGoalDescriptor() {
		return goalDescriptor;
	}
	
	public TypeSpecification getGoal() {
		return goal;
	}
	
	public TypeSpecification getActive() {
		return active;
	}
	
	public HashMap<String, MethodDistance> getMethods() {
		return methods;
	}

	public int getPenalty(){
		return penalty;
	}
	
	public SpecDiff getInvariants() {
		return invariants;
	}
}