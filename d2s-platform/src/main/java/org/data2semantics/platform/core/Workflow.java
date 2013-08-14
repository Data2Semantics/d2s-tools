package org.data2semantics.platform.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.data2semantics.platform.core.data.DataType;
import org.data2semantics.platform.core.data.Input;
import org.data2semantics.platform.core.data.JavaType;
import org.data2semantics.platform.core.data.MultiInput;
import org.data2semantics.platform.core.data.Output;
import org.data2semantics.platform.core.data.RawInput;
import org.data2semantics.platform.core.data.ReferenceInput;
import org.data2semantics.platform.domain.Domain;
import org.data2semantics.platform.exception.InconsistentWorkflowException;
import org.data2semantics.platform.resourcespace.ResourceSpace;
import org.data2semantics.platform.wrapper.SimpleModuleWrapper;

/**
 * This class represents a workflow. We guarantee the following:
 * 
 * <ul>
 * <li>After creation, a the object represents a consistent workflow.</li>
 * <li>The modules are sorted by rank, and can be executed in order.</li>
 * </ul>
 * 
 * The workflow class is immutable. 
 * 
 * 
 * @author Peter
 *
 */
public final class Workflow {
	
	// * Map from names to modules
	private Map<String, WorkflowBuilder.ModuleImpl> modules = new LinkedHashMap<String, WorkflowBuilder.ModuleImpl>();
	
	private ArrayList<Module> sortedList = null;
	
		
	/**
	 * Name of this workflow
	 */
	private String name;
	
	private Workflow() {
		
	}

//	/**
//	 * Get modules within this workflow which is in the given state
//	 * 
//	 * @param expectedState
//	 * @return
//	 */
//	public List<ModuleInstance> modules(State expectedState)
//	{
//		List<ModuleInstance> result  = new ArrayList<ModuleInstance>();
//		
//		
//		for(WorkflowBuilder.ModuleImpl module : modules.values()){
//			List<ModuleInstance> mInstances = module.instances();
//			for(ModuleInstance m : mInstances){
//			if(m.state().equals(expectedState))
//				result.add(m);
//			}
//		}
//		return result;
//	}
	
	/**
	 * @return the name
	 */
	public String name() {
		return name;
	}
	
	public List<Module> modules(){
			if(sortedList != null) return sortedList;
			
			sortedList = new ArrayList<Module>(modules.values());
			Collections.sort(sortedList, new ModuleComparator());
			return sortedList;
	}
	
	private class ModuleComparator implements Comparator<Module>
	{

		public int compare(Module first, Module second)
		{
			return Double.compare(first.rank(), second.rank());
		}
	}
	
	public static WorkflowBuilder builder()
	{
		return new WorkflowBuilder();
	}
	
	/**
	 * A builder class to construct workflows
	 * 
	 * TODO: How to communicate a list of references or a list of mixed raw
	 * values and references?
	 * 
	 * @author Peter
	 *
	 */
	public static class WorkflowBuilder
	{	
		private boolean dead = false;
		
		
		// * Cached references (stored until all modules have been created).
		private List<List<Object>> references = new ArrayList<List<Object>>();
		
		private Workflow workflow;
		
		private WorkflowBuilder() {
			workflow = new Workflow();
		}

		/**
		 * Add a module to the workflow
		 * 
		 * @param name
		 * @return
		 */
		public WorkflowBuilder module(String name, Domain domain)
		{
			check();
			
			if(! workflow.modules.containsKey(name))
				workflow.modules.put(name, new ModuleImpl(workflow, name, domain));
			else
				throw new IllegalArgumentException("Module ("+name+") already exists.");
			
			return this;
		}
		
		public WorkflowBuilder name(String name)
		{
			check();
			workflow.name = name;
			return this;
		}
		
		public WorkflowBuilder source(String moduleName, String source)
		{
			check();
			if(! workflow.modules.containsKey(moduleName))
				throw new IllegalArgumentException("Module ("+moduleName+") does not exist.");
			
			ModuleImpl module = workflow.modules.get(moduleName);
			module.setSource(source);

			return this;
		}
		
		/**
		 * Add an atomic value to the workflow
		 * 
		 * @param module
		 * @param name
		 * @param value
		 * @return
		 */
		public WorkflowBuilder rawInput(String moduleName, String description, String name, Object value, DataType type)
		{
			check();
			if(! workflow.modules.containsKey(moduleName))
				throw new IllegalArgumentException("Module ("+name+") does not exist.");
			
			ModuleImpl module = workflow.modules.get(moduleName);
			module.addInput(name, description, value, type);

			return this;
		}

		/**
		 * Add a multivalue input to the workflow
		 * 
		 * @param module
		 * @param name
		 * @param value
		 * @return
		 */
		public WorkflowBuilder multiInput(String moduleName, String description, String name, List<Object> value, DataType type)
		{
			check();
			if(! workflow.modules.containsKey(moduleName))
				throw new IllegalArgumentException("Module ("+moduleName+") does not exist.");
			
			ModuleImpl module = workflow.modules.get(moduleName);
			module.addMultiInput(name, description, value, type);

			return this;
		}
		
		/**
		 * Add a reference input to the workflow
		 * @param module
		 * @param name
		 * @param reference
		 * @return
		 */
		public WorkflowBuilder refInput(String moduleName, String inputName, String description, String referencedModule, String referencedOutput, DataType type)
		{
			check();
			if(! workflow.modules.containsKey(moduleName))
				throw new IllegalArgumentException("Module ("+moduleName+") does not exist.");
			
			references.add(Arrays.asList(moduleName, inputName, description, referencedModule, referencedOutput, type));

			return this;
		}
		
		public WorkflowBuilder output(String moduleName, String name, String description, DataType type)
		{
			check();
			if(! workflow.modules.containsKey(moduleName))
				throw new IllegalArgumentException("Module ("+name+") does not exist.");
			
			ModuleImpl module = workflow.modules.get(moduleName);
			module.addOutput(name, description, type);

			return this;
		}

		/**
		 * Returns the workflow object
		 * 
		 * Note that this method should be called only once. After it has been 
		 * called, the @WorkflowBuilder will "die", and a call to any of its 
		 * methods will results in an exception.
		 *  
		 * @return
		 */
		public Workflow workflow()
		{
			List<String> errors = new ArrayList<String>();
			
			if(! consistent(errors))
				throw new InconsistentWorkflowException(errors);
			
			for(List<Object> reference : references)
			{
				String moduleName = (String) reference.get(0),
				       inputName = (String) reference.get(1),
				       description = (String) reference.get(2),
				       refModuleName = (String) reference.get(3),
				       refOutputName = (String) reference.get(4);
				
				DataType type = (DataType) reference.get(5);
				
				ModuleImpl module = workflow.modules.get(moduleName),
				           refModule = workflow.modules.get(refModuleName);
				
				Output refOutput = refModule.output(refOutputName);
				Domain inputDomain = module.domain();
			
				DataType outputType = refOutput.dataType();

				module.addRefInput(inputName, description, refOutput, type, false);
				
				if(inputDomain.typeMatches(refOutput, module.input(inputName))){
					// Single reference input case
					((ReferenceInput) module.input(inputName)).setMultiValue(false);
					
				} else
				if(isList(outputType)){
					((ReferenceInput) module.input(inputName)).setMultiValue(true);
							
				} else
					throw new InconsistentWorkflowException("Input type of ("+moduleName + "."+inputName+") and output type "+refOutput+"."+refOutputName+"does not match ");
					
			}
			
			// * Kill the WorkflowBuilder
			Workflow result = workflow;
			dead = true;
			workflow = null;
			
			return result;
		}
		
		private boolean isList(DataType outputType) {
			if(!(outputType instanceof JavaType)) return false;
			JavaType jType = (JavaType)outputType;
			return List.class.isAssignableFrom(jType.clazz());
		}

		/**
		 * Determines whether the builder currently represents a consistent, 
		 * runnable workflow.   
		 */
		public boolean consistent(List<String> errors)
		{
			int eSize = errors.size();
			
			if(workflow.name == null)
				errors.add("Workflow name not set.");

			for(List<Object> reference : references)
			{
				String moduleName = (String) reference.get(0),
				       inputName = (String) reference.get(1),
				       description = (String) reference.get(2),
				       referencedModule = (String) reference.get(3),
				       referencedOutput = (String) reference.get(4);
				
				if(! workflow.modules.containsKey(moduleName))
					errors.add("Module ("+moduleName+") does not exist.");

				if(! workflow.modules.containsKey(referencedModule))
					errors.add("Module ("+referencedModule+") does not exist.");
				else if(! workflow.modules.get(referencedModule).hasOutput(referencedOutput))
					errors.add("Referenced module ("+referencedModule+") does not have output "+referencedOutput+".");

			}
			
			return eSize == errors.size();
				
		}
		
		private void check()
		{
			if(dead)
				throw new IllegalStateException("This workflowbuilder is dead. The method workflow has been called.");
		}
		
		private static class ModuleImpl extends AbstractModule 
		{

			public ModuleImpl(Workflow workflow, String name, Domain domain)
			{
				super(workflow, domain);
				this.name = name;
			}

			public void addRefInput(String inputName, String description, Output referencedOutput, DataType type, boolean multiRef)
			{
				
				if(inputs.containsKey(inputName))
					throw new IllegalArgumentException("Module ("+name()+") already contains input with the given name ("+inputName+")");
				
				inputs.put(inputName, new ReferenceInput(this, inputName, description, type, referencedOutput, multiRef));
			}

			public boolean hasOutput(String output)
			{
				return outputs.containsKey(output);
			}

			public void addOutput(String name, String description, DataType type)
			{
				if(outputs.containsKey(name))
					throw new IllegalArgumentException("Module ("+name()+") already contains output with the given name ("+name+")");
				
				outputs.put(name, new Output(name, description, this, type));
			}

			public void setSource(String source)
			{
				this.source = source;
			}
			
			public void setDomain(Domain domain){
				this.domain = domain;
			}
			
			public void addMultiInput(String name, String description, List<Object> values, DataType type)
			{
				if(inputs.containsKey(name))
					throw new IllegalArgumentException("Module ("+name()+") already contains input with the given name ("+name+")");
				
				List<RawInput> rawInputs = new ArrayList<RawInput>(values.size());
				for(Object value : values)
					rawInputs.add(new RawInput(value, name, description,  type, this));
				
				inputs.put(name, new MultiInput(name, description, type, this, rawInputs));
			}

			public void addInput(String name, String description, Object value, DataType type)
			{
				if(inputs.containsKey(name))
					throw new IllegalArgumentException("Module ("+name()+") already contains input with the given name ("+name+")");
				
				inputs.put(name, new RawInput(value, description,  name, type, this));
			}

						
		}
		
	}
	
}
