package org.data2semantics.platform.core.data;

import org.data2semantics.platform.core.Module;


public abstract class Input implements Data 
{
	private String name, description;
	private DataType dataType;
	private Module module;
	
	public Input(String name, String description,  DataType dataType, Module module)
	{
		this.name = name;
		this.dataType = dataType;
		this.module = module;
		this.description = description;
	}
	
	public String name()
	{
		return name;
	}
	
	public String description()
	{
		return description;
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
