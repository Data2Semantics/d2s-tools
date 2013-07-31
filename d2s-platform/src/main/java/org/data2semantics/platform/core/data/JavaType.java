package org.data2semantics.platform.core.data;

public class JavaType implements DataType
{
	
	protected Class<?> javaClass;
	
	
	public JavaType(Class<?> javaClass)
	{
		super();
		this.javaClass = javaClass;
	}

	@Override
	public String name()
	{
		return javaClass.getCanonicalName();
	}

	@Override
	public String domain()
	{
		return "java";
	}

}
