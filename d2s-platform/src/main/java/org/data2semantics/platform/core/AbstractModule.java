package org.data2semantics.platform.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.data2semantics.platform.core.data.Input;
import org.data2semantics.platform.core.data.InstanceInput;
import org.data2semantics.platform.core.data.InstanceOutput;
import org.data2semantics.platform.core.data.Output;
import org.data2semantics.platform.core.data.ReferenceInput;
import org.data2semantics.platform.domain.Domain;

/**
 * This is a representation of a concrete module, which is annotated.
 * Abstract Module will be generated based on information prescribed in YAML, 
 * and annotation on original source code.
 * 
 * 
 * @author wibisono
 *
 */
abstract class AbstractModule implements Module
{
	protected String name;
	protected Domain domain;
	protected String source;
	
	// Parent Workflow where this module belongs
	protected Workflow workflow;

	protected Map<String, Input>  inputs = new LinkedHashMap<String, Input>();
	protected Map<String, Output> outputs = new LinkedHashMap<String, Output>();
		
	public AbstractModule(Workflow workflow, Domain domain) {
		this.domain = domain;
		this.workflow = workflow;
	}
	
	public Workflow workflow(){
		return workflow;
	}

	public List<Input> inputs() {
		return new ArrayList<Input>(inputs.values());
	}
	public List<Output> outputs() {
		return new ArrayList<Output>(outputs.values());
	}
	

	public Input input(String name)
	{
		if(! inputs.containsKey(name))
			throw new IllegalArgumentException("Input "+name+" does not exist.");

		return inputs.get(name);
	}

	public Output output(String name)
	{
		if(! outputs.containsKey(name))
			throw new IllegalArgumentException("Output "+name+" does not exist.");

		return outputs.get(name);	
	}
	
	public String name() 
	{
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	// @Override // why does this give a compile error?
	public int rank()
	{
		int rank = 0;
		for(Input input : inputs())
			if(input instanceof ReferenceInput)
			{
				int dependencyRank = ((ReferenceInput)input).reference().module().rank();
				rank = Math.max(rank, dependencyRank);
			}
		
		return rank + 1; 
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

	public int repeats()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	private class ModuleInstanceImpl implements ModuleInstance
	{
		protected State state = State.READY;
		protected List<InstanceInput> inputs;
		protected List<InstanceOutput> outputs;
		
		public ModuleInstanceImpl(int i)
		{
			// TODO
		}
		
		public Module module()
		{
			return AbstractModule.this;
		}

		public List<InstanceInput> inputs()
		{
			return Collections.unmodifiableList(inputs);
		}

		public List<InstanceOutput> outputs()
		{
			return Collections.unmodifiableList(outputs);
		}

		public boolean execute()
		{
			boolean success = domain.execute(this, new ArrayList<String>());
			
			state = success ? State.FINISHED : State.READY;
			
			return success;
		}

		public State state()
		{
			return state;
		}
		
	}
	
}

