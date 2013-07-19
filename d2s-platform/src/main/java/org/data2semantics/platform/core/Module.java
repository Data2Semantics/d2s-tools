package org.data2semantics.platform.core;

import java.util.List;

/**
 * Define what is module, something that we can execute
 * @author wibisono
 *
 */
public interface Module {

		public boolean execute();

		public State getState();
		
		public void setState(State s);

		public abstract void setOutputs(List<Output> outputs);

		public abstract void setInputs(List<Input> inputs);

		public abstract List<Output> getOutputs();

		public abstract List<Input> getInputs();
		
		public IterationStrategy getIterationStrategy();
		
		public void setIterationStrategy(IterationStrategy strategy);
}
