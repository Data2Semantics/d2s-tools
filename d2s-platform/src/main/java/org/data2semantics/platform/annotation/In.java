package org.data2semantics.platform.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Annotation to mark a class field or a function parameter as an input of a 
 * module.
 * 
 * When annotating fields, the class should be a bean, tagged with @Module. 
 * 
 * When annotating parameters of a constructor, the constructor should be for a 
 * class tagged with @Module, and all parameters of the constructor should be 
 * tagged with @In.
 * 
 * When annotating a method, the method should be static, and return a class which 
 * is tagged with @Module. All parameters should be tagged.
 * 
 * 
 * @author wibisono
 *
 */

@Retention(RetentionPolicy.RUNTIME)
public @interface In {
	
	/**
	 * Name of this input
	 * @return
	 */
	public String name();
	
	public String description() default "";
	
	/**
	 * Whether the value is printed in reports
	 * @return
	 */
	public boolean print() default true;
}
