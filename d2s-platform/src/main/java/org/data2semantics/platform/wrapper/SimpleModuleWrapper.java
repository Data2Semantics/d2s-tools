package org.data2semantics.platform.wrapper;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.data2semantics.platform.core.AbstractModule;
import org.data2semantics.platform.core.State;
import org.data2semantics.platform.core.Workflow;
import org.data2semantics.platform.execution.ExecutionProfile;
import org.data2semantics.platform.resourcespace.ResourceSpace;
import org.data2semantics.platform.util.PlatformUtil;


/**
 * One of the three wrapper for modules based on annotation.
 * The execute part, should then be delegated to the actual annotated class.
 * @author wibisono
 *
 */
public class SimpleModuleWrapper extends AbstractModule {

	private final static Logger LOG = Logger.getLogger(SimpleModuleWrapper.class.getName());
		
	ClassLoader loader;
	Class<?> myclass;
	
	String name;
	
	
	/**
	 * Both input and output are key values.
	 */
	Map<String, Object> actualInputMap = new HashMap<String, Object>();
	Map<String, Object> outputs = new HashMap<String, Object>();
	
	Map<String, String> references = new HashMap<String, String>();
	
	public SimpleModuleWrapper(Workflow parent) {
		super(parent);
		
	}
	
	/**
	 * What happened here is an annotated module will be loaded based on the source code name.
	 * The input will then also be parsed, and later used when we have to execute method.
	 * 
	 * @param annotatedModule
	 */
	public void wrapModule(Map annotatedModule){
		
		name = (String) annotatedModule.get("name");
		
		//loading the class
		loader = getClass().getClassLoader();
		String source = (String)annotatedModule.get("source");
		try {
			myclass = loader.loadClass(source);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		Map inputMap = (Map)annotatedModule.get("inputs");
		
		for(Object key : inputMap.keySet()){
			// Storing references.
			if(inputMap.get(key) instanceof Map){
					Map ref = (Map) inputMap.get(key);
					references.put(key.toString(),ref.get("reference").toString());
			} else{
				// Store real input values
				actualInputMap.put(key.toString(), inputMap.get(key));
			}
		}
		System.out.println("Module name " + name + " reference size " + references.size());
			
		// IF there are no references means this module is ready
		
		if(references.size() == 0)
			setState(State.READY);
		else
			setState(State.BLOCKED);
	}

	
	
	
	
	//check for references if really ready to execute.
	@Override
	public boolean execute() {
		
		
		Method mainMethod = PlatformUtil.getMainMethod(myclass);
				
		Object myModuleObj = PlatformUtil.createModuleWithDefaultConstructor(myclass);
		
		Object [] args = PlatformUtil.getArgumentsfromInput(mainMethod, actualInputMap);
		
		LOG.info("Simple module wrapper execution");
		
		try {
			
			if(PlatformUtil.checkParameterCompatibilility(mainMethod, args)){
				LOG.info("Executing module : " + name + " " + myclass+" Main method " + mainMethod);
				Object result = mainMethod.invoke(myModuleObj,args);
				
				// Storing the intermediate result to workflow, instead this should be to resource space
				parent.storeIntermediateResult(name+".result", result);
				
				
				Collection<String> outputNames = PlatformUtil.getAllOutputNames(myclass);
				for(String outputName : outputNames){
					Object currentOutput = PlatformUtil.getOutputField(myModuleObj, outputName);
					parent.storeIntermediateResult(name+"."+outputName, currentOutput);
				}
					
			} else 
			// Still need to check first if this is really expandable before doing a real expansion.
			{
				
				LOG.info("Incompatible parameter, unrolling ");
				
				List<Object[]> unrolledArguments =  PlatformUtil.expandArguments(mainMethod, args);
				if(unrolledArguments == null) return false;
				
				
				// Here it might not be enough to have only one result list, we need more for each output field.
				ArrayList<Object> resultList = new ArrayList<Object>();
				
				for(Object[] currentArgs : unrolledArguments){
					Object result = mainMethod.invoke(myModuleObj,currentArgs);
					resultList.add(result);
				}
				
				// Need also to store all the other outputs.
				parent.storeIntermediateResult(name+".result", resultList);
	
			}
			
			setState(State.FINISHED);
			
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			setState(State.FAILED);
			return false;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();

			setState(State.FAILED);
			return false;
		} catch (InvocationTargetException e) {
			e.printStackTrace();

			setState(State.FAILED);
			return false;
		}
		return true;
	}

	@Override
	public Object getReference(String referenceKey) {
		return outputs.get(referenceKey);
	}

	public Map<String, String> getReferences() {
		return references;
	}

	public void updateInput(String key, Object value){
		actualInputMap.put(key, value);
	}

	public void resolveReferences(Set<String> resolved) {
		
		for (String key : resolved)
			references.remove(key);
				
		if(isReady())
			currentState = State.READY;
	}
	
	@Override
	public boolean isReady(){
		return currentState != State.FINISHED && references.size() == 0;
	}
	
	public void getNextInputs(ResourceSpace resourceSpace) {
	
		
	}

	public void storeResults(ResourceSpace resultSpace) {
		
		
	}
	
	@Override
	public String toString() {
		String result = "\nName : "+name + " Source : " + myclass;
		return result;
	}
}
