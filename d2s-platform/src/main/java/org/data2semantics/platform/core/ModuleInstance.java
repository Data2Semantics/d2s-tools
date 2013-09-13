package org.data2semantics.platform.core;

import java.util.List;
import java.util.Map;

import org.data2semantics.platform.core.data.Input;
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

	
	public int moduleID();
	
	public long creationTime();
	
	public long startTime();
	
	public long endTime();
	
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

	// Output instances of this current module
	public InstanceOutput output(String name);
	

	// Input instances of this current module
	public InstanceInput input(String name);

	// Return branch of this instance, place holder
	public Branch branch();
	
	/**
	 * Universe which is defined as Maps of moduleName.inputName to values, of all parent modules for this current instance.
	 * @return
	 */
	public Map<Input, InstanceInput> universe();
	
	/**
	 * Check whether this module instance is compatible with given {@param universe}.
	 * 
	 * Module instances within a universe have similar input value assigned for parents that they share.
	 * 
	 * @param universe
	 * @return
	 */
	public boolean withinUniverse(Map<Input, InstanceInput> universe);
	
	
}
