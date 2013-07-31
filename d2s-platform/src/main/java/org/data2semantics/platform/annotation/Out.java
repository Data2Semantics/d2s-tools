package org.data2semantics.platform.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation marking the output of a module. Can annotate either a field or a 
 * module.
 * 
 * @author wibisono
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Out {
	
	/**
	 * Name of this output
	 * @return
	 */
	public String name();
	
	public String description() default "";

}
