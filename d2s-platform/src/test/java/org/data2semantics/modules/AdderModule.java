package org.data2semantics.modules;

import org.data2semantics.platform.annotation.In;
import org.data2semantics.platform.annotation.Main;
import org.data2semantics.platform.annotation.Module;

@Module(name="Adder", description="Adds two numbers together.")
public class AdderModule {
	
	@Main
	public int add(
			@In(name="first") Integer first, 
			@In(name="second") Integer second)
	{
		return first + second;
	}

}
