package org.data2semantics.platform.execution;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import javax.swing.DebugGraphics;

import org.data2semantics.platform.core.AbstractModule;
import org.data2semantics.platform.core.Input;
import org.data2semantics.platform.core.Module;
import org.data2semantics.platform.core.State;
import org.data2semantics.platform.core.Workflow;
import org.data2semantics.platform.resourcespace.ResourceSpace;
import org.data2semantics.platform.wrapper.SimpleModuleWrapper;



/**
 * Every execution profile should be able to execute workflow right?
 * Ideally this will be derived by the different execution environment.
 * 
 * 
 * Main responsibility of this class is to execute a module in a certain environment.
 * 
 * @author wibisono
 *
 */
abstract public class ExecutionProfile {
	
		private final static Logger LOG = Logger.getLogger(ExecutionProfile.class.getName());
	
		/**
		 * Attempt to execute abstract modules, returning the status of the current module being executed.
		 * Maybe while enacting, we need also to have access to the resource space. 
		 * 
		 * The results then needs to be returned to resource space.
		 * I am not sure why this has to return a state, maybe not, just boolean success or not.
		 * @param currentWorkflow 
		 * @param m
		 * @param resultSpace 
		 * @return whether the execution is successful
		 */
		public boolean executeModule(Module currentModule, Workflow currentWorkflow, ResourceSpace resultSpace){
				boolean success = true;
				
				
				//Check each inputs of current Module, if they are all ready.
				
				//If there are references from other retrieve all required inputs from Resource Space
				
				// Execute the module based on inputs.
				
				// Now here we will have the cases, 
				
				
				// Perhaps at this stage actually all the inputs are already resolved ?
				List<Input> inputs = currentModule.getInputs();
				
				
				// if available inputs are exactly the same type with the expected inputs then we will only execute once.
				// Confused, when is it better to do this check haven't this been done already at this point?
				LOG.info("Executing module :- "+currentModule);
				success = currentModule.execute();
				
				
				if(success){
					currentModule.setState(State.FINISHED);
				} else
					currentModule.setState(State.FAILED);
		
				
				// at the end of this all we will then return result to result space

				
				return success;
				
		}
		
		
		public boolean atomicModuleExecution(Module currentModule, ResourceSpace resultSpace){
			boolean success = true;
			
			return success;
		}

}
