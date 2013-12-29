package com.schiller.veriasa.distance.test;

import static org.junit.Assert.*;

import org.jmlspecs.checker.JmlSpecExpression;
import org.junit.BeforeClass;
import org.junit.Test;

import antlr.RecognitionException;
import antlr.TokenStreamException;

import com.schiller.veriasa.distance.Normalize;
import com.schiller.veriasa.executejml.ExecuteJml;

public class NormalizeTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	private void checkOne(String test, String goal){
		JmlSpecExpression ex1 = null;
		JmlSpecExpression ex2 = null;
		try {
			ex1 = ExecuteJml.tryParse(test);
			ex2 = ExecuteJml.tryParse(goal);
		} catch (RecognitionException e) {
			fail("Error parsing ");
		} catch (TokenStreamException e) {
			fail("Error parsing ");
		}
		assertEquals(Normalize.normalize(ex2).toString(),Normalize.normalize(ex1).toString());
		assertEquals(ex2.toString(), Normalize.normalize(ex1).toString());
	}
	
	@Test
	public void testNormalize() {
	
		String test[] = new String[]{
				"a  <=  b - 1;",
				"x - 1 > 3;",
				"x - 1 < 3;",
				"-1 < 5;",
				"x < 5;",
				"x > 5;",
				"this.x < 5;",
				"x >= 5;",
				"y <== x;",
				"0 < 5;",
		};
		String expected[] = new String[]{
				"(a  <  b);",
				"(4 < x);",
				"(x < 4);",
				"(0 < 6);",
				"(x < 5);",
				"(5 < x);",
				"(this.x < 5);",
				"(4 < x));",
				"(x ==> y);",
				"(0 < 5);",
		};
		
		for (int  i =0; i <test.length; i++){
			checkOne(test[i],expected[i]);
		}
	}

}
