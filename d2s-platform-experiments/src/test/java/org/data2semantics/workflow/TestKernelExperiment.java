package org.data2semantics.workflow;

import org.data2semantics.platform.core.Workflow;
import org.data2semantics.platform.execution.LocalExecutionProfile;
import org.data2semantics.platform.execution.Orchestrator;
import org.data2semantics.platform.resourcespace.ResourceSpace;
import org.data2semantics.platform.util.WorkflowParser;
import org.junit.Test;

public class TestKernelExperiment {

	//@Test
	public void testFirstWorkflowWithKernelModules() throws Exception {
		
		WorkflowParser parser = new WorkflowParser();
		
		Workflow workflowContainer = parser.parseYAML("src/main/resources/DMoLD-experiment.yaml");
		
		ResourceSpace resourceSpace = new ResourceSpace();
		
		LocalExecutionProfile localExecutionProfile = new LocalExecutionProfile();
		
		Orchestrator platformOrchestrator = new Orchestrator(workflowContainer, localExecutionProfile, resourceSpace);
		
		platformOrchestrator.orchestrate();
		
		//workflowContainer.dumpIntermediateResults();
		
		platformOrchestrator.writeOutput("output_directory");
		
		
	}
}
