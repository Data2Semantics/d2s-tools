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

import org.data2semantics.platform.core.ModuleInstance;
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

			ProcessBuilder pb = new ProcessBuilder( command, "&&set");

			Map<String, String> env = pb.environment();
			
			for(InstanceInput input: inputs){
				env.put(input.name(), input.value().toString());
				System.out.println("Setting env " + input.name()+" = "+input.value());
			}
			

			Process process = pb.start();       
			pb.redirectErrorStream(true);

			process.waitFor();
			
			InputStream stdout = process.getInputStream ();
			BufferedReader stdOutReader = new BufferedReader (new InputStreamReader(stdout));

			String outLine;

			Map<String, String> envResults = new LinkedHashMap<String, String>();
			while ((outLine = stdOutReader.readLine ()) != null) {
			    if(outLine.trim().length() > 0){
			    	if(!outLine.contains("="))continue;
			    	String [] keyValue = outLine.split("=");
			    	envResults.put(keyValue[0], keyValue[1]);
			    }    
			}
			
			for(InstanceOutput output : instance.outputs()){
				results.put(output.name(), envResults.get(output.name()));
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new IllegalStateException("Failed to execute command line module "+e.getMessage());
			
		}
		
		// Set back the output to list of results.
		// The assumption here is that outputs are stored in the environments variables also.
		
		
		return false;
	}
	
	@Override
	public boolean typeMatches(Output output, Input input) {
		// TODO
		// Implement conversions, we so far only have JavaType datatype
		return false;
	}

	@Override
	public List<DataType> conversions(DataType type) {
		// TODO
		// Implement conversions, we so far only have JavaType datatype
		return null;
	}


	@Override
	public DataType inputType(String source, String inputName) {
	
		return new JavaType(Integer.class);
	}

	@Override
	public DataType outputType(String source, String outputName) {
		// TODO Auto-generated method stub
		return new JavaType(Integer.class);
	}

	@Override
	public boolean valueMatches(Object value, DataType type) {
		
		
		JavaType jType = (JavaType)type;
		
		return PlatformUtil.isAssignableFrom(jType.clazz(), value.getClass());
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
			if(input.get("name").equals(name))
				return input.get("description");
		}
		return null;
	}

	@Override
	public String outputDescription(String source, String name) {
		for(Map<String,String> output : CommandLineConfigParser.getOutputList(source)){
			if(output.get("name").equals(name))
				return output.get("description");
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
	

		
		static List<String> outputs(String source){
			List<String> result = new ArrayList<String>();
			List<Map<String,String>> outputs = getOutputList(source);
			for(Map<?,?> output: outputs){
				result.add((String)output.get("name"));
			}
			return result;
		}
		
		public static List<String> inputs(String source) {
			List<String> result = new ArrayList<String>();
			List<Map<String,String>> inputs = getInputList(source);
			for(Map<?,?> input: inputs){
				result.add((String)input.get("name"));
			}
			return result;
		}

		@SuppressWarnings("unchecked")
		static List<Map<String, String>> getOutputList(String source){
			Map<?,?> configMap = getConfigMap(source);
			return  (List<Map<String,String>>)configMap.get("Outputs");
		}
		
		@SuppressWarnings("unchecked")
		static List<Map<String, String>> getInputList(String source){
			Map<?,?> configMap = getConfigMap(source);
			return  (List<Map<String,String>>)configMap.get("Inputs");
		}
		
		static String getCommand(String source){
			Map<?,?> configMap = getConfigMap(source);
			return (String) configMap.get("Command");
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

}
