package org.data2semantics.platform.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.data2semantics.platform.Global;
import org.data2semantics.platform.core.Workflow;
import org.data2semantics.platform.core.data.DataType;
import org.data2semantics.platform.core.data.Input;
import org.data2semantics.platform.domain.Domain;
import org.data2semantics.platform.domain.JavaDomain;
import org.data2semantics.platform.domain.PythonDomain;
import org.data2semantics.platform.exception.InconsistentWorkflowException;
import org.data2semantics.platform.wrapper.SimpleModuleWrapper;
import org.yaml.snakeyaml.Yaml;

/**
 * For now this class will parse YAML, and then produces the workflow representation
 * @author wibisono
 *
 */
public class WorkflowParser {

	private String workflowDescription;
	private static Yaml yaml = new Yaml();
	
	/**
	 * Perhaps not only the parsed yaml file will be required here as parameter, but also what kind of wrapper.
	 * @param yamlFile
	 * @return
	 */
	public static Workflow parseYAML(String yamlFile)
		throws IOException
	{
		return parseYAML(new File(yamlFile));
	}
	
	public static Workflow parseYAML(File yamlFile)
		throws IOException
	{
		
		Workflow.WorkflowBuilder builder = Workflow.builder();
		
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(yamlFile));
		
		// Need to create bean instead of directly working with map
		Map<?, ?> loadMap = (Map<?, ?>) yaml.load(bis);
		Map<?, ?> workflowMap = (Map<?, ?>) loadMap.get("workflow");
		ArrayList<Map<?, ?>> modules = (ArrayList <Map<?, ?>>) workflowMap.get("modules");
	
		// workflow name
		String workflowName= (String) workflowMap.get("name");
		builder.name(workflowName);
		/**
		 * First setup the workflow to contain all modules.
		 */
		for (Map m : modules)
		{
			Map module = (Map) m.get("module");

			String name = (String) module.get("name");
			String source = (String) module.get("source");

			// Default domain
			Domain domain;
			String domainPrefix, sourceTail;
			
			if(! source.contains(":"))
			{
				domain = Global.defaultDomain();
				domainPrefix = "java";
				sourceTail = source;
			} else
			{
				domainPrefix = source.split(":")[0];
				sourceTail = source.split(":", 2)[1];
				
				if(! Global.domainExists(domainPrefix))
					throw new RuntimeException("Domain "+domainPrefix+" is not known");
				
				domain = Global.domain(name);
			}
			
			
		

			// get name
			builder.module(name, domain);

			// get the source
			builder.source(name, sourceTail);

			// get the inputs
			Map inputMap = (Map) module.get("inputs");
			
			for (Object inputKey : inputMap.keySet())
			{
				String inputName = inputKey.toString();

				// If the input is a map, then it is actually a reference input
				if (inputMap.get(inputKey) instanceof Map)
				{
					Map ref = (Map) inputMap.get(inputKey);
					String referenceString = (String) ref.get("reference");

					// Reference is in : module.output format, we split using .
					String referencedModule = referenceString.split("\\.")[0];
					String referencedOutput = referenceString.split("\\.")[1];

					DataType inputType = domain.inputType(sourceTail, inputName);
					
					String description = domain.inputDescription(sourceTail, inputName);
					
					builder.refInput(name, inputName, description, referencedModule,
							referencedOutput, inputType);
					
				} else // Raw value
				{
					Object value = inputMap.get(inputKey);
					
					DataType dataType = domain.inputType(sourceTail, inputName);
					
					String description = domain.inputDescription(sourceTail, inputName);
					
					if(domain.valueMatches(value, dataType))
						builder.rawInput(name, description, inputName, value, domain.inputType(sourceTail, inputName));
					else if((value instanceof List<?> ) && listItemsMatch((List<Object>) value, dataType, domain))
						builder.multiInput(name,  description, inputName, (List<Object>)value, dataType);
					else
						throw new InconsistentWorkflowException("Module "+name+", input " + inputName + ": value ("+value+") does not match the required data type ("+dataType+").");
				}
			}

			// ask the domain object for the outputs
			Map<String, DataType> outputTypeMap = getOutputTypes(source, domain);
			
			for(String outputName : outputTypeMap.keySet())
			{
				String description = domain.outputDescription(sourceTail, outputName);
				builder.output(name, outputName, description, outputTypeMap.get(outputName));
			}
		}
		
		return builder.workflow();
	}

	private static boolean listItemsMatch(List<Object> list, DataType type, Domain domain)
	{
		for(Object item : list)
			if(! domain.valueMatches(item, type))
				return false;
		
		return true;
	}
	
	private static Map<String, DataType> getOutputTypes(String source, Domain domain)
	{
		Map<String, DataType> result = new LinkedHashMap<String, DataType>();
		
		for(String output : domain.outputs(source))
			result.put(output, domain.outputType(source, output));
		
		return result;
	}

	public String getWorkflowDescription() 
	{
		return workflowDescription;
	}

	public void setWorkflowDescription(String workflowDescription)
	{
		this.workflowDescription = workflowDescription;
	}
	
}
