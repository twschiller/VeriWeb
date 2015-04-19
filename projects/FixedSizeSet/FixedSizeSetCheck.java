/**
 * FixedSizeSetCheck is a class which calls FixedSizeSet and verifies
 * that the written specification is sufficient.
 **/
public class FixedSizeSetCheck {
    
    /**
     * Check that the constructor creates a set which is empty.  Also
     * checks the contains method.
     **/
    public void checkConstructor() {
        FixedSizeSet set = new FixedSizeSet();
        for (int i = 0; i < 8; i++) {
            if (set.contains(i)) {
                throw new RuntimeException("New set should be empty");
            }
        }
    }
    
    /**
     * Check that add correctly adds an element to the set, and that
     * other elements are not modified.  Also checks the contains
     * method.
     **/
    public void checkAdd() {
        FixedSizeSet set = new FixedSizeSet();
        set.add(3);
        if (! set.contains(3)){
            throw new RuntimeException("Add should affect contains on the same element");
        }
        if (set.contains(2)){
            throw new RuntimeException("Add should not affect contains on a different element");
        }
    }
    
    /**
     * Check that union has no runtime exceptions.
     **/
    public void checkUnion() {
        FixedSizeSet set = new FixedSizeSet();
        FixedSizeSet set2 = new FixedSizeSet();
        set.union(set2);
    }
    
    /**
     * Check that fillDigits has no runtime exceptions.
     **/
    public void checkFillDigits() {
        FixedSizeSet set = new FixedSizeSet();
        Object[] result = new Object[8];
        set.fillDigits(result, "0", "1");
    }
    
    /**
     * Check that similar handles argument types correctly.
     **/
    public void checkSimilar() {
        FixedSizeSet set = new FixedSizeSet();
        
        // call should return exceptionally
        try {
            set.similar(null);
            throw new Error("Expected RuntimeException");
        } catch (RuntimeException e) {
            // we expect this exception
        }
        
        // call should return normally
        set.similar(set);
    }
    
}
