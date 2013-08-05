package org.data2semantics.platform.core.data;

import org.data2semantics.platform.core.Module;


public abstract class Input implements Data 
{
	private String name;
	private DataType dataType;
	private Module module;

	
	public Input(String name, DataType dataType, Module module)
	{
		this.name = name;
		this.dataType = dataType;
		this.module = module;
	}
	
	public String name()
	{
		return name;
	}
	
	public DataType dataType()
	{
		return dataType;
	}

	public Module module()
	{
		return module;
	}

	public String toString(){
		return name + " " + dataType;
	}
}
