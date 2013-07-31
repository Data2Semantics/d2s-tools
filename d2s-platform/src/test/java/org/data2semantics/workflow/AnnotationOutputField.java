package org.data2semantics.workflow;

import org.data2semantics.platform.annotation.Main;
import org.data2semantics.platform.annotation.Out;

public class AnnotationOutputField {
	@Out(name ="intField")
	public int result=0;
	
	@Main
	public void setResult(int x){
		result =x;
	}
}