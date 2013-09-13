package org.data2semantics.platform.core.data;

import org.data2semantics.platform.core.Module;

public class RawInput extends Input
{
	private Object value = null;


	public RawInput(Object value, String name, String description, DataType dataType,
			Module module)
	{
		super(name, description, dataType, module);
		this.value = value;
	}
	
	public RawInput(Object value, String name, String description, DataType dataType,
			Module module, boolean print)
	{
		super(name, description, dataType, module, print);
		this.value = value;
	}

	public Object value()
	{
		return value;
	}
	
	/**
	 * Signal's whether the value is available.
	 * 
	 * @return
	 */
	public boolean available()
	{
		return true;
	}
}
