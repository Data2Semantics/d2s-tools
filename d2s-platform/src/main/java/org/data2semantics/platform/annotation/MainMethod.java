package org.data2semantics.platform.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark the main method of a class that we are going to wrap as a module
 * The result of this method call will immediately referred as result.
 * @author wibisono
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MainMethod {
	
	public String name() default "result";

}
