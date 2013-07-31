package org.data2semantics.platform.core;

/**
 * Represents a datatype in the java class hierarchy.
 * 
 * @author Peter
 *
 */
public class JavaDataType
{
	private Class<?> cls;

	public JavaDataType(Class<?> cls)
	{
		this.cls = cls;
	}

	public Class<?> type()
	{
		return cls;
	}
	
}
