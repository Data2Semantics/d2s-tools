package org.data2semantics.platform.domain;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.data2semantics.platform.annotation.DomainDefinition;
import org.data2semantics.platform.annotation.Factory;
import org.data2semantics.platform.annotation.In;
import org.data2semantics.platform.annotation.Out;
import org.data2semantics.platform.core.ModuleInstance;
import org.data2semantics.platform.core.data.DataType;
import org.data2semantics.platform.core.data.Input;
import org.data2semantics.platform.core.data.JavaType;
import org.data2semantics.platform.core.data.Output;

@DomainDefinition(prefix="java")
public class JavaDomain implements Domain
{
	private static JavaDomain domain = new JavaDomain();
	
	@Override
	public boolean typeMatches(Output output, Input input)
	{
		return false; // TODO
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
	public boolean execute(ModuleInstance instance, List<String> errors)
	{
		return false; // TODO
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
		
		
		return null;
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
			// TODO Auto-generated catch block
			e.printStackTrace();
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
						JavaType jType = new JavaType(m.getReturnType());
						return jType;
					}
					
				}
			}

		}
		
		
		return null;
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
				if(a instanceof Out){
					outputNames.add(((Out)a).name());
				}
			}
		}
		
		Field[] fields = theClass.getFields();
		for(Field f : fields){
			Out outputAnnotation = getOutputAnnotations(f);
			outputNames.add(outputAnnotation.name());
		}
		
		return outputNames;
	}

	@Override
	public boolean valueMatches(Object value, DataType type)
	{
		// TODO Auto-generated method stub
		return false;
	}
	
	public static JavaDomain domain()
	{
		return domain;
	}

}
