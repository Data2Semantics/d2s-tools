package org.data2semantics.platform.util;

import org.data2semantics.platform.core.Workflow;

/*
 * Some validation to check if the workflow definition provided is valid.
 */
public class ValidateWorkflow {

	/**
	 * Check for all modules within a workflow, and see if they are all exist in the bundle/jar file provided.
	 * @param w
	 * @return
	 */
	public boolean validateModuleSources(Workflow w){
		boolean result = false;
		
		return result;
	}
	
	/**
	 * Make sure that all module names are unique
	 * @param w
	 * @return
	 */
	public boolean validateModuleNames(Workflow w){
		boolean result = false;
		
		return result;
	}
}
