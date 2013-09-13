package org.data2semantics.platform.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.data2semantics.platform.Global;
import org.data2semantics.platform.core.Workflow;
import org.data2semantics.platform.core.data.DataType;
import org.data2semantics.platform.domain.Domain;
import org.data2semantics.platform.exception.InconsistentWorkflowException;
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
		builder.file(yamlFile);
		
		
		/**
		 * First setup the workflow to contain all modules.
		 */
		for (Map m : modules)
		{
			Map module = (Map) m.get("module");

			String moduleName = (String) module.get("name");
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
				
				domain = Global.domain(domainPrefix);
			}

			// get name
			builder.module(moduleName, domain);

			// get the source
			builder.source(moduleName, sourceTail);

			// get the inputs
			Map inputMap = (Map) module.get("inputs");
			
			// Get the coupled inputs
			List<?> couples = (List <?>) module.get("couple");
		
			
			List<String> errors = new ArrayList<String>();
			
			if(!domain.validate(source, errors)){
				throw new InconsistentWorkflowException(errors);
			}
			
			parseInputAndCouples(builder, moduleName, domain, sourceTail, inputMap, couples);
			
			// ask the domain object for the outputs
			Map<String, DataType> outputTypeMap = getOutputTypes(source, domain);
			
			for(String outputName : outputTypeMap.keySet())
			{
				boolean print = domain.printOutput(sourceTail, outputName);

				String description = domain.outputDescription(sourceTail, outputName);
				builder.output(moduleName, outputName, description, outputTypeMap.get(outputName), print);
			}
		}
		
		return builder.workflow();
	}

	private static void parseInputAndCouples(Workflow.WorkflowBuilder builder,
			String moduleName, Domain domain, String sourceTail, Map inputMap,
			List<?> couples) 
	{
		// Process all the inputs.
		for (Object inputKey : inputMap.keySet())
		{
			String inputName = inputKey.toString();

			Object inputValue = inputMap.get(inputKey);
			
			// If the input is a map, then it is actually a reference input
			if (inputValue instanceof Map)
			{
				Map ref = (Map) inputValue;
				String referenceString = (String) ref.get("reference");

				// Reference is in : module.output format, we split using .
				String referencedModule = referenceString.split("\\.")[0];
				String referencedOutput = referenceString.split("\\.")[1];

				DataType inputType = domain.inputType(sourceTail, inputName);
				
				String description = domain.inputDescription(sourceTail, inputName);
				boolean print = domain.printInput(sourceTail, inputName);
				
				
				builder.refInput(moduleName, inputName, description, referencedModule,
						referencedOutput, inputType, print);
				
			} else
			{
				
				DataType inputDataType = domain.inputType(sourceTail, inputName);
				
				String description = domain.inputDescription(sourceTail, inputName);
				
				
				// First handle multi input case, now we include  the case that items in this list this might also be reference.
				if(inputValue instanceof List<?>){
					
					// Each item in the list matches the expected input datatype in this domain, we are doing a sweep a.k.a. multi value solely consisting of raw inputs.
					if(listItemMatch((List<?>) inputValue, inputDataType, domain)){
						builder.multiInput(moduleName,  description, inputName, (List<?>)inputValue, inputDataType);
					} else 
					// Either items in the list are references or they match the expected input data type
					if(listItemMatchOrReference((List<?>)inputValue, inputDataType, domain)){
						builder.multiInputRef(moduleName, description, inputName, (List<?>) inputValue, inputDataType);
					} else
					// The input are expecting a list.
					if(domain.valueMatches(inputValue, inputDataType)){
						boolean print = domain.printInput(sourceTail, inputName);
						
						builder.rawInput(moduleName, description, inputName, inputValue, domain.inputType(sourceTail, inputName), print);
							
					} else
						throw new InconsistentWorkflowException("Module "+moduleName+", input " + inputName + ": value ("+inputValue+") does not match the required data type ("+inputDataType+").");
					
				}
				else 
				if(domain.valueMatches(inputValue, inputDataType)){
					boolean print = domain.printInput(sourceTail, inputName);

					builder.rawInput(moduleName, description, inputName, inputValue, domain.inputType(sourceTail, inputName), print);
					
				}
				
				else
					throw new InconsistentWorkflowException("Module "+moduleName+", input " + inputName + ": value ("+inputValue+") does not match the required data type ("+inputDataType+").");
			}
		}
		
		// Process the couple lists
		if(couples != null)
		for(Object couple : couples){
			if(!(couple instanceof List)) throw new InconsistentWorkflowException("Couple info is not a list, it should be a list of input names in this module");
			List <String> coupleLS = (List<String>) couple;
			for(String inputName : coupleLS){
				if(!inputMap.containsKey(inputName)){
					throw new InconsistentWorkflowException("Couple list contains input "+inputName+" which is not defined for this module ");
				}
			}
			
			builder.coupledInputs(moduleName, coupleLS);
		}
	}
	
	// Check if list of values provided in workflow description matches the input datatype expected in this domain.
	private static boolean listItemMatch(List<?> list, DataType type, Domain domain)
	{
		for(Object item : list) {
		
			if(! domain.valueMatches(item, type)){
				return false;
			}
		}
		
		return true;
	}

	private static boolean listItemMatchOrReference(List<?> list, DataType type, Domain domain)
	{
		for(Object item : list) {
		
			if(!isAReference(item) && ! domain.valueMatches(item, type)){
				return false;
			}
		}
		
		return true;
	}
	
	// A reference is a map which has "reference" as key
	private static boolean isAReference(Object item) {
		if((item instanceof Map)) {
			Map mitem = (Map) item;
			 return mitem.containsKey("reference");
		}
		
		return false;
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
