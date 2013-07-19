package org.data2semantics.platform.util;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Map;

import org.data2semantics.platform.core.Workflow;
import org.data2semantics.platform.wrapper.SimpleModuleWrapper;
import org.yaml.snakeyaml.Yaml;

/**
 * For now this class will parse YAML, and then produces the workflow representation
 * @author wibisono
 *
 */
public class WorkflowParser {

	private String workflowDescription;
	private Yaml yaml = new Yaml();
	
	public WorkflowParser() {
		
		
		
	}
	
	/**
	 * Perhaps not only the parsed yaml file will be required here as parameter, but also what kind of wrapper.
	 * @param yamlFile
	 * @return
	 */
	public Workflow parseYAML(String yamlFile){
		Workflow workflowContainer = new Workflow();
		BufferedInputStream bis=null;
		try {
			bis = new BufferedInputStream(new FileInputStream(yamlFile));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		
		// Need to create bean instead of directly working with map
		Map  workflow = (Map) yaml.load(bis);
		ArrayList<Map> modules = (ArrayList <Map>) workflow.get("workflow");
	
		
		
		/**
		 * First setup the workflow to contain all modules.
		 */
		for(Map m : modules){
			Map module = (Map)m.get("module");
			SimpleModuleWrapper moduleWrapper = new SimpleModuleWrapper(workflowContainer);
			moduleWrapper.wrapModule(module);
			workflowContainer.addModule(moduleWrapper);
		}
		return workflowContainer;
				
	}

	public String getWorkflowDescription() {
		return workflowDescription;
	}

	public void setWorkflowDescription(String workflowDescription) {
		this.workflowDescription = workflowDescription;
	}
	
}
