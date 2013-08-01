package org.data2semantics.modules;

import org.data2semantics.platform.annotation.Factory;
import org.data2semantics.platform.annotation.In;
import org.data2semantics.platform.annotation.Main;
import org.data2semantics.platform.annotation.Module;

@Module(name="Adder", description="Adds two numbers together.")
public class AdderModule {
	
	@In(name="first")
	public Integer first;
	
	
	@In(name="second")
	public Integer second;
	
	@Main
	public int result()	{
		return first + second;
	}


}
