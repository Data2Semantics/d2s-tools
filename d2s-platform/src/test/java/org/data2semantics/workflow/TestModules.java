package org.data2semantics.workflow;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map;

import org.data2semantics.modules.SimpleExtendedModule;
import org.data2semantics.platform.core.AbstractModule;
import org.data2semantics.platform.core.Workflow;
import org.data2semantics.platform.util.PlatformUtil;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;


public class TestModules {
	
	@Test
	public void testYamlSimple() throws FileNotFoundException{
			Yaml yaml = new Yaml();
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream("src/test/resources/simple.yaml"));
			Map  data = (Map) yaml.load(bis);
			System.out.println(data +"  "+ data.get("inputs"));
	}
	
	@Test
	public void testYamlModules() throws FileNotFoundException{
			Yaml yaml = new Yaml();
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream("src/test/resources/first-workflow.yaml"));
			Map  data = (Map) yaml.load(bis);
			ArrayList modules = (ArrayList) data.get("workflow");
			System.out.println(modules.get(0));
			
	}
	
	@Test
	public void testDumpExtendedModuleToYAML(){
			Workflow w = new Workflow("SimpleWorkflow");
			AbstractModule m = new SimpleExtendedModule(w);
			m.setName("tester");
			w.getModules().add(m);
			
			
			AbstractModule m1 = new SimpleExtendedModule(w);
			m1.setName("classifier");
			w.getModules().add(m1);
			
			Yaml yaml = new Yaml();
			System.out.println(yaml.dump(w));
			
	}
	
	@Test
	public void testSingleModule() throws FileNotFoundException, ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
			Yaml yaml = new Yaml();
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream("src/test/resources/single.yaml"));
		
			
			// Need to create bean instead of directly working with map
			Map  singleModule = (Map) yaml.load(bis);
			Map input = (Map) singleModule.get("inputs");
			
		
			ClassLoader currentLoader = this.getClass().getClassLoader();
			Class<?> myModuleClass = currentLoader.loadClass((String)singleModule.get("source"));
			
			
			// All these manual steps need to be extracted as part of the enactor, but this might help to see under the hood what is going on.
			if(PlatformUtil.hasModuleAnnotation(myModuleClass)){

				Method mainMethod = PlatformUtil.getMainMethod(myModuleClass);
				
				// Assuming module can be created just with default constructor
				Object myModuleObj = PlatformUtil.createModuleWithDefaultConstructor(myModuleClass);
				
				Object [] args = {(Integer)input.get("population size"), (Double)input.get("initial variance")};
				
				Object result = mainMethod.invoke(myModuleObj,args);
				
				System.out.println( result);
				
				
				//For each data/data set, assign them to the appropriate input parameter
				
				
				// Execute module.
			}
	}
	
	@Test
	public void testSingleAdder() throws FileNotFoundException, ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
			Yaml yaml = new Yaml();
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream("src/test/resources/single-adder.yaml"));
		
			
			// Need to create bean instead of directly working with map
			Map  singleModule = (Map) yaml.load(bis);
			Map input = (Map) singleModule.get("inputs");
			
		
			ClassLoader currentLoader = this.getClass().getClassLoader();
			Class<?> myModuleClass = currentLoader.loadClass((String)singleModule.get("source"));
			
			
			// All these manual steps need to be extracted as part of the enactor, but this might help to see under the hood what is going on.
			if(PlatformUtil.hasModuleAnnotation(myModuleClass)){

				Method mainMethod = PlatformUtil.getMainMethod(myModuleClass);
				
				// Assuming module can be created just with default constructor
				Object myModuleObj = PlatformUtil.createModuleWithDefaultConstructor(myModuleClass);
				
				Object [] args = {(Integer)input.get("first"), (Integer)input.get("second")};
				
				Object result = mainMethod.invoke(myModuleObj,args);
				
				System.out.println( result);
				
				
				//For each data/data set, assign them to the appropriate input parameter
				
				
				// Execute module.
			}
	}
	
	
	

	
}
