package org.data2semantics.platform.domain;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.data2semantics.platform.annotation.DomainDefinition;
import org.data2semantics.platform.annotation.Factory;
import org.data2semantics.platform.annotation.In;
import org.data2semantics.platform.annotation.Main;
import org.data2semantics.platform.annotation.Out;
import org.data2semantics.platform.core.Module;
import org.data2semantics.platform.core.ModuleInstance;
import org.data2semantics.platform.core.data.DataType;
import org.data2semantics.platform.core.data.Input;
import org.data2semantics.platform.core.data.InstanceInput;
import org.data2semantics.platform.core.data.InstanceOutput;
import org.data2semantics.platform.core.data.JavaType;
import org.data2semantics.platform.core.data.Output;
import org.data2semantics.platform.exception.InconsistentWorkflowException;
import org.data2semantics.platform.exception.WorkflowCodeMatchException;
import org.data2semantics.platform.util.PlatformUtil;

@DomainDefinition(prefix="java")
public class JavaDomain implements Domain
{
	private static JavaDomain domain = new JavaDomain();
	
	@Override
	public boolean typeMatches(Output output, Input input)
	{
		DataType outputType = output.dataType();
		DataType inputType =  input.dataType();
		
		return PlatformUtil.isAssignableFrom( inputType.clazz(), outputType.clazz());
	}

	@Override
	public boolean check(ModuleInstance instance, List<String> errors)
	{
		return true; // TODO
	}

	@Override
	public List<DataType> conversions(DataType type)
	{
		return Collections.emptyList(); // TODO
	}

	@Override
	public boolean execute(ModuleInstance instance, List<String> errors, Map<String, Object> outputs)
	{
		// Three cases based on inputs :
		Class<?> curClass =  loadClass(instance.module().source());
	
		// Tagged input fields -> Build using default constructor
		if(fieldsMatchesInputs(instance.inputs(), curClass))			
			return fieldInputExecute(instance, errors, outputs);

		// Factory methods with tagged parameters
		for(Method curMethod : curClass.getMethods())
			if(factoryMethodMatchesInputs(instance.inputs(), curMethod))
				return factoryMethodExecute(instance, curMethod, errors, outputs);
		
		// Constructor with tagged parameters.
		for(Constructor<?> curConstructor: curClass.getConstructors())
			if(constructorMatchesInputs(instance.inputs(), curConstructor))
				return constructorExecute(instance, curConstructor, errors, outputs);
		
		errors.add("No module execution available for inputs of this module" );
		return false; 
	}
	

	private boolean constructorExecute(ModuleInstance instance, Constructor<?> constructor,
			List<String> errors, Map<String, Object> outputs) {
		
		// Create an array of objects to pass to the constructor
		// (ie. put the input values into the right order)		
		Annotation[][] annotations = constructor.getParameterAnnotations();
		Object [] args = new Object[annotations.length]; // this will be passed to the constructor
		
		// Find the values (in order) for each parameter of the constructor
		for(int i=0;i<annotations.length;i++)
		{
			In ia = getAnnotation(annotations[i], In.class);
			InstanceInput ii = instance.input(ia.name());
			args[i] = ii.value();
		}
		
		// Call constructor to instantiate the module object
		Object moduleObject = null;
		try {
			moduleObject = constructor.newInstance(args);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Constructor not accessible ("+constructor+").", e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException("Constructor threw exception ("+constructor+").", e);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		}
		
		return executeInstantiated(moduleObject, instance, errors, outputs);
	}

	private boolean factoryMethodExecute(ModuleInstance instance, Method factoryMethod,
			List<String> errors, Map<String, Object> outputs) {
		
		// Create an array of objects to pass to the factory nethod
		// (ie. put the input values into the right order)
		Annotation[][] annotations = factoryMethod.getParameterAnnotations();
		Object [] args = new Object[annotations.length];
		
		for(int i=0;i<annotations.length;i++){
			for(int j=0;j<annotations[i].length;j++){
				if(annotations[i][j] instanceof In)
				{
					In ia = (In)annotations[i][j];
					InstanceInput ii = instance.input(ia.name());
					args[i] = ii.value();
				}
			}
		}
		
		// Call the factory method to instantiate the module object
		Object moduleObject = null;
		try {
			moduleObject = factoryMethod.invoke( null, args);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Factory method not accessible (method: "+factoryMethod+").", e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
		
		return executeInstantiated(moduleObject, instance, errors, outputs);
	}

	/**
	 * Executes a module with Inputs defined as class fields.
	 * 
	 * @param instance
	 * @param errors
	 * @param outputs
	 * @return
	 */
	private boolean fieldInputExecute(ModuleInstance instance, List<String> errors, Map<String, Object> outputs) 
	{
		
		// Instantiate class with default constructor
		Class<?> curClass =  loadClass(instance.module().source());
		Object moduleObject = null;
		
		try {
			// Instantiate the module object
			Constructor<?> defaultConstructor = curClass.getConstructor();
			moduleObject = defaultConstructor.newInstance();
			
			// Enter the correct values into the fields
			List<InstanceInput> inputs = instance.inputs();
			Map<String, Field > nameToFieldMap = new LinkedHashMap<String, Field>();
			
			// -- Create a map from names to fields
			for(Field f : curClass.getFields())
			{
				for(Annotation a : f.getAnnotations())
				{		
					if(a instanceof In)
					{
						if(nameToFieldMap.containsKey(((In) a).name()))
							throw new WorkflowCodeMatchException("Multiple input field with the same name for input with name : " +((In)a).name());
					
						nameToFieldMap.put(((In)a).name(), f);
					}
				}
			}
			
			// -- enter the values
			for(InstanceInput ii : inputs) 
			{
				Field curField = nameToFieldMap.get(ii.name());
				curField.set(moduleObject, ii.value());
			}
				
		} catch (NoSuchMethodException e) {
			throw new RuntimeException("Module instantiated by fields should have a default constructor.", e);
		} catch (SecurityException e) {
			throw new RuntimeException("Default constructor is not accessible.", e);
		} catch (Exception e ){
			throw new RuntimeException("Failed to instantiate object", e);
		}
		
		// Execute the instantiated module object (and collect the results)
		boolean success = executeInstantiated(moduleObject, instance, errors, outputs);
		
		return success;
	}

	/**
	 * Executes an instantiated object and collects all the results to be set as instanceoutputs. 
	 * @param moduleObject
	 * @param errors
	 * @param outputs A map collection the values returned for the various outputs
	 * @return
	 */
	private boolean executeInstantiated(
			Object moduleObject, ModuleInstance instance, List<String> errors, Map<String, Object> outputs) 
	{
		Class<?> curClass =  loadClass(instance.module().source());
		
		// Retrieve the main method and its annotation
		Method mainMethod = PlatformUtil.getMainMethod(curClass);
		Main mainAnnotation = PlatformUtil.getMainAnnotation(curClass);
		
		try {
			// This is where the module is actually executed
			Object result = mainMethod.invoke(moduleObject);
			
			// Store the result of the main method as the first output
			if(result != null) // if the main method is not null
				outputs.put(mainAnnotation.name(), result);
			
		} catch (Exception e)
		{
			throw new RuntimeException("Something went wrong during module execution.", e);
		}
		
		// Loop through all fields to check for outputs
		for(Field field : curClass.getFields())
		{
			Out anno = getAnnotation(field.getAnnotations(), Out.class);
			if(anno != null)
			{
				String name = anno.name();
				Object value = null;
				try {
					value = field.get(moduleObject);
				} catch (IllegalArgumentException e) {
					throw new RuntimeException(e);
				} catch (IllegalAccessException e) {
					throw new RuntimeException("Output field '"+field+"' in module '"+instance.module().source()+"' not accessible. ");
				}
				
				outputs.put(name, value);
			}
		}
		
		// Loop through all methods to check for outputs
		for(Method method : curClass.getMethods())
		{
			Out anno = getAnnotation(method.getAnnotations(), Out.class);
			
			if(anno != null)
			{
				String name = anno.name();
				Object value = null;
				try {
					value = method.invoke(moduleObject);
				} catch (IllegalArgumentException e) {
					throw new RuntimeException(e);
				} catch (IllegalAccessException e) {
					throw new RuntimeException("Output method '"+method+"' in module '"+instance.module().source()+"' not accessible. ", e);
				} catch (InvocationTargetException e) {
					throw new RuntimeException("Output method '"+method+"' in module '"+instance.module().source()+"' threw Exception of type "+e.getClass()+". ", e);
				}
				
				outputs.put(name, value);
			}
		}
		
		return true;
	}

	@SuppressWarnings("unchecked")
	private <T extends Annotation> T getAnnotation(Annotation[] annotations, Class<T> cls) 
	{
		for(Annotation anno : annotations)
			if(cls.isAssignableFrom(anno.getClass()))
				return (T)anno;
		
		return null;
	}

	private boolean constructorMatchesInputs(List<InstanceInput> inputs,
			Constructor<?> curConstructor) {
		
		Annotation[][] paramAnnotations = curConstructor.getParameterAnnotations();
		Class<?> [] paramTypes = curConstructor.getParameterTypes();
		
		return methodMatchesInputs(inputs, paramAnnotations, paramTypes);
	}

	private boolean factoryMethodMatchesInputs(List<InstanceInput> inputs,
			Method m) {
		
		if(!hasFactoryAnnotation(m)) return false;
		Annotation [][] paramAnnotations = m.getParameterAnnotations();
		Class<?> [] paramTypes = m.getParameterTypes();

		return methodMatchesInputs(inputs, paramAnnotations, paramTypes);
	}

	private boolean methodMatchesInputs(List<InstanceInput> inputs, Annotation[][] paramAnnotations, Class<?> [] paramTypes) {
	
		Map<String, Class<?>> inputNameToParamType = new LinkedHashMap<String, Class<?>>();
		
		for(int i=0;i<paramAnnotations.length;i++){
			boolean found = false;
			for(int j=0;j<paramAnnotations[i].length;j++){
				if(paramAnnotations[i][j] instanceof In){
					found = true;
					inputNameToParamType.put(((In)paramAnnotations[i][j]).name(), paramTypes[i]);
				}
			}
			if(!found) {
				throw new WorkflowCodeMatchException("Method tagged with factory without all of it's parameter tagged as input");
			}
		}
		
		if(inputNameToParamType.size() != inputs.size())
			return false;
		
		for(InstanceInput ii : inputs){
			if(!inputNameToParamType.containsKey(ii.name()))
				return false;
		
			Class<?> paramClazz = inputNameToParamType.get(ii.name());
			
			if(!(ii.dataType() instanceof JavaType))
				return false;
			
			JavaType jType = (JavaType)ii.dataType();
		
			if(!PlatformUtil.isAssignableFrom(paramClazz, jType.clazz()))
				return false;
		}
		
		return true;
	}

	private boolean fieldsMatchesInputs(List<InstanceInput> list,
			Class<?> curClass) {
		
		Map<String, Field > nameToFieldMap = new LinkedHashMap<String, Field>();
		
		for(Field f : curClass.getFields()){
			for(Annotation a : f.getAnnotations()){
				
				if(a instanceof In){
					if(nameToFieldMap.containsKey(((In) a).name()))
						throw new WorkflowCodeMatchException("Multiple input field with the same name for input with name : " +((In)a).name());
				
					nameToFieldMap.put(((In)a).name(), f);
				}
			}
		}
		
		if(nameToFieldMap.size() != list.size())
			return false;
		
		for(InstanceInput ii : list){
			if(!nameToFieldMap.containsKey(ii.name()))
				return false;
		
			Field f = nameToFieldMap.get(ii.name());
			
			if(!(ii.dataType() instanceof JavaType))
				return false;
			
			JavaType jType = (JavaType)ii.dataType();
		
			if(!PlatformUtil.isAssignableFrom(f.getType(), jType.clazz()))
				return false;
			
			
		}
		
		return true;
	}

	@Override
	public DataType inputType(String source, String name)
	{
		
		Class<?> theClass = loadClass(source);
		
		Method[] methods = theClass.getMethods();
		Field[] fields = theClass.getFields();
		Constructor<?>[] constructors = theClass.getConstructors();
		
		for(Field f : fields){
			In inputAnnotation = getInputAnnotations(f);
			if(inputAnnotation != null && inputAnnotation.name().equals(name)){
				JavaType jType = new JavaType(f.getType());
				return jType;
			}
		}

		// Case where input are defined as parameter of constructors.
		for(Constructor c : constructors){
			Annotation [][] parameterAnnotations = c.getParameterAnnotations();
			for(int i =0; i< parameterAnnotations.length;i++){
				for(int j=0;j< parameterAnnotations[i].length;j++){
					if (parameterAnnotations[i][j] instanceof In)
					{ In in = (In)parameterAnnotations[i][j];
					  if(in.name().equals(name)){
						  Class[] paramTypes = c.getParameterTypes();
						  JavaType jType = new JavaType(paramTypes[i]);
						  return jType;
					  }
					}
				}
			}
		}
		
		// Case where inputs are defined as parameter of input factory method.
		for(Method m : methods){
			if(hasFactoryAnnotation(m)){
				Annotation [][] parameterAnnotations = m.getParameterAnnotations();
				for(int i =0; i< parameterAnnotations.length;i++){
					for(int j=0;j< parameterAnnotations[i].length;j++){
						if (parameterAnnotations[i][j] instanceof In)
						{ In in = (In)parameterAnnotations[i][j];
						  if(in.name().equals(name)){
							  Class[] paramTypes = m.getParameterTypes();
							  JavaType jType = new JavaType(paramTypes[i]);
							  return jType;
						  }
						}
					}
				}	
			}
			
		}
		
		
		throw new IllegalArgumentException("@In field with name "+name+" not found in "+source+" (perhaps it isn't public?).");
	}

	private Class<?> loadClass(String source)
	{
		ClassLoader classLoader = getClass().getClassLoader();
		Class <?> theClass = null;
		
		try
		{
			theClass = classLoader.loadClass(source);
		} catch (ClassNotFoundException e)
		{
			throw new IllegalArgumentException("Source class can not be loaded " + source);
		}
		return theClass;
	}

	private boolean  hasFactoryAnnotation(Method m)
	{
		Annotation[] annotations = m.getAnnotations();
		for(Annotation a : annotations){
			if( a instanceof Factory){
				return true;
			}
		}
		return false;
	}

	private In getInputAnnotations(Field f)
	{
		Annotation[] annotations = f.getAnnotations();
		for(Annotation a : annotations){
			if(a instanceof In){
				return (In)a;
			}
		}
		return null;
	}
	
	private Out getOutputAnnotations(Field f)
	{
		Annotation[] annotations = f.getAnnotations();
		for(Annotation a : annotations){
			if(a instanceof Out){
				return (Out)a;
			}
		}
		return null;
	}

	@Override
	/**
	 * Look within annotated source code and tries to find output named name.
	 * Return its java type as datatype.
	 */
	public DataType outputType(String source, String name)
	{
		Class<?> theClass = loadClass(source);
		Method[] methods = theClass.getMethods();
		Field[] fields = theClass.getFields();

		for (Field f : fields)
		{
			Out outputAnnotation = getOutputAnnotations(f);
			if (outputAnnotation != null
					&& outputAnnotation.name().equals(name))
			{
				JavaType jType = new JavaType(f.getType());
				return jType;
			}
		}

		for (Method m : methods)
		{
			
			Annotation[] annotations = m.getAnnotations();
			for(Annotation a : annotations){
				if(a instanceof Out){
					Out outAnnotation = (Out) a;
					if(outAnnotation.name().equals(name)){
						if(m.getReturnType().equals(Void.TYPE)){
							throw new IllegalArgumentException("@Out method with name "+((Out)a).name()+" has a void return type.");
						}
						JavaType jType = new JavaType(m.getReturnType());
						return jType;
					}
				}
				if(a instanceof Main){
					Main mainAnnotation = (Main)a;
					if(mainAnnotation.name().equals(name)){
						JavaType jType = new JavaType(m.getReturnType());
						return jType;
					}
				}
			}

		}
		
		
		throw new IllegalArgumentException("@Out field with name "+name+" not found in "+source+".");
	}

	@Override

	public List<String> outputs(String source)
	{
		Class <?> theClass = loadClass(source);
		List<String> outputNames = new ArrayList<String>();
		
		Method[] methods = theClass.getMethods();
		for(Method m : methods){
			
			Annotation[] annotations = m.getAnnotations();
			for(Annotation a : annotations){
				if(a instanceof Main){
					outputNames.add(0,((Main)a).name());
				}
				if(a instanceof Out){
					if(m.getReturnType().equals(Void.TYPE)){
						throw new IllegalArgumentException("@Out method with name "+((Out)a).name()+" has a void return type.");
					}
					outputNames.add(((Out)a).name());
				}
				
			}
		}
		
		Field[] fields = theClass.getFields();
		for(Field f : fields){
			Out outputAnnotation = getOutputAnnotations(f);
			if(outputAnnotation != null)
				outputNames.add(outputAnnotation.name());
		}
		
		return outputNames;
	}

	@Override
	public boolean valueMatches(Object value, DataType type)
	{
		//Because this is java domain
		if(!(type instanceof JavaType))
			return false;
		
		JavaType jType = (JavaType)type;
		
		return PlatformUtil.isAssignableFrom(jType.clazz(), value.getClass());
		
	}
	
	public static JavaDomain domain()
	{
		return domain;
	}

	@Override
	public String inputDescription(String source, String name)
	{
		
		Class<?> theClass = loadClass(source);
		
		Method[] methods = theClass.getMethods();
		Field[] fields = theClass.getFields();
		Constructor<?>[] constructors = theClass.getConstructors();
		
		for(Field f : fields){
			In inputAnnotation = getInputAnnotations(f);
			if(inputAnnotation != null && inputAnnotation.name().equals(name))
				return inputAnnotation.description();
		}

		// Case where input are defined as parameter of constructors.
		for(Constructor c : constructors){
			Annotation [][] parameterAnnotations = c.getParameterAnnotations();
			for(int i =0; i< parameterAnnotations.length;i++){
				for(int j=0;j< parameterAnnotations[i].length;j++){
					if (parameterAnnotations[i][j] instanceof In)
					{ In in = (In)parameterAnnotations[i][j];
					  if(in.name().equals(name))
							return in.description();
					}
				}
			}
		}
		
		// Case where inputs are defined as parameter of input factory method.
		for(Method m : methods){
			if(hasFactoryAnnotation(m)){
				Annotation [][] parameterAnnotations = m.getParameterAnnotations();
				for(int i =0; i< parameterAnnotations.length;i++){
					for(int j=0;j< parameterAnnotations[i].length;j++){
						if (parameterAnnotations[i][j] instanceof In)
						{ In in = (In)parameterAnnotations[i][j];
						  if(in.name().equals(name))
							  return in.description();
						}
					}
				}	
			}
			
		}
		
		throw new IllegalArgumentException("@In field with name "+name+" not found in "+source+".");
	}

	@Override
	public String outputDescription(String source, String name)
	{
		Class<?> theClass = loadClass(source);
		Method[] methods = theClass.getMethods();
		Field[] fields = theClass.getFields();

		for (Field f : fields)
		{
			Out outputAnnotation = getOutputAnnotations(f);
			if (outputAnnotation != null
					&& outputAnnotation.name().equals(name))
				  return outputAnnotation.description();

		}

		for (Method m : methods)
		{
			//if(m.getReturnType().equals(Void.TYPE)) continue;
			
			Annotation[] annotations = m.getAnnotations();
			for(Annotation a : annotations){
				if(a instanceof Out){
					Out outAnnotation = (Out) a;
					if(outAnnotation.name().equals(name))				
						return outAnnotation.description();

				}
				if(a instanceof Main){
					Main mainAnnotation = (Main)a;
					if(mainAnnotation.name().equals(name))				 
						return mainAnnotation.description();

				}
			}

		}
		
		
		throw new IllegalArgumentException("@In field with name "+name+" not found in "+source+".");
	}

	@Override
	public boolean validate(String source, List<String> errors) {
		
		Class<?> theClass = loadClass(source);
		Method[] methods = theClass.getDeclaredMethods();
		
		
		// Check if @Out, @Factory, @Main annotations are only used in public methods.
		for(Method m : methods){
			
			//We ignore all public methods, since they will have no problem with respect to our annotations
			if((m.getModifiers() & Modifier.PUBLIC) != 0) continue;
			
			Annotation[] annotations = m.getAnnotations();
			for(Annotation a : annotations){
				if(a instanceof Out || a instanceof Main || a instanceof Factory){
					errors.add( "Method " +m.getName()+ " from " +source+ " is not public while it is annotated as "+a);
				}
			}
			
		}
		
		// Check if there are more than one input type, whether their type are consistent.
		
		checkInputConsistency(source, errors);
		
		return errors.size()==0;
	}

	private void checkInputConsistency(String source, List<String> errors) {
		
		Class<?> theClass = loadClass(source);
		
		Method[] methods = theClass.getMethods();
		Field[] fields = theClass.getFields();
		Constructor<?>[] constructors = theClass.getConstructors();
		
		Map<String, JavaType> inputNameToTypeMap = new LinkedHashMap<String, JavaType>();
		
		for(Field f : fields){
			In inputAnnotation = getInputAnnotations(f);
			if(inputAnnotation != null){
				JavaType jType = new JavaType(f.getType());
				if(inputNameToTypeMap.containsKey(inputAnnotation.name())){
					JavaType storedType = inputNameToTypeMap.get(inputAnnotation.name());
					if(!storedType.equals(jType)){
						errors.add("Inputs named as : '"+inputAnnotation.name() + "' are declared using  different types : " + storedType.clazz()+ " != "+jType.clazz());
					}
				} else {
					inputNameToTypeMap.put(inputAnnotation.name(), jType);
				}
			}
		}

		// Case where input are defined as parameter of constructors.
		for(Constructor c : constructors){
			Annotation [][] parameterAnnotations = c.getParameterAnnotations();
			for(int i =0; i< parameterAnnotations.length;i++){
				for(int j=0;j< parameterAnnotations[i].length;j++){
					if (parameterAnnotations[i][j] instanceof In)
					{ In in = (In)parameterAnnotations[i][j];
					  Class<?>[] paramTypes = c.getParameterTypes();
					  JavaType jType = new JavaType(paramTypes[i]);
						if(inputNameToTypeMap.containsKey(in.name())){
							JavaType storedType = inputNameToTypeMap.get(in.name());
							if(!storedType.equals(jType)){
								errors.add("Inputs named as : '"+in.name() + "' are declared using  different types : " + storedType.clazz()+ " != "+jType.clazz());
							}
						} else {
							inputNameToTypeMap.put(in.name(), jType);
						}
					}
				}
			}
		}
		
		// Case where inputs are defined as parameter of input factory method.
		for(Method m : methods){
			if(hasFactoryAnnotation(m)){
				Annotation [][] parameterAnnotations = m.getParameterAnnotations();
				for(int i =0; i< parameterAnnotations.length;i++){
					for(int j=0;j< parameterAnnotations[i].length;j++){
						if (parameterAnnotations[i][j] instanceof In)
						{ In in = (In)parameterAnnotations[i][j];
						  Class <?>[] paramTypes = m.getParameterTypes();
						  JavaType jType = new JavaType(paramTypes[i]);
						  if(inputNameToTypeMap.containsKey(in.name())){
								JavaType storedType = inputNameToTypeMap.get(in.name());
								if(!storedType.equals(jType)){
									errors.add("Inputs named as : '"+in.name() + "' are declared using  different types : " + storedType.clazz()+ " != "+jType.clazz());
								}
							} else {
								inputNameToTypeMap.put(in.name(), jType);
						      }	  
						}
					}
				}	
			}
			
		}
		
	}

	@Override
	public boolean printInput(String source, String name)
	{
	Class<?> theClass = loadClass(source);
		
		Method[] methods = theClass.getMethods();
		Field[] fields = theClass.getFields();
		Constructor<?>[] constructors = theClass.getConstructors();
		
		for(Field f : fields){
			In inputAnnotation = getInputAnnotations(f);
			if(inputAnnotation != null && inputAnnotation.name().equals(name)){
				return inputAnnotation.print();
			}
		}

		// Case where input are defined as parameter of constructors.
		for(Constructor c : constructors){
			Annotation [][] parameterAnnotations = c.getParameterAnnotations();
			for(int i =0; i< parameterAnnotations.length;i++){
				for(int j=0;j< parameterAnnotations[i].length;j++){
					if (parameterAnnotations[i][j] instanceof In)
					{ In in = (In)parameterAnnotations[i][j];
					  if(in.name().equals(name)){
							return in.print();
					  }
					}
				}
			}
		}
		
		// Case where inputs are defined as parameter of input factory method.
		for(Method m : methods){
			if(hasFactoryAnnotation(m)){
				Annotation [][] parameterAnnotations = m.getParameterAnnotations();
				for(int i =0; i< parameterAnnotations.length;i++){
					for(int j=0;j< parameterAnnotations[i].length;j++){
						if (parameterAnnotations[i][j] instanceof In)
						{ In in = (In)parameterAnnotations[i][j];
						  if(in.name().equals(name)){
								return in.print();
						  }
						}
					}
				}	
			}
			
		}
		
		
		throw new IllegalArgumentException("@In field with name "+name+" not found in "+source+" (perhaps it isn't public?).");
	}

	@Override
	public boolean printOutput(String source, String name)
	{
		Class<?> theClass = loadClass(source);
		Method[] methods = theClass.getMethods();
		Field[] fields = theClass.getFields();

		for (Field f : fields)
		{
			Out outputAnnotation = getOutputAnnotations(f);
			if (outputAnnotation != null
					&& outputAnnotation.name().equals(name))
			{
				return outputAnnotation.print();
			}
		}

		for (Method m : methods)
		{
			
			Annotation[] annotations = m.getAnnotations();
			for(Annotation a : annotations){
				if(a instanceof Out){
					Out outAnnotation = (Out) a;
					if(outAnnotation.name().equals(name)){
						return outAnnotation.print();

					}
				}
				if(a instanceof Main){
					Main mainAnnotation = (Main)a;
					if(mainAnnotation.name().equals(name)){
						return mainAnnotation.print();
					}
				}
			}

		}
		
		throw new IllegalArgumentException("@Out field with name "+name+" not found in "+source+".");
	}



}
