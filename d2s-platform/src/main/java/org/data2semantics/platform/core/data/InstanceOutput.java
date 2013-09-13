package org.data2semantics.platform.core.data;

import org.data2semantics.platform.core.Module;
import org.data2semantics.platform.core.ModuleInstance;

/**
 * Class which hold the output of module execution
 * 
 * @author wibisono
 *
 */
public class InstanceOutput extends Output
{
	private Output original;
	private ModuleInstance instance;
	private Object value = null;
	private long creationTime = 0;
	
	public InstanceOutput(Module module, Output original, ModuleInstance instance)
	{
		super(original.name(), original.description(), module, original.dataType());
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
	
	public Object value(){
		return value;
	}
	
	public void setValue(Object val){
		value = val;
		creationTime = System.currentTimeMillis();
	}
	
	public long creationTime() {
		return creationTime;
	}
	
	public String toString(){
		return name() + ": "+value;
	}
}
