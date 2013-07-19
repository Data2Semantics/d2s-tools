package org.data2semantics.platform.resourcespace;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * A rough implementation of what will later on be triple space.  
 * Currently, we will have this as a kind place holder for intermediate results, 
 * where results from module execution will be stored.
 * 
 * Interesting point:
 * 	- If implemented properly in the future this will be the point where different execution profile (cluster, cloud) can communicate
 * 	- We can use whatever stored here also as provenance, 
 * 	- Reports of execution can also be generated from this space
 * 
 * 	- If we properly define interface I suppose JavaSpace and others can be used as a backing system for this.
 * 
 *  - We might even avoid execution if one resource space can forward a request of data to another resource space, and resolve a data without computing
 *    if some other resource space already containe an expected result.
 * 
 * @author wibisono
 *
 */
public class ResourceSpace {
	
	
	Map<String, Object> space = new HashMap<String, Object>();

	/**
	 * Initialize the result space
	 */
	public void initialize() {
		
		space = new HashMap<String, Object>();
	}
	
	/**
	 * Generate report ?
	 */
	
	public void generateReport(File outputDirectory){
		
	}
	
	
	
	public void storeResult(String identifier, Object result){
			
		space.put(identifier, result);
	}

	public boolean containsKey(String ref) {
		
		return space.containsKey(ref);
	}

	public Object get(String ref) {
		
		return space.get(ref);
	}

	public Set<String> keySet() {
		
		return space.keySet();
	}
	
	
	
	
}
