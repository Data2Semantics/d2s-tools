package org.data2semantics.workflow;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.data2semantics.platform.domain.CommandLineDomain;
import org.junit.Test;

public class CommandLineDomainTests {

	@Test
	public void testArithConfigFile(){
			CommandLineDomain domain = new CommandLineDomain();
			
			final String SOURCE= "src/main/resources/ArithModule.cfg";
			
			final String [] expectedOutputs = new String[]{"product","sum","difference"};
			final String [] expectedInputs  = new String[]{"first","second"};
			final String expectedFirstDescription = "this is the first input";
			final String expectedProductDescription = "this is the product of the inputs";
			
			assertEquals(domain.outputs(SOURCE), Arrays.asList(expectedOutputs));
			assertEquals(domain.inputs(SOURCE), Arrays.asList(expectedInputs));
			
			assertEquals(domain.inputDescription(SOURCE, "first"), expectedFirstDescription);
			assertEquals(domain.outputDescription(SOURCE, "product"), expectedProductDescription);
			
			
				
			
	}
}
