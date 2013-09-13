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
	private Object value;
	private InstanceOutput instanceOutput;
	
	public InstanceInput(Module module, Input original, Object value)
	{
		super(original.name(), original.description(), original.dataType(), module);
		this.original = original;
		this.value = value;
		this.instance = null;
		this.instanceOutput = null;

	}

	public InstanceInput(Module module, Input original, ModuleInstance instance, Object value)
	{
		super(original.name(), original.description(), original.dataType(), module);
		this.original = original;
		this.instance = instance;
		this.value = value;
		this.instanceOutput = null;
	}

	
	public InstanceInput(Module module, Input original,
			Object nextValue, InstanceOutput refInstanceOutput) {
		super(original.name(), original.description(), original.dataType(), module);
		
		this.original = original;
		this.value = nextValue;
		this.instance = null;
		this.instanceOutput = refInstanceOutput;
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
	
	public InstanceOutput instanceOutput(){
		return instanceOutput;
	}
	
	public void setInstance(ModuleInstance instance){
		this.instance = instance;
	}
	
	public Object value(){
		return value;
	}

	public String toString(){
		return name() + ": "+value;
	}
}
