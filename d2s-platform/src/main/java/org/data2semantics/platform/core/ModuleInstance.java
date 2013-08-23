package org.data2semantics.platform.core;

import java.util.List;

import org.data2semantics.platform.core.data.InstanceInput;
import org.data2semantics.platform.core.data.InstanceOutput;

/*
 * ModuleInstance is the concrete realization of abstract module which can be executed in a {@link Domain}
 * 
 * 
 */
public interface ModuleInstance
{
	public Module module();

	/**
	 * This module's singular inputs.
	 * @return
	 */
	public List<InstanceInput> inputs();
	
	/**
	 * This module's singular outputs.
	 * @return
	 */
	public List<InstanceOutput> outputs();
	
	/**
	 * This modules execute function which will use the appropriate domain for execution.
	 * @return
	 */
	public boolean execute(); 

	public State state();

	public InstanceOutput output(String name);
	

	public InstanceInput input(String name);
	
	public Branch branch();
	
	
}
