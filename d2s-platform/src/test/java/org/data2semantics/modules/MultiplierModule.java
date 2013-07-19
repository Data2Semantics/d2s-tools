package org.data2semantics.modules;

import org.data2semantics.platform.annotation.InputParameter;
import org.data2semantics.platform.annotation.MainMethod;
import org.data2semantics.platform.annotation.Module;

@Module(name="Multiplier")
public class MultiplierModule {
	
	@MainMethod
	public Integer multiplyNumber(@InputParameter(name="first") Integer first, @InputParameter(name="second") Integer second){
		Integer result = first * second;
		return result;
	}

}
