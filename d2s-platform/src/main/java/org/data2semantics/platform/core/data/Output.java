package org.data2semantics.platform.core.data;

import java.util.List;

import org.data2semantics.platform.core.Module;
import org.data2semantics.platform.core.State;

public class Output implements Data 
{
	private String name;
	private Module module;
	private DataType dataType;
	
	public Output(String name, Module module, DataType dataType)
	{
		super();
		this.name = name;
		this.module = module;
		this.dataType = dataType;
	}

	/**
	 * @return The inputs which reference this output.
	 */
	public Module module()
	{
		return module;
	}

	public String name()
	{
		return name;
	}

	public DataType getDataType()
	{
		return dataType;
	}
	
}
