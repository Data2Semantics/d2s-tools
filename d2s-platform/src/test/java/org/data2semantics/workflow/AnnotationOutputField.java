package org.data2semantics.workflow;

import org.data2semantics.platform.annotation.MainMethod;
import org.data2semantics.platform.annotation.OutputField;

public class AnnotationOutputField {
	@OutputField(name ="intField")
	public int result=0;
	
	@MainMethod
	public void setResult(int x){
		result =x;
	}
}