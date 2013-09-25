package org.data2semantics.tools.cat;

import java.io.File;

import org.data2semantics.platform.core.Workflow;
import org.data2semantics.platform.execution.LocalExecutionProfile;
import org.data2semantics.platform.execution.Orchestrator;
import org.data2semantics.platform.reporting.HTMLReporter;
import org.data2semantics.platform.resourcespace.ResourceSpace;
import org.data2semantics.platform.util.WorkflowParser;
import org.junit.Test;


public class TestAdjacency {

	@Test
	public void testAdjacencyWorkflow() throws Exception {
		
		WorkflowParser parser = new WorkflowParser();
		
		Workflow workflow = parser.parseYAML("src/main/resources/adjacency.yaml");
		
		ResourceSpace resourceSpace = new ResourceSpace();
		
		LocalExecutionProfile localExecutionProfile = new LocalExecutionProfile();
		
		Orchestrator platformOrchestrator = new Orchestrator(workflow, localExecutionProfile, resourceSpace);
		
		platformOrchestrator.orchestrate();
		
		HTMLReporter reporter = new HTMLReporter(workflow, new File("output"));
		reporter.report();
	}
}
