package org.data2semantics.platform.domain;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.data2semantics.platform.core.ModuleInstance;
import org.data2semantics.platform.core.data.CommandLineType;
import org.data2semantics.platform.core.data.DataType;
import org.data2semantics.platform.core.data.Input;
import org.data2semantics.platform.core.data.InstanceInput;
import org.data2semantics.platform.core.data.InstanceOutput;
import org.data2semantics.platform.core.data.JavaType;
import org.data2semantics.platform.core.data.Output;
import org.data2semantics.platform.util.PlatformUtil;
import org.yaml.snakeyaml.Yaml;

public class CommandLineDomain implements Domain {

	private static CommandLineDomain domain = new CommandLineDomain();

	@Override
	public boolean execute(ModuleInstance instance, List<String> errors,
			Map<String, Object> results) {
		// TODO 
		// implement execute module, using pipe/runtime
		
		String cmdLineSource = instance.module().source();
		
		String command = CommandLineConfigParser.getCommand(cmdLineSource);
		
		// input and output perhaps either passed through file or environment variables
		// Setup inputs from module instance
		
		List<InstanceInput> inputs = instance.inputs();
		String []inputEnvironments = new String[inputs.size()];
		
		for(int i=0;i<inputEnvironments.length;i++){
			inputEnvironments[i] = inputs.get(i).name()+"="+inputs.get(i).value();
		}
		
		// Call the main method of the command line
		try {
			
			// Adding an additional command set to show environment variables, in unix this would be env.
			ProcessBuilder pb = new ProcessBuilder( command );

			if(command.endsWith(".py")){
				String osname = System.getProperty("os.name");
				if(osname.startsWith("Windows"))
					pb = new ProcessBuilder("C:\\python27\\python.exe", command);
				else
					pb = new ProcessBuilder("/usr/bin/python", command);
			}
			
			Map<String, String> env = pb.environment();
			
			for(InstanceInput input: inputs){
				env.put(input.name(), input.value().toString());
//				System.out.println("Setting env "+ input.name()+ " " +input.value());
			}
			
			Process process = pb.start();       
			pb.redirectErrorStream(true);

			process.waitFor();
			
//			System.out.println("Exit value : "+		process.exitValue());
			InputStream inputStream = process.getInputStream ();
			String result = IOUtils.toString(inputStream, "UTF-8");
			
			results.put("result", result);
		
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new IllegalStateException("Failed to execute command line module "+e.getMessage());
			
		}
		
		// Set back the output to list of results.
		// The assumption here is that outputs are stored in the environments variables also.
		
		
		return true;
	}
	
	/**
	 * Accept a by default string value of an output from this command line. In case there is an appropriate type cast to it.
	 * @param output
	 * @param stringValue
	 * @return
	 */
	private Object castOutputType(InstanceOutput output, String stringValue) {
		CommandLineType type = (CommandLineType) output.dataType();
		
		return type.valueOf(stringValue);
	}

	@Override
	public boolean typeMatches(Output output, Input input) {
		DataType outputType = output.dataType();
		DataType inputType =  input.dataType();
		
		return PlatformUtil.isAssignableFrom( inputType.clazz(), outputType.clazz());

	}

	@Override
	public List<DataType> conversions(DataType type) {
		// TODO
		// Implement conversions, we so far only have JavaType datatype
		return null;
	}


	@Override
	public DataType inputType(String source, String inputName) {
		String inputType = CommandLineConfigParser.getInputType(source, inputName);
		
		if(inputType.contains(CommandLineType.Types.INTEGER.toString()))
			return new CommandLineType(CommandLineType.Types.INTEGER);
		
		return new CommandLineType(CommandLineType.Types.STRING);
	}

	@Override
	public DataType outputType(String source, String outputName) {
		String outputType = CommandLineConfigParser.getOutputType(source, outputName);
		
		if(outputType.contains(CommandLineType.Types.INTEGER.toString()))
			return new CommandLineType(CommandLineType.Types.INTEGER);
		
		return new CommandLineType(CommandLineType.Types.STRING);
	}

	@Override
	public boolean valueMatches(Object value, DataType type) {
		
		return PlatformUtil.isAssignableFrom(type.clazz(), value.getClass());
	}

	@Override
	public List<String> outputs(String source) {

		return CommandLineConfigParser.outputs(source);
	}
	
	public List<String> inputs(String source) {

		return CommandLineConfigParser.inputs(source);
	}
	
	public String getCommand(String source){
		return CommandLineConfigParser.getCommand(source);
	}


	@Override
	public String inputDescription(String source, String name) {
		for(Map<String,String> input : CommandLineConfigParser.getInputList(source)){
			if(input.get(CommandLineConfigParser.NAME).equals(name))
				return input.get(CommandLineConfigParser.DESCRIPTION);
		}
		return null;
	}

	@Override
	public String outputDescription(String source, String name) {
		for(Map<String,String> output : CommandLineConfigParser.getOutputList(source)){
			if(output.get(CommandLineConfigParser.NAME).equals(name))
				return output.get(CommandLineConfigParser.DESCRIPTION);
		}
		return null;
	}

	@Override
	public boolean check(ModuleInstance instance, List<String> errors) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean validate(String source, List<String> errors) {
		
		
		return true;
	}

	
	public static CommandLineDomain domain(){
		return domain;
	}
	
	
	private static class CommandLineConfigParser{
	

		static final String NAME = "name";

		static final String OUTPUTS = "outputs";
		static final String INPUTS = "inputs";
		static final String DESCRIPTION = "description";
		static final String TYPE = "type";
		private static final String COMMAND = "command";

		static List<String> outputs(String source){
			List<String> result = new ArrayList<String>();
			List<Map<String,String>> outputs = getOutputList(source);
			for(Map<?,?> output: outputs){
				result.add((String)output.get(NAME));
			}
			return result;
		}
		
		public static List<String> inputs(String source) {
			List<String> result = new ArrayList<String>();
			List<Map<String,String>> inputs = getInputList(source);
			for(Map<?,?> input: inputs){
				result.add((String)input.get(NAME));
			}
			return result;
		}

		@SuppressWarnings("unchecked")
		static List<Map<String, String>> getOutputList(String source){
			Map<?,?> configMap = getConfigMap(source);
			return  (List<Map<String,String>>)configMap.get(OUTPUTS);
		}
		
		@SuppressWarnings("unchecked")
		static List<Map<String, String>> getInputList(String source){
			Map<?,?> configMap = getConfigMap(source);
			return  (List<Map<String,String>>)configMap.get(INPUTS);
		}
		
		static String getCommand(String source){
			Map<?,?> configMap = getConfigMap(source);
			return (String) configMap.get(COMMAND);
		}
		
		// Get the input type from configuration and returned it, to be used for value matches etc on top, which would then have corresponding type value with the java type.
		@SuppressWarnings("unchecked")
		static String getInputType(String source, String inputName){
			Map<?,?> configMap = getConfigMap(source);
			List<Map<String,String>> inputList = (List<Map<String,String>>)configMap.get(INPUTS);
			String result = null;
			for(Map<String,String> input : inputList)
				if(input.get(NAME).equals(inputName)){
					result = input.get(TYPE);
					break;
				}
			
			if(result == null)
				
				throw new IllegalStateException("Input name "+inputName+" is undefined in source " + source);
			
			return result;
					
		}
		
		@SuppressWarnings("unchecked")
		static String getOutputType(String source, String outputName){
			Map<?,?> configMap = getConfigMap(source);
			List<Map<String,String>> inputList = (List<Map<String,String>>)configMap.get(OUTPUTS);
			String result = null;
			for(Map<String,String> input : inputList)
				if(input.get(NAME).equals(outputName)){
					result = input.get(TYPE);
					break;
				}
			
			if(result == null)
				
				throw new IllegalStateException("Output name "+outputName+" is undefined in source " + source);
			
			return result;
					
		}
		
		private static Map<?,?> getConfigMap(String source) {
			Map<?,?> result = null;
			if(source.contains(":")) source = source.split(":")[1];
			try{
				BufferedInputStream bis = new BufferedInputStream(new FileInputStream(source));
				result = (Map<?, ?>) new Yaml().load(bis);
			} catch(FileNotFoundException e){
				throw new IllegalArgumentException("Command line source configuration file " +source +" can not be found ");
			}
			return result;
		} 
		
	}


	@Override
	public boolean printInput(String source, String input)
	{
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean printOutput(String source, String input)
	{
		// TODO Auto-generated method stub
		return true;
	}

}
