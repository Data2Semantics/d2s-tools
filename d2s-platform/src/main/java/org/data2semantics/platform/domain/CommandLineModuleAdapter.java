package org.data2semantics.platform.domain;

import java.util.List;

import org.data2semantics.platform.core.IterationStrategy;
import org.data2semantics.platform.core.Module;
import org.data2semantics.platform.core.ModuleInstance;
import org.data2semantics.platform.core.State;
import org.data2semantics.platform.core.Workflow;
import org.data2semantics.platform.core.data.Input;
import org.data2semantics.platform.core.data.Output;

/**
 * This clas is responsible as an adapter to command line
 * @author wibisono
 *
 */

public class CommandLineModuleAdapter  implements Module {

	public Workflow workflow()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public List<Output> outputs()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public List<Input> inputs()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Input input(String name)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Output output(String name)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public ModuleInstance instance(int i)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public int numInstances()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	public int rank()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	public int repeats()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<ModuleInstance> instances() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean finished() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean ready() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void instantiate() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean instantiated() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String source() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String name() {
		// TODO Auto-generated method stub
		return null;
	}

	

	

}
