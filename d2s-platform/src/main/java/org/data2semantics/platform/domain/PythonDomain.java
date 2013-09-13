package org.data2semantics.platform.domain;

import java.util.List;
import java.util.Map;

import org.data2semantics.platform.annotation.DomainDefinition;
import org.data2semantics.platform.core.ModuleInstance;
import org.data2semantics.platform.core.data.DataType;
import org.data2semantics.platform.core.data.Input;
import org.data2semantics.platform.core.data.Output;

@DomainDefinition(prefix="python")
public class PythonDomain implements Domain
{
	private static PythonDomain domain = new PythonDomain();

	public boolean typeMatches(Output output, Input input)
	{
		return false;
	}

	public boolean check(ModuleInstance instance, List<String> errors)
	{
		return false;
	}

	public List<DataType> conversions(DataType type)
	{
		return null;
	}

	public boolean execute(ModuleInstance instance, List<String> errors)
	{
		return false;
	}
	
	public static PythonDomain domain()
	{
		return domain;
	}

	public DataType inputType(String source, String name)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public DataType outputType(String source, String name)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public List<String> outputs(String source)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public boolean valueMatches(Object value, DataType type)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean execute(ModuleInstance instance, List<String> errors,
			Map<String, Object> results) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String inputDescription(String source, String name)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String outputDescription(String source, String name)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean validate(String source, List<String> errors) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean printInput(String source, String input)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean printOutput(String source, String input)
	{
		// TODO Auto-generated method stub
		return false;
	}
	
}
