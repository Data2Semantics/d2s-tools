package org.data2semantics.platform.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.security.auth.login.AccountNotFoundException;

import org.data2semantics.platform.core.data.DataType;
import org.data2semantics.platform.core.data.Input;
import org.data2semantics.platform.core.data.InstanceInput;
import org.data2semantics.platform.core.data.InstanceOutput;
import org.data2semantics.platform.core.data.JavaType;
import org.data2semantics.platform.core.data.MultiInput;
import org.data2semantics.platform.core.data.Output;
import org.data2semantics.platform.core.data.RawInput;
import org.data2semantics.platform.core.data.ReferenceInput;
import org.data2semantics.platform.domain.Domain;
import org.data2semantics.platform.util.PlatformUtil;

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
			}else 
				if(input instanceof MultiInput){
					MultiInput mi = (MultiInput) input;
					for(Input i : mi.inputs()){
						if(i instanceof ReferenceInput){
							int dependencyRank = ((ReferenceInput)i).reference().module().rank();
							rank = Math.max(rank, dependencyRank);
						}
					}
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

	

	
	public boolean dependsOn(Module curModule) {
		if(parents().contains(curModule))
			return true;
		
		for(Module p : parents()){
			if(p.dependsOn(curModule))
				return true;
		}
		
		return false;
	}

	private Collection<Module> parents() {
		
		Set<Module> result = new LinkedHashSet<Module>();
		
		for(Input i : inputs()){
			if(i instanceof ReferenceInput){
				result.add(((ReferenceInput) i).reference().module());
			} else
			if(i instanceof MultiInput){
				for(Input ii : ((MultiInput) i).inputs()){
					if(ii instanceof ReferenceInput){
						result.add(((ReferenceInput) ii).reference().module());
					}
				}
			}
		}
		
		return result;
	}


	

	@Override
	public void instantiate(){

		instantiateUniverse();
		instantiated = true;
	}

	
	public void instantiateUniverse(){
		if(!ready())
			throw new IllegalStateException("Failed to instantiate, because the module is not ready");
		
		if(instantiated)
			throw new IllegalStateException("Module can't be instantiated twice");
		
		ArrayList<Object> values = new ArrayList<Object>();
		Map<String, Object> universe = new LinkedHashMap<String, Object>();
		List<AbstractModule.BranchImpl> parentBranches = new ArrayList<AbstractModule.BranchImpl>();
		
		instantiateInputRec(values, universe, parentBranches, 0);
	}

	private void instantiateInputRec(ArrayList<Object> values,
			Map<String, Object> universe, List<AbstractModule.BranchImpl> parentBranches,  int depth) {
		
			if(depth == inputs().size()){
				
				ModuleInstanceImpl newInstance = new ModuleInstanceImpl();
				
				for(int i=0;i<inputs().size();i++){
					InstanceInput ii = new InstanceInput(this, inputs().get(i), newInstance, values.get(i));
					newInstance.inputs.put(ii.name(), ii);
				}
				
				for(Output original : outputs.values()){
					InstanceOutput instanceOutput = new InstanceOutput( this, original, newInstance);
					newInstance.outputs.put(instanceOutput.name(), instanceOutput);
				}
				
				
				if(parentBranches.size() == 0)
					parentBranches.add((BranchImpl)workflow().rootBranch);
				
				Branch newBranch = BranchImpl.createChild(newInstance, parentBranches);
				
				newInstance.setBranch(newBranch);
				
				instances.add(newInstance);
				
				return;
			}
			
			Input curInput = inputs().get(depth);
			if(curInput instanceof RawInput){
				ArrayList<Object> nextValues = new ArrayList<Object>(values);
				nextValues.add(((RawInput) curInput).value());
				instantiateInputRec(nextValues, universe, parentBranches, depth+1);
			
			} else
			if(curInput instanceof ReferenceInput){
				handleReferenceInput(values, universe, parentBranches, depth, curInput);
			
			} else
			if(curInput instanceof MultiInput){
				
				for(Input curMultiRefInput : ((MultiInput) curInput).inputs()){
					
					if(curMultiRefInput instanceof RawInput){
						
						ArrayList<Object> nextValues = new ArrayList<Object>(values);
						nextValues.add(((RawInput) curMultiRefInput).value());
						instantiateInputRec(nextValues, universe, parentBranches, depth+1);
						
					} else
					if(curMultiRefInput instanceof ReferenceInput){
						
						handleReferenceInput(values, universe, parentBranches,	depth, curMultiRefInput);
						
					} else
						throw new IllegalArgumentException("Input type not recognized " + curMultiRefInput);
					
				}
				
			}
		
	}

	private void handleReferenceInput(ArrayList<Object> values,
			Map<String, Object> universe,
			List<AbstractModule.BranchImpl> parentBranches, int depth,
			Input curMultiRefInput) {
		ReferenceInput ri = (ReferenceInput) curMultiRefInput;
		List<ModuleInstance> parentInstances = ri.reference().module().instances();
		for(ModuleInstance mi : parentInstances){
			if(mi.withinUniverse(universe) ){
					Map<String, Object> nextUniverse = new LinkedHashMap<String, Object>(universe);
					nextUniverse.putAll(mi.universe());
					
					
					List<AbstractModule.BranchImpl> nextParentBranches = new ArrayList<AbstractModule.BranchImpl>(parentBranches);
					nextParentBranches.add((BranchImpl)mi.branch());
					
					Object value = mi.output(((ReferenceInput) curMultiRefInput).reference().name()).value();
					
					if(!ri.multiValue()){
						ArrayList<Object> nextValues = new ArrayList<Object>(values);
						nextValues.add(value);

						instantiateInputRec(nextValues, nextUniverse, nextParentBranches, depth+1);
					} else {
						for(Object v : (List<Object>)value){
							ArrayList<Object> nextValues = new ArrayList<Object>(values);
							nextValues.add(v);

							instantiateInputRec(nextValues, nextUniverse, nextParentBranches, depth+1);
						}
					}
			}
		}
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
			} else 
			if(input instanceof MultiInput){
				MultiInput mi = (MultiInput) input;
				for(Input i : mi.inputs()){
					if(i instanceof ReferenceInput){
						if(!((ReferenceInput)i).reference().module().finished())
							return false;
					}
				}
			}
		return true;
	}
	
	public String source(){
		return source;
	}
	
	public Domain domain(){
		return domain;
	}
	
	private class ModuleInstanceImpl implements ModuleInstance
	{
		protected State state = State.READY;
		protected Map<String, InstanceInput> inputs = new LinkedHashMap<String, InstanceInput>();
		protected Map<String, InstanceOutput> outputs = new LinkedHashMap<String, InstanceOutput>();
		
		Branch branch;
		
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
		
			// After execution, set values of output so that it can be referenced later on.
			for(String resultName : results.keySet())
				outputs.get(resultName).setValue(results.get(resultName));
			
			state = success ? State.FINISHED : State.FAILED;
			
			return success;
		}

		public State state()
		{
			return state;
		}
		
		@Override
		public InstanceOutput output(String name) {
			
			if(! outputs.containsKey(name))
				throw new IllegalArgumentException("Output '"+name+"' does not exist (module: "+module().source()+").");
			return outputs.get(name);
		}
		
		@Override
		public InstanceInput input(String name) {
			
			if(! inputs.containsKey(name))
				throw new IllegalArgumentException("Input '"+name+"' does not exist (module: "+module().source()+").");

			return inputs.get(name);
		}
		
		@Override
		public Branch branch() {
			return branch;
		}
		
		public void setBranch(Branch b){
			this.branch = b;

		}
	
		public Map<String, Object> universe(){
			Map<String, Object> valueMap = new LinkedHashMap<String, Object>();
			
			for(InstanceInput i : inputs()){
				valueMap.put(name()+"."+i.name(), i.value() );
			}
			for(Branch b : branch().parents()){
				if(b.point() != null)
				 valueMap.putAll((b.point()).universe());
			}
			return valueMap;
		}
		
		@Override
		public boolean withinUniverse(Map <String, Object> otherParentValueMap) {
			Map <String, Object> curParentValueMap = universe();
			
			for(String moduleInputKey : otherParentValueMap.keySet()){
				// Ignoring Different set of module/input
				if(!curParentValueMap.containsKey(moduleInputKey)) continue;
				
				// If the same module/input key are assigned different value we are on different universe/scope
				if(!curParentValueMap.get(moduleInputKey).equals(otherParentValueMap.get(moduleInputKey)))
					return false;
			}
			
			return true;
		}
	}

	static class BranchImpl implements Branch {
		
		List<Branch> parents 	= new ArrayList<Branch>();
		List<Branch> children 	= new ArrayList<Branch>();
		
		ModuleInstance creationPoint = null;
		
		@Override
		public List<Branch> parents() {
			return Collections.unmodifiableList(parents);
		}

		@Override
		public List<Branch> children() {
			return Collections.unmodifiableList(children);
		}

		@Override
		public Collection<Branch> ancestors() {
			Set<Branch> result = new LinkedHashSet<Branch>();
			
			result.addAll(parents);
			for(Branch parent : parents)
				result.addAll(parent.ancestors());
			
			return result;
		}

		@Override
		public Collection<Branch> descendants() {
			Set<Branch> result = new LinkedHashSet<Branch>();
			
			result.addAll(children);
			for(Branch child : children)
				result.addAll(child.descendants());
			
			return result;
		}

		@Override
		public ModuleInstance point() {
			
			return creationPoint;
		} 
		
		
		static Branch createChild(ModuleInstance childPoint, List<BranchImpl> parents){
				
				BranchImpl newBranch = new BranchImpl();
				newBranch.creationPoint = childPoint;
			
				for(BranchImpl p : parents){
					p.children.add(newBranch);
				}
				
				newBranch.parents.addAll(parents);
				
				
				return newBranch;
		}

		@Override
		public Collection<Branch> siblings() {
			List<ModuleInstance> instanceSiblings = point().module().instances();
			Set<Branch> result = new HashSet<Branch>();
			for(ModuleInstance mi : instanceSiblings){
				if(mi.branch().equals(this)) continue;
				result.add(mi.branch());
			}
			
			return result;
		}
		
		
		
	}
}

