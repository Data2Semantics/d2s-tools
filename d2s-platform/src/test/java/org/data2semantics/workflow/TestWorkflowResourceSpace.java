package org.data2semantics.workflow;

import java.lang.reflect.Method;

import org.data2semantics.platform.core.Workflow;
import org.data2semantics.platform.execution.LocalExecutionProfile;
import org.data2semantics.platform.execution.Orchestrator;
import org.data2semantics.platform.resourcespace.ResourceSpace;
import org.data2semantics.platform.util.PlatformUtil;
import org.data2semantics.platform.util.WorkflowParser;
import org.junit.Test;

public class TestWorkflowResourceSpace {

	@Test
	public void testFirstWorkflowWithRS() throws Exception {
		WorkflowParser parser = new WorkflowParser();
		
		Workflow workflowContainer = parser.parseYAML("src/test/resources/multi-modules.yaml");
		
		ResourceSpace resourceSpace = new ResourceSpace();
		LocalExecutionProfile localExecutionProfile = new LocalExecutionProfile();
		Orchestrator platformOrchestrator = new Orchestrator(workflowContainer, localExecutionProfile, resourceSpace);
		
		platformOrchestrator.execute();
		
		workflowContainer.dumpIntermediateResults();

	}
	
	@Test
	public void justSomeTest() throws ClassNotFoundException{
		ClassLoader loader = getClass().getClassLoader();
		Class <?> test = loader.loadClass("org.data2semantics.modules.ListModule");
		Method m = PlatformUtil.getMainMethod(test);
		
		
		
	}
}
