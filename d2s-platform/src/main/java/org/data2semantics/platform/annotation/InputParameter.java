package org.data2semantics.platform.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a method parameter as an input.
 * @author wibisono
 *
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface InputParameter {
	
	/**
	 * Name of this input
	 * @return
	 */
	public String name();
	
	/**
	 * Type of this input.
	 * @return
	 */
	public String type() default "String";

}
