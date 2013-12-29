package com.schiller.veriasa.executejml.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.util.Arrays;

import org.jmlspecs.checker.JmlSpecExpression;
import org.junit.BeforeClass;
import org.junit.Test;

import antlr.RecognitionException;
import antlr.TokenStreamException;

import com.schiller.veriasa.executejml.CollectDataProcessor;
import com.schiller.veriasa.executejml.ExecuteJml;

import daikon.FileIO;
import daikon.PptMap;
import daikon.PptTopLevel;
import daikon.ValueTuple;


public class TestExecuteJml  {

	private final static String DTRACE_FILES[] = new String[]{"/home/tws/StackArDriver.dtrace"};
	private final static CollectDataProcessor processor = new CollectDataProcessor();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		PptMap ppts = new PptMap();
		try {
			FileIO.read_data_trace_files (Arrays.asList(DTRACE_FILES), ppts, processor, false);
		} catch (Exception e) {
			throw new Error(e);
		}
	}


	public static void checkOne(PptTopLevel ppt, ValueTuple vt, String s, boolean expect){
		for (ExecuteJml.ExecutionVisitor.Mode mode : ExecuteJml.ExecutionVisitor.Mode.values()){

			JmlSpecExpression ex = null;
			try {
				ex = ExecuteJml.tryParse(s);
			} catch (RecognitionException e) {
				fail("Error parsing " + s);
			} catch (TokenStreamException e) {
				fail("Error parsing " + s);
			}
			ExecuteJml.ExecutionVisitor v = ExecuteJml.ExecutionVisitor.exec(ex, ppt, vt, mode);
			assertEquals(expect,v.getStatus());

		}
	}

	public static void checkPost(PptTopLevel ppt, Iterable<ValueTuple> vts, String s, boolean expect) throws RecognitionException, TokenStreamException{
		JmlSpecExpression ex = ExecuteJml.tryParse(s);
		
		for (ValueTuple vt : vts){
			ExecuteJml.ExecutionVisitor v = ExecuteJml.ExecutionVisitor.exec(ex, ppt, vt, ExecuteJml.ExecutionVisitor.Mode.POST);

			//all must be true
			//assertFalse(expect && !v.getStatus());

			if (!v.getStatus()){
				if (expect){
					fail("Valid postcondition " + s + " doesn't hold");
				}else{
					return;
				}
			}
		}

		if (!expect){	
			fail("Postcondition " + s + " holds");
		}
	}


	@Test
	public void testSetup(){
		assertFalse(processor.samples.isEmpty());
	}

	@Test
	public void testArrayAccess(){
		String pass[] = new String[] { "this.theArray[topOfStack] == x;", "this.theArray[0] != null;", };
		String bad[] = new String[] { "this.theArray[0] == x;" };
		testPost("StackAr.push(java.lang.Object)",pass,bad);
	}

	public void testPost(String signature, String good[], String bad[]){
		boolean found = false;


		for (PptTopLevel ppt : processor.samples.keySet()) {
			if (ppt.name.startsWith(signature + ":::EXIT")){
				found = true;

				for (String s : good){
					try {
						checkPost(ppt, processor.samples.get(ppt),s,true);
					} catch (RecognitionException e) {
						fail("Malformed contract " + s);
					} catch (TokenStreamException e) {
						fail("Malforned contract " + s);
					}
				}
				for (String s : bad){

					try {	
						checkPost(ppt, processor.samples.get(ppt),s,false);
					} catch (RecognitionException e) {
						fail("Malformed contract " + s);
					} catch (TokenStreamException e) {
						fail("Malforned contract " + s);
					}
				}
			}	
		}
		assertEquals(true,found);
	}

	@Test
	public void testPred(){
		String pass[] = new String[] { "\\result == (this.topOfStack == -1);"};
		String bad[] = new String[] { "\\result == (this.topOfStack == 0);", };
		testPost("StackAr.isEmpty()",pass,bad);
	}

	@Test
	public void testForAll(){
		String pass[] = new String[] {  "(\\forall int i; 0 <= i && i < this.topOfStack; this.theArray[i] != null);",
				"(\\forall int i;  i >= 0 && this.topOfStack > i; this.theArray[i] != null);",
				"(\\forall int i;  i >= 0 && i <= this.topOfStack - 1; this.theArray[i] != null);",	
				"(\\forall int i;  (i >= 0 && i <= this.topOfStack - 1) ==> this.theArray[i] != null);",
				"(\\forall int i;  this.theArray[i] != null <== (i >= 0 && i <= this.topOfStack - 1) );",
				"(\\forall int i;  ((((i) >= 0) && (i) <= (this.topOfStack - 1))) ==> this.theArray[i] != null);",
		};
		String bad[] = new String[] { "(\\forall int i; 0 <= i && i < this.topOfStack; this.theArray[i] == null);" };
		testPost("StackAr.push(java.lang.Object)",pass,bad);
	}


	@Test
	public void testOld(){
		String pass[] = new String[] { "this.topOfStack == \\old(this.topOfStack)+1;"};
		String bad[] = new String[] { "this.topOfStack == \\old(this.topOfStack);", 
				"this.topOfStack < \\old(this.topOfStack);", "this.topOfStack == \\old(this.topOfStack)-1;"};
		testPost("StackAr.push(java.lang.Object)",pass,bad);
	}




	@Test
	public void testScalarFields(){
		String pass[] = new String[] { "this.theArray != null;", "this.topOfStack >= -1;","this.topOfStack >= -3;", "topOfStack >= -3;", };
		String bad[] = new String[] { "this.topOfStack > 2;","topOfStack > 2;","this.theArray == null;" };

		for (PptTopLevel ppt : processor.samples.keySet()) {
			if (ppt.name.equals("StackAr.isEmpty():::ENTER")){
				for (ValueTuple vt : processor.samples.get(ppt)) {
					for (String s : pass){
						checkOne(ppt, vt,s,true);
					}
					for (String s : bad){
						checkOne(ppt, vt,s,false);
					}

				}
			}	
		}	
	}

	@Test
	public void testArrayNullness(){
		String pass[] = new String[] {  "this.theArray != null;" };
		String bad[] = new String[] {"this.theArray == null;" };

		for (PptTopLevel ppt : processor.samples.keySet()) {
			if (ppt.name.equals("StackAr.isEmpty():::ENTER")){
				for (ValueTuple vt : processor.samples.get(ppt)) {
					for (String s : pass){
						checkOne(ppt, vt,s,true);
					}
					for (String s : bad){
						checkOne(ppt, vt,s,false);
					}

				}
			}	
		}	
	}



	@Test
	public void testScalarParams(){
		String pass[] = new String[] { "capacity > -1;","capacity > 0;"};
		String bad[] = new String[] { "capacity > 2;", };

		for (PptTopLevel ppt : processor.samples.keySet()) {
			if (ppt.name.equals("StackAr.StackAr(int):::ENTER")){
				for (ValueTuple vt : processor.samples.get(ppt)) {
					for (String s : pass){
						checkOne(ppt, vt,s,true);
					}
					for (String s : bad){
						checkOne(ppt, vt,s,false);
					}

				}
			}	
		}

	}


	@Test
	public void testSizeFields(){
		String pass[] = new String[] { "this.theArray.length > 0;"};
		String bad[] = new String[] { "this.theArray.length > 5;", "this.theArray.length == 0;"};

		for (PptTopLevel ppt : processor.samples.keySet()) {
			if (ppt.name.equals("StackAr.isEmpty():::ENTER")){
				for (ValueTuple vt : processor.samples.get(ppt)) {
					for (String s : pass){
						checkOne(ppt, vt,s,true);
					}
					for (String s : bad){
						checkOne(ppt, vt,s,false);
					}

				}
			}	
		}
	}


	@Test
	public void testNestedTrueConstants(){
		String pass[] = new String[]{"2 < 4 ==> 3 < 4;", "3 > 4 ==> false;", "(3 > 4 ==> false) ==> true;"};

		for (String s : pass){
			for (PptTopLevel ppt : processor.samples.keySet()) {
				for (ValueTuple vt : processor.samples.get(ppt)) {
					checkOne(ppt, vt,s,true);
				}
			}
		}
	}




	@Test
	public void testBasicTrueConstants(){
		String pass[] = new String[]{"true;", "3 == 3;", "2 < 4;" , "3 <= 3;"
				, "3 <= 4;", "4 >= 3;", "4 > 1;", "!false;" , "!(3 < 2);", "-2 < 3;", "2.0 < 3.0;"};

		for (String s : pass){
			for (PptTopLevel ppt : processor.samples.keySet()) {
				for (ValueTuple vt : processor.samples.get(ppt)) {
					checkOne(ppt, vt,s,true);
				}
			}
		}
	}

	@Test
	public void testBasicMath(){
		String pass[] = new String[]{"2 + 1 > 2;", "3 <= 0 + 3;", "100 - 5 > 90;",
				"2.5 + 1.0 > 3.2;", "2.5 - 1.1 <= 4.2;"};

		for (String s : pass){
			for (PptTopLevel ppt : processor.samples.keySet()) {
				for (ValueTuple vt : processor.samples.get(ppt)) {
					checkOne(ppt, vt,s,true);
				}
			}
		}
	}


	@Test
	public void testLogicTrueConstants(){
		String pass[] = new String[]{"true ==> true;", "false ==> true;", "false ==> false;",
				"true <==> true;", "true <== true;", "false <== false;", "false <==> false;",
				"false <=!=> true;","true <=!=> false;", "true && true;", "true || false;","false || true;"};

		for (String s : pass){
			for (PptTopLevel ppt : processor.samples.keySet()) {
				for (ValueTuple vt : processor.samples.get(ppt)) {
					checkOne(ppt, vt,s,true);
				}
			}
		}
	}

	@Test
	public void testLogicFalseConstants(){
		String pass[] = new String[]{"true ==> false;","true <==> false;","false <==> true;"
				,"false <== true;","true <=!=> true;","false <=!=> false;", "true && false;", "false || false;"};

		for (String s : pass){
			for (PptTopLevel ppt : processor.samples.keySet()) {
				for (ValueTuple vt : processor.samples.get(ppt)) {
					checkOne(ppt, vt,s,false);
				}
			}
		}
	}

	@Test
	public void testBasicFalseConstants(){
		String pass[] = new String[]{"false;", "3 == 4;", "4 < 3;", "2 < 2;", "3 <= 2;", "!true;",};

		for (String s : pass){
			for (PptTopLevel ppt : processor.samples.keySet()) {
				for (ValueTuple vt : processor.samples.get(ppt)) {
					checkOne(ppt, vt,s,false);
				}
			}
		}
	}
}
