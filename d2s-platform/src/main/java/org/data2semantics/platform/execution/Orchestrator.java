package org.data2semantics.platform.execution;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

import org.data2semantics.platform.core.AbstractModule;
import org.data2semantics.platform.core.Module;
import org.data2semantics.platform.core.State;
import org.data2semantics.platform.core.Workflow;
import org.data2semantics.platform.resourcespace.ResourceSpace;
import org.data2semantics.platform.wrapper.SimpleModuleWrapper;


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
	private Workflow currentWorkflow;
	
	// This is the execution profile associated with this workflow, which seems 
	// to be not accurate since if we will have module from different Execution 
	// profile, then we should have an execution profile associated with module
	private ExecutionProfile executionProfile;
	
	// Result space, where we will have the results stored
	private ResourceSpace resourceSpace;
	
	// Retries policy, how many times a failed modules should be retried
	public Orchestrator(Workflow w, ExecutionProfile ep, ResourceSpace rs)
	{
		currentWorkflow = w;
		executionProfile = ep;
		resourceSpace = rs;
		
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
	public void execute(){
		
		// Get all modules which are ready to be executed
		List<Module> readyModules = currentWorkflow.modules(State.READY);
		
		// Main execution loop, find all ready modules and execute
		while(readyModules.size() > 0){
			
			// Normal batch of execution all ready module
			for(Module currentModule : readyModules ){
				
				executionProfile.executeModule(currentModule, currentWorkflow,  resourceSpace);
				
			}
	
			// Need to update blocked module also here, 
			currentWorkflow.updateModuleReferences();
			
		
			// Update ready modules, assuming that above executions updates successfully state of the modules.
			readyModules = currentWorkflow.modules(State.READY);
			
		}
		
		logModuleStatuses();
	}
	
	public void logModuleStatuses(){
		StringBuffer statuses = new StringBuffer();
		for(State currentState: State.values()){
			List<Module> modules= currentWorkflow.modules(currentState);
			if(modules.size() > 0){
				statuses.append("\nState : " + currentState);
				for(Module m : modules)statuses.append("     "+m);
			}
		}
		LOG.info(statuses.toString());
	}

	public void writeOutput(String output_directory) {
		resourceSpace.generateReport(new File(output_directory));	
	}
	
}
