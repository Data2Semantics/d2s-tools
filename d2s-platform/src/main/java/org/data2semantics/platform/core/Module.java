package org.data2semantics.platform.core;

import java.util.List;

import org.data2semantics.platform.core.data.Input;
import org.data2semantics.platform.core.data.Output;

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
		 * A module instance is a "copy" of this module with single values for 
		 * each input. This method returns the i-th module instance of this 
		 * module.
		 * 
		 * @param i
		 * @return
		 */
		public abstract ModuleInstance instance(int i);
		
		/**
		 * The number of instances for this module.
		 * @return
		 */
		public abstract int numInstances();
		
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
}
