package org.data2semantics.platform.core.data;

import org.data2semantics.platform.core.Module;
import org.data2semantics.platform.core.ModuleInstance;

/**
 * This input type represents an
 * 
 * @author Peter
 *
 */
public class InstanceInput extends Input
{
	private Input original;
	private ModuleInstance instance;
	
	public InstanceInput(String name, Module module, DataType dataType, Input original, ModuleInstance instance)
	{
		super(name, dataType, module);
		this.original = original;
		this.instance = instance;
	}

	
	/**
	 * The original input of which this is an instance
	 * @return
	 */
	public Input original()
	{
		return original;
	}
	
	/**
	 * The module instance for this instance input
	 * @return
	 */
	public ModuleInstance instance()
	{
		return instance;
	}

}
