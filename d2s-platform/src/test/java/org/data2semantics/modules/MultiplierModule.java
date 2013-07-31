package org.data2semantics.modules;

import org.data2semantics.platform.annotation.In;
import org.data2semantics.platform.annotation.Main;
import org.data2semantics.platform.annotation.Module;

@Module(name="Multiplier")
public class MultiplierModule {
	
	@Main
	public Integer multiplyNumber(
			@In(name="first") Integer first, 
			@In(name="second") Integer second){
		Integer result = first * second;
		return result;
	}

}
