package org.data2semantics.platform.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation marking the output of a module.
 * 
 * @author wibisono
 *
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OutputField {
	
	/**
	 * Name of this output
	 * @return
	 */
	public String name();
	
	/**
	 * Type of this output
	 * @return
	 */
	public String type() default "String";
	
}
