package org.data2semantics.workflow;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.data2semantics.platform.core.Module;
import org.data2semantics.platform.core.ModuleInstance;
import org.data2semantics.platform.core.Workflow;
import org.data2semantics.platform.core.data.InstanceOutput;
import org.data2semantics.platform.domain.CommandLineDomain;
import org.data2semantics.platform.execution.ExecutionProfile;
import org.data2semantics.platform.execution.LocalExecutionProfile;
import org.data2semantics.platform.execution.Orchestrator;
import org.data2semantics.platform.reporting.CSVReporter;
import org.data2semantics.platform.resourcespace.ResourceSpace;
import org.data2semantics.platform.util.WorkflowParser;
import org.junit.Test;

public class CommandLineDomainTests {

	@Test
	public void testArithConfigFile(){
			CommandLineDomain domain = new CommandLineDomain();
			
			final String SOURCE= "src/test/resources/commandLine/ArithModule.cfg";
			
			final String [] expectedOutputs = new String[]{"result","product","sum","difference"};
			final String [] expectedInputs  = new String[]{"first","second"};
			final String expectedFirstDescription = "this is the first input";
			final String expectedProductDescription = "this is the product of the inputs";
			final String expectedCommandLine ="src/test/resources/commandLine/arith.bat";
			
			assertEquals(Arrays.asList(expectedOutputs), domain.outputs(SOURCE));
			assertEquals(Arrays.asList(expectedInputs), domain.inputs(SOURCE));
			
			assertEquals(expectedFirstDescription, domain.inputDescription(SOURCE, "first"));
			assertEquals(expectedProductDescription, domain.outputDescription(SOURCE, "product"));
			
			assertEquals( expectedCommandLine, domain.getCommand(SOURCE));
			
				
			
	}
	
	@Test 
	public void testArithWorkflow() throws IOException{
		Workflow workflow = WorkflowParser.parseYAML("src/test/resources/commandLine/CLIAdder.yaml");
		
		ResourceSpace resourceSpace = new ResourceSpace();
		
		ExecutionProfile localExecutionProfile = new LocalExecutionProfile();
		
		Orchestrator platformOrchestrator = new Orchestrator(workflow, localExecutionProfile, resourceSpace);
		
		platformOrchestrator.orchestrate();
		
		for(Module m : workflow.modules()){
			System.out.println("\nModule " + m.name());
			
			for(ModuleInstance mi :  m.instances()){
					for(InstanceOutput io : mi.outputs())
					System.out.print(io.name()+":"+io.value()+ " ");
			}
		}
		
		CSVReporter reporter = new CSVReporter(workflow, new File("output_dir_iterator"));
		reporter.report();
		
	}
	
	@Test 
	public void testTemplateWorkflow() throws IOException{
		Workflow workflow = WorkflowParser.parseYAML("src/test/resources/commandLine/template.yaml");
		
		ResourceSpace resourceSpace = new ResourceSpace();
		
		ExecutionProfile localExecutionProfile = new LocalExecutionProfile();
		
		Orchestrator platformOrchestrator = new Orchestrator(workflow, localExecutionProfile, resourceSpace);
		
		platformOrchestrator.orchestrate();
		
		for(Module m : workflow.modules()){
			System.out.println("\nModule " + m.name());
			
			for(ModuleInstance mi :  m.instances()){
					for(InstanceOutput io : mi.outputs())
					System.out.print(io.name()+":"+io.value()+ " ");
			}
		}
	}
	
	@Test 
	public void testExtractExcel() throws IOException{
		Workflow workflow = WorkflowParser.parseYAML("src/test/resources/commandLine/excel.yaml");
		
		ResourceSpace resourceSpace = new ResourceSpace();
		
		ExecutionProfile localExecutionProfile = new LocalExecutionProfile();
		
		Orchestrator platformOrchestrator = new Orchestrator(workflow, localExecutionProfile, resourceSpace);
		
		platformOrchestrator.orchestrate();
		
		for(Module m : workflow.modules()){
			System.out.println("\nModule " + m.name());
			
			for(ModuleInstance mi :  m.instances()){
					for(InstanceOutput io : mi.outputs())
					System.out.print(io.name()+":"+io.value()+ " ");
			}
		}
	}
	@Test
	public void testProcessBuilder() throws IOException{
		
		ProcessBuilder pb = new ProcessBuilder("src/test/resources/commandLine/arith.bat");
		Process p = pb.start();
		
	}
}
