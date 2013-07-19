package org.data2semantics.platform.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.data2semantics.platform.resourcespace.ResourceSpace;
import org.data2semantics.platform.wrapper.SimpleModuleWrapper;

public class Workflow {
	
	private  List<AbstractModule> modules = new ArrayList<AbstractModule>();
	private  ResourceSpace resourceSpace  = new ResourceSpace();
	/**
	 * Name of this workflow
	 */
	private String name;
	
	public Workflow(){
		
	}
	public Workflow(String name) {
		super();
		this.name = name;
	}
	
	public List<AbstractModule> getModules() {
		return modules;
	}
	public void setModules(List <AbstractModule> m){
		this.modules = m;
	}
	
	public void addModule(AbstractModule m){
		
		modules.add(m);
	}
	
	/**
	 * Get modules within this workflow which is in the @param expectedState
	 * @param expectedState
	 * @return
	 */
	public List<Module> getModulesWithState(State expectedState) {
		List<Module> result  = new ArrayList<Module>();
		
		for(Module m : modules){
			if(m.getState().equals(expectedState))
				result.add(m);
		}
		return result;
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Main function to run current workflow.
	 */
	public void runWorkflow() {
		
		boolean done = false;
		
		while(!done){
			
			//Separate module based on state
			List<Module> readyModules = getModulesWithState(State.READY);
			List<Module> blockedModules = getModulesWithState(State.BLOCKED);
			
			System.out.println("List sizes " +readyModules.size() + " " +blockedModules.size());

			// No more modules ready for execution
			// This can either be all modules finished or some of them blocking.
			if(readyModules.size() == 0){
				done = true;
			
			}
			
			// Execute  all ready modules 
			for(Module m : readyModules){
				boolean success = m.execute();
				if(success){
					m.setState(State.FINISHED);
				}
			}
			
			updateModuleReferences();
			
		}
		

		
	}
	
	public void setResourceSpace(ResourceSpace space){
		resourceSpace = space;
	}
	
	public void updateModuleReferences(){
		List<Module> blockedModules = getModulesWithState(State.BLOCKED);
		
		//Attempt to resolve blocked module
		for(Module b : blockedModules){
			//for each of the blocked reference, check if it is resolved by the new results from modules which just finished.
			SimpleModuleWrapper abr = (SimpleModuleWrapper) b;
			Map<String, String> references = abr.getReferences();
			Set<String> resolved = new HashSet<String>();
			for(String inputKey : references.keySet()){
				String referredOutput = references.get(inputKey);
				if(resourceSpace.containsKey(referredOutput)){
					abr.updateInput(inputKey, resourceSpace.get(referredOutput));
					resourceSpace.addReference(abr.getName(), referredOutput);
					resolved.add(inputKey);
				}
			}
			
			abr.resolveReferences(resolved);
		}
	}
	
	public void storeIntermediateResult(String key, Object value){
		resourceSpace.storeResult(key, value);
	}
	

	
	public void dumpIntermediateResults(){
		System.out.println("Intermediate results \n");
		TreeSet<String> sortedKey = new TreeSet<String>();
		sortedKey.addAll(resourceSpace.keySet());
		for(String key : sortedKey){
			System.out.println(key + "  " + resourceSpace.get(key));
		}
	}

}
