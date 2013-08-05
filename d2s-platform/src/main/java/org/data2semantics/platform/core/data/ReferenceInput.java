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
	
	boolean multiValue = false;
	
	public ReferenceInput(Module module, String name, DataType dataType,
			Output refOutput, boolean multiValue)
	{
		super(name, dataType, module);
		
		this.refOutput = refOutput;
		
		this.multiValue = multiValue;
	}
	
	public Output reference()
	{
		return refOutput;
	}

	public boolean multiValue(){
		return multiValue;
	}

	public void setMultiValue(boolean b) {
		multiValue = b;
	}
}
