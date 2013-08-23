package org.data2semantics.platform.core;

import java.util.List;

import org.data2semantics.platform.core.data.Input;
import org.data2semantics.platform.core.data.Output;
import org.data2semantics.platform.domain.Domain;

/**
 * Define what is module, something that we can execute.
 * 
 * Note that this is actually a wrapper around the the original java class 
 * tagged @Module by the workflow author.
 * 
 * Apart from inheritance, modules should be immutable after creation. 
 * 
 * @author wibisono
 *
 */
public interface Module 
{
		
		public Workflow workflow();

		public abstract List<Output> outputs();

		public abstract List<Input> inputs();

		public Input input(String name);
		
		public Output output(String name);
		
		/**
		 * Function to return all instances of this module, will return null unless the module is {@link #ready}
		 * 
		 * @return
		 */
		public abstract List<ModuleInstance> instances();
		
		/**
		 * Determines the rank of the given module. 
		 * 
		 * The rank is defined as follows: A dependency is another module which 
		 * provides and input to this module. If a module has no dependencies (ie.
		 * all its inputs are atomic), it has rank 0. Otherwise, a modules rank is 
		 * 1 + the maximum rank among its dependencies. 
		 * 
		 * @param module
		 * @return
		 */
		public int rank();
		
		/**
		 * Whether this module is set to repeat. Repeats inherited from 
		 * dependencies are not reflected in this value. Ie. if one of this 
		 * modules' dependencies has "repeat: 3" in the yaml definition, but 
		 * this module doesn't have a repeat key, this moethod returns 1,
		 * but the module will still be run three times.
		 * 
		 * @return
		 */
		public int repeats();
		
		
		/**
		 * Whether all instances of this module are successfully executed.
		 * 
		 * @return
		 */
		public boolean finished();

	
		/**
		 * Whether all the dependencies of this module are already finished.
		 * 
		 * @return
		 */
		public boolean ready();
		
		
		/**
		 * Create necessary {@link ModuleInstance}  for this module
		 */
		public void instantiate();
		
		/**
		 * Check whether module instances have been created
		 * @return
		 */
		public boolean instantiated();
		
		/**
		 * Getter for the module source
		 * @return source
		 */
		public String source();
		
		/**
		 * The name of this module.
		 * @return
		 */
		public String name();
		
		/**
		 * Associated domain of this module.
		 * @return
		 */
		public Domain domain();

		public boolean dependsOn(Module curModule);
}
