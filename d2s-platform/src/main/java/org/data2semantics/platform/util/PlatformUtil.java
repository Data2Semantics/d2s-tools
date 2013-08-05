package org.data2semantics.platform.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;

import org.data2semantics.platform.annotation.In;
import org.data2semantics.platform.annotation.Main;
import org.data2semantics.platform.annotation.Module;
import org.data2semantics.platform.annotation.Out;

public class PlatformUtil {
	private final static Logger LOG = Logger.getLogger(PlatformUtil.class.getName());
	/**
	 * Call default constructor of a module and return created object
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	
	public static Object createModuleWithDefaultConstructor(Class<?> c) {

		Constructor<?> [] constructors = c.getConstructors();
		
		
		Object myModuleObj = null;
		
		try {
			myModuleObj = constructors[0].newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		
		return myModuleObj;
	}
	
	/**
	 * Checking if class c is a platform module, by looking if it is annotated with Module Annotation.
	 * @param c
	 * @return
	 */
	public static boolean hasModuleAnnotation(Class<?>  c){
		Annotation [] annotations = c.getAnnotations();
		for(Annotation a : annotations)
			if(a instanceof Module) return true;
		
		return false;
	}
	
	
	
	/**
	 * Getting the first found main method in an annotated module.
	 * @param c
	 * @return
	 */
	public static Method getMainMethod(Class<?> c ){
		
		Method[] methods = c.getMethods();
		
		for(Method m : methods){
			Annotation [] methans = m.getAnnotations();
			for(Annotation a : methans)
				if(a instanceof Main){
					return m;
				}
		}
		
		return null;
				
	}
	
	/**
	 * Getting the first found main method name in an annotated module.
	 * @param c
	 * @return
	 */
	public static Main getMainAnnotation(Class<?> c ){
		
		Method[] methods = c.getMethods();
		
		for(Method m : methods){
			Annotation [] methans = m.getAnnotations();
			for(Annotation a : methans)
				if(a instanceof Main){
					return  (Main)a;
				}
		}
		
		return null;
				
	}
	
	/**
	 * Get list of public fields which is annotated as input of modules.
	 * @param c
	 * @return
	 */
	public static List<Field> getInputFields(Class<?> c){
		List<Field> result = new ArrayList<Field>();
		
		Field[] fields = c.getFields();
		
		for(Field f : fields){
			Annotation [] fieldAnnotations = f.getAnnotations();
			for(Annotation a : fieldAnnotations){
				if(a instanceof In){
					result.add(f);
					break;
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Getting method with one of parameter annotated as input parameter
	 * @param c
	 * @return
	 */
	public static List<Method> getInputParameter(Class <?> c){
		List<Method> result = new ArrayList<Method>();
		
		Method[] methods = c.getDeclaredMethods();
		
		for(Method m : methods){
			Annotation [][] annotations = m.getParameterAnnotations();
			Class<?> [] paramTypes = m.getParameterTypes();
			
			
			for(int i=0;i<annotations.length;i++){
				for(Annotation curAnnotation : annotations[i]){
					if(curAnnotation instanceof In){
						result.add(m);
						System.out.println("DEBUG "+paramTypes[i] + " annotation "+curAnnotation);
						break;
					}
				}
			}
		}
		
		return result;
	}

	public static boolean checkParameterCompatibilility(Method mainMethod, Object[] args) {
		
		Class <?> parameterTypes [] = mainMethod.getParameterTypes();
		
		
		for(int i=0;i<args.length;i++){
			if(!isAssignableFrom(parameterTypes[i], args[i].getClass())){
				LOG.info(parameterTypes[i]+ " is not assignable to " + args[i].getClass());
				return false;
			}
		}
		
		
		return true;
	}

	public static boolean isAssignableFrom(Class <?> superClass,  Class <?> subClass ){
			Class <?> boxedSource = superClass.isPrimitive() ? getPrimitiveBox(superClass) : superClass;
			Class <?> boxedTarget = subClass.isPrimitive() ? getPrimitiveBox(subClass) : subClass;
			return boxedSource.isAssignableFrom(boxedTarget);
	}

	private static Class<?> getPrimitiveBox(Class<?> source) {
		if(source == boolean.class) return Boolean.class;
		if(source == byte.class) return Byte.class;
		if(source == char.class) return Character.class;
		if(source == short.class) return Short.class;
		if(source == int.class) return Integer.class;
		if(source == long.class) return Long.class;
		if(source == float.class) return Float.class;
		if(source == double.class) return Double.class;
			
		return null;
	}

	/**
	 * This function should expand arguments in the args, according to (if) acceptable parameters in main methods.
	 * If this function failed to unroll arguments, according to the unrolling strategy then we will return null.
	 * @param mainMethod
	 * @param args
	 * @return result will be a list of expanded arguments, each matching the parameter signature of the main method.
	 */
	public static List<Object[]> expandArguments(Method mainMethod,
			Object[] args) {
		
	
		List<Object[]> result = new ArrayList<Object[]>();
		Class<?> [] parameters = mainMethod.getParameterTypes();
		
		// Marking not all parameter needs to be unrolled.
		boolean [] tobeExpanded = new boolean[parameters.length];
		
		for(int i=0;i<parameters.length;i++){
			
			// In the case where we can assign, no need to expand
			if(isAssignableFrom(parameters[i], args[i].getClass()))
					tobeExpanded[i] = false;
			else
			if(expandable(args[i], parameters[i]))
				tobeExpanded[i] = true;
			else
				// We can't expand if the thing is not expandable. Perhaps I should throw exception instead here.
				return null;
			
		}
		
		Object[] temp =  new Object[parameters.length];
		
		
		unroll(0, temp, result, args, tobeExpanded);
		
		return result;
	}

	public  static void unroll(int i, Object[] curentAssignment, List<Object[]> result,
			Object[] arguments, boolean[] tobeExpanded) {
			int N = curentAssignment.length;
			if(i == N){
				result.add(curentAssignment.clone());
				return;
			}
			
			if(tobeExpanded[i]){
				Collection currentCol = ((Collection)arguments[i]);
				for(Object current : currentCol ){
					curentAssignment[i] = current;
					unroll(i+1, curentAssignment, result, arguments, tobeExpanded);
				}
			} else {
				curentAssignment[i] = arguments[i];
				unroll(i+1, curentAssignment, result, arguments, tobeExpanded);
			}
	}

	private static boolean expandable(Object object, Class<?> class1) {
		
		if(!(object instanceof Collection)) return false;
		
		Collection coll = (Collection) object;
		Object checkMember = coll.iterator().next(); 
		
		return isAssignableFrom(class1, checkMember.getClass());
	}

	public static Object[] getArgumentsfromInput(Method mainMethod,
			Map<String, Object> actualInputMap) {
		Class<?>[] parameterTypes = mainMethod.getParameterTypes();
		Annotation[][] annotations = mainMethod.getParameterAnnotations();
		
		int nParam = parameterTypes.length;
		Object [] result = new Object[nParam];
		
		for(int i = 0; i < nParam; i++){
			
			// Let's for now assume that we care only for the first annotation
			if(annotations[i].length > 0 )
			if(annotations[i][0] instanceof In){
				In ip = (In) annotations[i][0];
				result[i] = actualInputMap.get(ip.name());
				LOG.info("Getting " + ip.name() + " " + result[i]);
			}
			
		}
		
		return result;
	}
	
	/**
	 * Based on annotated output field name that we wanted to access, using this utility method we can get the value of the current field.
	 * 
	 * @param instance
	 * @param outputName
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public static Object getOutputField(Object instance, String outputName) throws IllegalArgumentException, IllegalAccessException{
		Object result= null;
		for(Field f : instance.getClass().getDeclaredFields()){
			if(f.isAnnotationPresent(Out.class)){
				Out outputAnnotation = f.getAnnotation(Out.class);
				if(outputName.equals(outputAnnotation.name())){
					return f.get(instance);
				}
			}
		}
		
		return result;
	}
	
	public static Collection<String> getAllOutputNames(Class<?> clz){
			Collection<String> result = new Vector<String>();
			
			for(Field f: clz.getDeclaredFields()){
				if(f.isAnnotationPresent(Out.class)){
					Out outputAnnotation = f.getAnnotation(Out.class);
					result.add(outputAnnotation.name());	
				}
			}
			
			return result;
	}
}
