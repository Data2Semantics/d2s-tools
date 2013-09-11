package org.data2semantics.platform.core.data;

import java.util.List;

import org.data2semantics.platform.core.Module;

/**
 * Represents an input to a module that has multiple values, either because of 
 * a direct parameter sweep, or a replicated module somewhere down the line. 
 * 
 * @author Peter
 *
 */
public class MultiInput extends Input
{

	// Not only raw input but allowing these multiple inputs to be also reference inputs.
	private List<? extends Input> inputs;
	
	public MultiInput(String name, String description, DataType dataType, Module module,
			List<? extends Input> inputs)
	{
		super(name, description, dataType, module);
		this.inputs = inputs;
	}

	/**
	 * Returns a list of single inputs that comprise this multi-input represents
	 * 
	 * @return
	 */
	public List<? extends Input> inputs()
	{
		return inputs;
	}
	
}
