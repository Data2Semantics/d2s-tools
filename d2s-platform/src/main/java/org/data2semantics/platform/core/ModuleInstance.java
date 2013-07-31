package org.data2semantics.platform.core;

import java.util.List;

import org.data2semantics.platform.core.data.InstanceInput;
import org.data2semantics.platform.core.data.InstanceOutput;

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
	
	public boolean execute(); 

	public State state();

}
