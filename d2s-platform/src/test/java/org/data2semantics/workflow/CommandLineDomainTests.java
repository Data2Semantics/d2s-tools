package org.data2semantics.workflow;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.data2semantics.platform.domain.CommandLineDomain;
import org.junit.Test;

public class CommandLineDomainTests {

	@Test
	public void testArithConfigFile(){
			CommandLineDomain domain = new CommandLineDomain();
			
			final String SOURCE= "src/test/resources/ArithModule.cfg";
			
			final String [] expectedOutputs = new String[]{"product","sum","difference"};
			final String [] expectedInputs  = new String[]{"first","second"};
			final String expectedFirstDescription = "this is the first input";
			final String expectedProductDescription = "this is the product of the inputs";
			final String expectedCommandLine ="set /a product=%first%*%second% && set /a sum=%first%+%second% && set /a difference=%first%-%second%";
			
			assertEquals(Arrays.asList(expectedOutputs), domain.outputs(SOURCE));
			assertEquals(Arrays.asList(expectedInputs), domain.inputs(SOURCE));
			
			assertEquals(expectedFirstDescription, domain.inputDescription(SOURCE, "first"));
			assertEquals(expectedProductDescription, domain.outputDescription(SOURCE, "product"));
			
			assertEquals( expectedCommandLine, domain.getCommand(SOURCE));
			
				
			
	}
}
