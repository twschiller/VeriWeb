/**
 * A FixedSizeSet is a mutable set of integers drawn from the range [0..7]
 **/
public class FixedSizeSet {
    
    /*@ invariant bits.owner == this; */ 
    /*@ spec_public */ private boolean[] bits;
    
    /**
     * Creates a new, empty FixedSizeSet.
     **/
    public FixedSizeSet() 
    {
        bits = new boolean[8]; 
        /*@ set bits.owner = this; */
    }
    
    /**
     * Adds n to this set
     **/
    public void add(int n) 
    {
        bits[n] = true;
    }
    
    /**
     * Returns true iff n is in this set
     **/
    public boolean contains(int n) 
    {
        return bits[n];
    }
    
    /**
     * Unions other into this (this' <= this U other)
     **/
    public void union(FixedSizeSet other) 
    {
        for (int i = 0; i < bits.length; i++) {
            if (other.bits[i]){
                bits[i] = true;
            }
        }
    }
    
    /**
     * Fill an array with one of two objects, based on whether the index
     * is in this set.
     **/
    public void fillDigits(Object[] digits, Object zero, Object one)
    //@requires \typeof(digits) == \type(Object[]);
    {
        for (int i = 0; i < bits.length; i++) {
            Object digit = (bits[i] ? one : zero);
            digits[i] = digit;
        }
    }
    
    /**
     * Return true iff this set represents the same abstract value as
     * the argument, which must be of the same type.
     **/
    public boolean similar(FixedSizeSet other) throws RuntimeException
    {
        if (other == null){
            throw new RuntimeException("null");
        }
        for (int i = 0; i < bits.length; i++) {
            if (bits[i] != other.bits[i]){
                return false;
            }
        }
        return true;
    }
    
}
