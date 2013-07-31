package org.data2semantics.platform.resourcespace;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * A rough implementation of what will later on be triple space.  
 * Currently, we will have this as a kind place holder for intermediate results, 
 * where results from module execution will be stored.
 * 
 * Interesting point:
 * 	- If implemented properly in the future this will be the point where 
 *    different execution profile (cluster, cloud) can communicate
 * 	- We can use whatever stored here also as provenance, 
 * 	- Reports of execution can also be generated from this space
 * 
 * 	- If we properly define interface I suppose JavaSpace and others can be used
 *    as a backing system for this.
 * 
 *  - We might even avoid execution if one resource space can forward a request 
 *    of data to another resource space, and resolve a data without computing
 *    if some other resource space already containe an expected result.
 * 
 * @author wibisono
 *
 */
public class ResourceSpace {
	
	
	// Hashmap storing intermediate results. The key would be Modulename.Output
	Map<String, Object> space = new HashMap<String, Object>();

	// Keeping track of which module is referring to output of which other modules.
	Map<String, List<String>> refersTo = new HashMap<String, List<String>>();
	
	/**
	 * Initialize the result space
	 */
	public void initialize() {
		
		space = new HashMap<String, Object>();
	}

	
	/**
	 * Store intermediate results
	 * @param identifier
	 * @param result
	 */
	
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
	
	
	
	/**
	 * Generate report ?
	 */
	
	public void generateReport(File outputDirectory){
		// Create a directory
		outputDirectory.mkdir();
		
		// Create the file output.txt
		File report = new File(outputDirectory, "Report.html");
		
		try {
			FileWriter writer = new FileWriter(report);
			writer.append("\n<h1>Workflow execution Report</h1>");
			writer.append("\b<h2>Call  dependency </h2>");
			
			writer.append("<img src='https://chart.googleapis.com/chart?cht=gv:dot&chl=");
			writer.append(produceDotString());
			writer.append("'>");
			
			writer.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	public void addReference(String module, String referredModule){
		if(refersTo.containsKey(module)){
			List<String> referredList = refersTo.get(module);
			referredList.add(referredModule);
		} else {
			List<String> referredList = new ArrayList<String>();
			referredList.add(referredModule);
			refersTo.put(module, referredList);
		}
	}
	
	private String produceDotString(){
		StringBuffer result = new StringBuffer();
		
		result.append ("digraph{");
		
		for(String module : refersTo.keySet()){
				for(String referringOutput : refersTo.get(module)){
					String source = referringOutput.split("\\.")[0];
					String label =  referringOutput.split("\\.")[1];
					result.append(source + "->" + module + "[label=\"" + label + "\"]");
				}
		}
		
		result.append("}");
		
		return result.toString();
		
	}
}
