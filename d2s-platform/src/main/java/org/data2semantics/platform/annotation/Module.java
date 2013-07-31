package org.data2semantics.platform.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)

/**
 * Signifies that a given java class can be used as a module. 
 * 
 * 
 * @author Peter
 */
public @interface Module 
{
	public String name();
	
	public String description() default "";
}
