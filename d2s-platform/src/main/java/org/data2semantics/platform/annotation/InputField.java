package org.data2semantics.platform.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Annotation to mark a class field as an input of a module.
 * @author wibisono
 *
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface InputField {
	
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
