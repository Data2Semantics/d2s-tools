package org.data2semantics.platform.execution;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import org.data2semantics.platform.core.Module;
import org.data2semantics.platform.core.ModuleInstance;
import org.data2semantics.platform.core.Workflow;
import org.data2semantics.platform.reporting.Reporter;
import org.data2semantics.platform.resourcespace.ResourceSpace;


/**
 * Class that will be responsible for executing workflow. Given a workflow, 
 * execution profile and a resource space, this executor will be responsible
 * to decide which modules will be executed next.
 * 
 * @author wibisono
 *
 */
public class Orchestrator {

	private final static Logger LOG = Logger.getLogger(Orchestrator.class.getName());
	
	// Current workflow containing modules.
	private Workflow workflow;
	
	// This is the execution profile associated with this workflow, which seems 
	// to be not accurate since if we will have module from different Execution 
	// profile, then we should have an execution profile associated with module
	private ExecutionProfile executionProfile;
	
	// Result space, where we will have the results stored
	private ResourceSpace resourceSpace;
	
	List<Reporter> reporters;
	public Orchestrator(Workflow w, ExecutionProfile ep, ResourceSpace rs)
	{
		workflow = w;
		executionProfile = ep;
		resourceSpace = rs;
		reporters = new ArrayList<Reporter>();
		//currentWorkflow.setResourceSpace(resourceSpace);
	}
	// Retries policy, how many times a failed modules should be retried
	public Orchestrator(Workflow w, ExecutionProfile ep, ResourceSpace rs, List<Reporter> rep)
	{
		workflow = w;
		executionProfile = ep;
		resourceSpace = rs;
		reporters = rep;
		//currentWorkflow.setResourceSpace(resourceSpace);
	}
	
	/**
	 * Initialize the result space, workflow and execution profile.
	 */
	public void initialize(){
		resourceSpace.initialize();
	}
	
	
	/**
	 * Perhaps this will be the main loop where all the execution happened.
	 */
	public void orchestrate(){
		
		// Instantiate and execute modules on stages (based on rank)
		
		executionProfile.executeModules(workflow.modules(), reporters);
		
		logModuleStatuses();
	}

	
	public void logModuleStatuses(){
		StringBuffer statuses = new StringBuffer();
//		for(State currentState: State.values()){
//			List<ModuleInstance> modules= currentWorkflow.modules(currentState);
//			if(modules.size() > 0){
//				statuses.append("\nState : " + currentState);
//				for(ModuleInstance m : modules)statuses.append("     "+m);
//			}
//		}
		LOG.info(statuses.toString());
	}

	public void writeOutput(String output_directory) {
		resourceSpace.generateReport(new File(output_directory));	
	}

	public void setExecutionProfile(ExecutionProfile profile){
		executionProfile = profile;
	}
}
