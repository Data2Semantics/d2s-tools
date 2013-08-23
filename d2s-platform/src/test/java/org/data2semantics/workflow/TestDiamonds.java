package org.data2semantics.workflow;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.data2semantics.platform.core.Module;
import org.data2semantics.platform.core.ModuleInstance;
import org.data2semantics.platform.core.Workflow;
import org.data2semantics.platform.core.data.InstanceOutput;
import org.data2semantics.platform.execution.ExecutionProfile;
import org.data2semantics.platform.execution.LocalExecutionProfile;
import org.data2semantics.platform.execution.Orchestrator;
import org.data2semantics.platform.execution.ThreadedLocalExecutionProfile;
import org.data2semantics.platform.reporting.HTMLReporter;
import org.data2semantics.platform.resourcespace.ResourceSpace;
import org.data2semantics.platform.util.PlatformUtil;
import org.data2semantics.platform.util.WorkflowParser;
import org.junit.Test;

public class TestDiamonds {
	@Test
	public void testDiamond() throws Exception {
		
		Workflow workflow = WorkflowParser.parseYAML("src/test/resources/Diamond.yaml");
		
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
		
				
		HTMLReporter reporter = new HTMLReporter(workflow, new File("output_diamond"));
		reporter.report();
		
		assertEquals(2, workflow.getModuleByName("A").instances().size());
		assertEquals(4, workflow.getModuleByName("B").instances().size());
		assertEquals(4, workflow.getModuleByName("C").instances().size());
		assertEquals(8, workflow.getModuleByName("D").instances().size());
					
	}
	
	@Test
	public void testDiamondRef() throws Exception {
		
		Workflow workflow = WorkflowParser.parseYAML("src/test/resources/DiamondWithRef.yaml");
		
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
		
				
		HTMLReporter reporter = new HTMLReporter(workflow, new File("output_diamond_ref"));
		reporter.report();
		
		assertEquals(2, workflow.getModuleByName("A").instances().size());
		assertEquals(4, workflow.getModuleByName("B").instances().size());
		assertEquals(4, workflow.getModuleByName("C").instances().size());
		assertEquals(24, workflow.getModuleByName("D").instances().size());
					
	}
	
	@Test
	public void testBrokenDiamond() throws Exception {
		
		Workflow workflow = WorkflowParser.parseYAML("src/test/resources/BrokenDiamond.yaml");
		
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
		
				
		HTMLReporter reporter = new HTMLReporter(workflow, new File("output_diamond_ref"));
		reporter.report();
		
		assertEquals(2, workflow.getModuleByName("A").instances().size());
		assertEquals(4, workflow.getModuleByName("B").instances().size());
		assertEquals(4, workflow.getModuleByName("C").instances().size());
		assertEquals(4, workflow.getModuleByName("D").instances().size());
					
	}
	
	@Test
	public void testTriangle() throws Exception {
		
		Workflow workflow = WorkflowParser.parseYAML("src/test/resources/Triangle.yaml");
		
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
		
				
		HTMLReporter reporter = new HTMLReporter(workflow, new File("output_diamond_ref"));
		reporter.report();
		
		assertEquals(2, workflow.getModuleByName("A").instances().size());
		assertEquals(4, workflow.getModuleByName("B").instances().size());
		assertEquals(4, workflow.getModuleByName("C").instances().size());

					
	}
}
