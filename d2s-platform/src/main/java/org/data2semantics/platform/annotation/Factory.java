package org.data2semantics.platform.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * Signals that a method can be used in place of constructor to construct the module. 
 * Using the @In within a method parameters requires this @Factory.
 * 
 * @author Peter
 *
 */

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)

public @interface Factory
{

}
