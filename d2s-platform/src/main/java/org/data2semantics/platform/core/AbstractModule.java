package org.data2semantics.platform.core;

import java.util.ArrayList;
import java.util.List;

/**
 * This is a representation of a concrete module, which is annotated.
 * Abstract Module will be generated based on information prescribed in YAML, and annotation on original source code.
 * @author wibisono
 *
 */
public abstract class AbstractModule implements Module {
	
	protected String name;
	
	// Parent Workflow where this module belongs
	protected Workflow parent;
	
	// Module state which depends on the state of the inputs
	protected State currentState;

	List<Input> inputs = new ArrayList<Input>();
	List<Output> outputs = new ArrayList<Output>();
	
	IterationStrategy iterationStrategy;
	
	public AbstractModule(Workflow parent) {
		this.parent = parent;
	}
	
	public void setParent(Workflow parent) {
		this.parent = parent;
	}
	
	public Workflow getParent(){
		return parent;
	}

	public List<Input> getInputs() {
		return inputs;
	}
	public List<Output> getOutputs() {
		return outputs;
	}
	
	public void setInputs(List<Input> inputs) {
		this.inputs = inputs;
	}
	
	public void setOutputs(List<Output> outputs) {
		this.outputs = outputs;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * This should be generated/instantiated whenever we generate source code based on the annotated source code.
	 * Execution would change the state of the module. Results must be handled after execution.
	 */
	
	public abstract boolean execute();
	
	
	/**
	 * Result from this module is stored in output with a certain reference key.
	 * @param referenceKey
	 * @return
	 */
	public abstract Object getReference(String referenceKey);
		
	
	public State getState(){
		return currentState;
	}
	
	public void setState(State newState){
		currentState  = newState;
	}
	
	public boolean isReady(){
		return currentState == State.READY;
	}
	
	public boolean isBlocked(){
		return currentState == State.BLOCKED;
	}
	
	public IterationStrategy getIterationStrategy() {
		return iterationStrategy;
	}
	
	public void setIterationStrategy(IterationStrategy strategy) {
		iterationStrategy = strategy;
	}
	
}

