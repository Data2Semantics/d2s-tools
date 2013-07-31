package org.data2semantics.platform.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark that the given class describes a domain for the given 
 * prefix.
 * 
 * The class must have a static method, with no arguments called domain, which 
 * returns a singleton object for the given domain.
 * 
 * @author wibisono
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)

public @interface DomainDefinition {
	
	public String prefix();
	
	public String description() default "";
}
