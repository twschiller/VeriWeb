package com.schiller.veriasa.distance;

/**
 * Distance between two method specifications
 * @author Todd Schiller
 */
public class MethodDistance{
	final SpecDiff requires;
	final SpecDiff ensures;
	final SpecDiff exsures;
	
	/**
	 * @param requires difference in requires clauses
	 * @param ensures difference in ensures clauses
	 * @param exsures difference in exsures clauses, or null if the method does not declare thrown exceptions
	 */
	public MethodDistance(SpecDiff requires, SpecDiff ensures, SpecDiff exsures) {
		super();
		this.requires = requires;
		this.ensures = ensures;
		this.exsures = exsures;
	}
	
	/**
	 * Returns the total distance between the requires, ensures, and exsures clauses
	 * @return the total distance between the requires, ensures, and exsures clauses
	 */
	public int getDistance(){
		return requires.getDistance() + ensures.getDistance() + (exsures == null ? 0 : exsures.getDistance());
	}
	
	public void print(){
		if (requires.getDistance() > 0){
			System.out.println("Requires");
			System.out.println(requires);
		}
		if (ensures.getDistance() > 0){
			System.out.println("Ensures");
			System.out.println(ensures);
		}
		
		if ( exsures != null && exsures.getDistance() > 0){
			System.out.println("Exsures");
			System.out.println(exsures);
		}
		
	}
	
	public SpecDiff getRequires() {
		return requires;
	}
	
	public SpecDiff getEnsures() {
		return ensures;
	}
	
	/**
	 * Returns the difference in exsures clauses, or null if the method does not declare thrown exceptions
	 * @return the difference in exsures clauses, or null if the method does not declare thrown exceptions
	 */
	public SpecDiff getExsures() {
		return exsures;
	}
}

