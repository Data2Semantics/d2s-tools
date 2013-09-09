package org.data2semantics.platform.core.data;

public class CommandLineType implements DataType
{
	
	protected Class<?> javaClass;
	
	
	public CommandLineType(Class<?> javaClass)
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

	public Class<?> clazz(){
		return javaClass;
	}
	
	public String toString(){
		return clazz().toString();
	}
	
	@Override
	public boolean equals(Object obj) {
	
		return javaClass.equals(((CommandLineType)obj).clazz());
	}
}
