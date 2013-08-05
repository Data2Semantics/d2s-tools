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
	
	// Retries policy, how many times a failed modules should be retried
	public Orchestrator(Workflow w, ExecutionProfile ep, ResourceSpace rs)
	{
		workflow = w;
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
		
		// Instantiate and execute modules on stages (based on rank)
		
		for(Module m : workflow.modules()){

			if(m.ready()){
				
				// Instances of this module will be created
				// Outputs from previous dependency are also provided here.
				m.instantiate();

				
				for(ModuleInstance mi : m.instances()){
	
					System.out.println(" Executing instance of module  : " + mi.module().name());
					System.out.println("    Inputs : "+mi.inputs());
					mi.execute();
					System.out.println("    Outputs : "+mi.outputs());
					
				}
			} else 
				throw new IllegalStateException("Module not ready: " + m.name());
		}
		
		
		logModuleStatuses();
	}
	
	public void executeParallel(){
		 ExecutorService executor = Executors.newFixedThreadPool(10);
		 
		for(Module m : workflow.modules()){

			if(m.ready()){
				
				// Instances of this module will be created
				// Outputs from previous dependency are also provided here.
				m.instantiate();

				List<Future<Boolean>> results = new ArrayList<Future<Boolean>>();
				
				for(ModuleInstance mi : m.instances()){
	
					Callable<Boolean> moduleInstanceWorker = new ModuleInstanceWorker(mi);
					results.add(executor.submit(moduleInstanceWorker));
				}
				
				//Make sure all the instance finished before continue to next batch of modules.
				for(Future<Boolean> res : results ){
					try {
						res.get();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ExecutionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
			} else 
				throw new IllegalStateException("Module not ready: " + m.name());
		}		
		
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
	
}
