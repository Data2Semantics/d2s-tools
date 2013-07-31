package org.data2semantics.platform.core.data;

import org.data2semantics.platform.core.Module;
import org.data2semantics.platform.core.ModuleInstance;

public class InstanceOutput extends Output
{
	private Output original;
	private ModuleInstance instance;
	
	public InstanceOutput(String name, Module module, DataType dataType, Output original, ModuleInstance instance)
	{
		super(name, module, dataType);
		this.original = original;
		this.instance = instance;
	}

	/**
	 * The original input of which this is an instance
	 * @return
	 */
	public Output original()
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
