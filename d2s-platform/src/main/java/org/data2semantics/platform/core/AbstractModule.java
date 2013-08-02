package org.data2semantics.platform.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.data2semantics.platform.core.data.Input;
import org.data2semantics.platform.core.data.InstanceInput;
import org.data2semantics.platform.core.data.InstanceOutput;
import org.data2semantics.platform.core.data.MultiInput;
import org.data2semantics.platform.core.data.Output;
import org.data2semantics.platform.core.data.RawInput;
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
public abstract class AbstractModule implements Module
{
	protected String name;
	protected Domain domain;
	protected String source;
	
	// Parent Workflow where this module belongs
	protected Workflow workflow;

	protected Map<String, Input>  inputs = new LinkedHashMap<String, Input>();
	protected Map<String, Output> outputs = new LinkedHashMap<String, Output>();
	
	protected List<ModuleInstance> instances = new ArrayList<ModuleInstance>();
	
	protected boolean instantiated = false;
	
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

    @Override
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
	
	public List<ModuleInstance> instances(){
		return Collections.unmodifiableList(instances);
	}
	
	public int repeats()
	{
		return 0;
	}

	@Override
	public void instantiate() {
		
		if(!ready())
			throw new IllegalStateException("Failed to instantiate, because the module is not ready");
		
		if(instantiated)
			throw new IllegalStateException("Module can't be instantiated twice");
		
		int [] inpIndexes = new int[inputs.size()];
		
		instantiateRec(0, inpIndexes);
		
		instantiated = true;
	}
	
	
	private void instantiateRec(int depth, int [] chosenIndexes) {
		if(depth == inputs().size()){
			// here we create the module
			ModuleInstanceImpl instance = new ModuleInstanceImpl();
			
			for(int i=0;i<inputs().size();i++){
				Input curInput = inputs().get(i);
				
				InstanceInput newInstanceInput = null;
				Object value = null;
				
				if(curInput instanceof MultiInput){
					
					MultiInput originalInput = (MultiInput)curInput;
					
					RawInput rawInput = originalInput.inputs().get(chosenIndexes[i]);
					
					value = rawInput.value();
				} else
				if(curInput instanceof ReferenceInput){
					
					ReferenceInput originalInput = (ReferenceInput) curInput;
					
					List<ModuleInstance> refInstances = originalInput.reference().module().instances();
					
					ModuleInstance mInstance = refInstances.get(chosenIndexes[i]);
					
					InstanceOutput theOutput = mInstance.output(originalInput.name());
					
					value = theOutput.value();
				} else
				if(curInput instanceof RawInput){
					
					// There will only be one option
					assert(chosenIndexes[i] == 0);
					
					RawInput originalInput = (RawInput) curInput;
					
					value = originalInput.value();
				}
				
				newInstanceInput = new InstanceInput( this, curInput, instance, value);
				
				instance.inputs.put(newInstanceInput.name(), newInstanceInput);
			
			}
			
			for(Output original : outputs.values()){
				InstanceOutput instanceOutput = new InstanceOutput( this, original, instance);
				instance.outputs.put(instanceOutput.name(), instanceOutput);
			}
			
			instances.add(instance);
		
			return;
		} 
		
		System.out.println("Depth " + depth);
		Input curInput = inputs().get(depth);
		System.out.println(" Check " + curInput.name() + " " + curInput.dataType() + " " + curInput.getClass());
		int nOptions = getNumberOfOptions(curInput);
		
		for(int i=0;i<nOptions;i++){
			chosenIndexes[depth] = i;
			instantiateRec(depth+1, chosenIndexes);
		}
		
	}

	private int getNumberOfOptions(Input curInput) {
		int nOptions = 0;
		
		if(curInput instanceof MultiInput){
			MultiInput originalInput = (MultiInput)curInput;
			nOptions = originalInput.inputs().size();
		} else
		if(curInput instanceof ReferenceInput){
			ReferenceInput originalInput = (ReferenceInput) curInput;
			nOptions = originalInput.reference().module().instances().size();
		} else
		if(curInput instanceof RawInput){
			RawInput originalInput = (RawInput) curInput;
			nOptions = 1;
		} else
			throw new IllegalStateException("Instantiation can't handle unknown Input type ");
		
		return nOptions;
	}

	@Override
	public boolean finished() {
		if(!instantiated())
			return false;
		
		for(ModuleInstance mi : instances){
			if(!mi.state().equals(State.FINISHED))
				return false;
		}
		
		return true;
	}
	
	@Override
	public boolean instantiated(){
		
		return instantiated;
	}
	
	@Override
	public boolean ready(){
		for(Input input : inputs())
			if(input instanceof ReferenceInput)
			{
				if(!((ReferenceInput)input).reference().module().finished())
					return false;
			}
		return true;
	}
	
	public String source(){
		return source;
	}
	
	private class ModuleInstanceImpl implements ModuleInstance
	{
		protected State state = State.READY;
		protected Map<String, InstanceInput> inputs = new LinkedHashMap<String, InstanceInput>();
		protected Map<String, InstanceOutput> outputs = new LinkedHashMap<String, InstanceOutput>();
		
	
		public ModuleInstanceImpl() {
		}

		public Module module()
		{
			return AbstractModule.this;
		}

		public List<InstanceInput> inputs()
		{
			return new ArrayList<InstanceInput>(inputs.values());
		}

		public List<InstanceOutput> outputs()
		{
			return new ArrayList<InstanceOutput>(outputs.values());
		}

		public boolean execute()
		{
			ArrayList<String> errors = new ArrayList<String>();
			Map<String, Object> results = new LinkedHashMap<String, Object>();
			
			boolean success = domain.execute(this, errors, results);
			
			for(String resultName : results.keySet()){
				outputs.get(resultName).setValue(results.get(resultName));
			}
			
			state = success ? State.FINISHED : State.FAILED;
			
			return success;
		}

		public State state()
		{
			return state;
		}
		
		@Override
		public InstanceOutput output(String name) {
			return outputs.get(name);
		}
		
		@Override
		public InstanceInput input(String name) {
			return inputs.get(name);
		}
	
	}
	
}

