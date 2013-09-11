package org.data2semantics.platform.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark the main method of a class that we are going to wrap as a 
 * module.
 * 
 * This method's output will always be one of the module's outputs.
 * 
 * 
 * @author wibisono
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)

public @interface Main {
	
	public String name() default "result";
	
	public String description() default "";
	
	public boolean print() default true;
}
