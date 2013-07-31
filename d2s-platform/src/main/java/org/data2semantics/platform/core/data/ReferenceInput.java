package org.data2semantics.platform.core.data;

import org.data2semantics.platform.core.Module;

/**
 * An input which references an output of another module
 * 
 * @author Peter
 *
 */
public class ReferenceInput extends Input
{
	private Output refOutput;
	
	public ReferenceInput(Module module, String name, DataType dataType,
			Output refOutput)
	{
		super(name, dataType, module);
		
		this.refOutput = refOutput;
	}
	
	public Output reference()
	{
		return refOutput;
	}

}
