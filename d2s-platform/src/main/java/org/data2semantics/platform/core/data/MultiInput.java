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

	private List<RawInput> inputs;
	
	
	
	public MultiInput(String name, DataType dataType, Module module,
			List<RawInput> inputs)
	{
		super(name, dataType, module);
		this.inputs = inputs;
	}

	/**
	 * Returns a list of single inputs that comprise this multi-input represents
	 * 
	 * @return
	 */
	public List<RawInput> inputs()
	{
		return inputs;
	}
}
