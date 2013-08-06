package org.data2semantics.modules;

import org.data2semantics.platform.annotation.Factory;
import org.data2semantics.platform.annotation.In;
import org.data2semantics.platform.annotation.Main;
import org.data2semantics.platform.annotation.Module;
import org.data2semantics.platform.annotation.Out;

@Module(name="Adder", description="Adds two numbers together.")
public class AdderModule {
	
	@In(name="first")
	public Integer first;
	
	
	@In(name="second")
	public Integer second;
	
	@Main
	public int result()	{
		difference = first-second;
		return first + second;
	}

	@Out(name="product")
	public int product(){
		return first*second;
	}
	
	@Out(name="difference")
	public Integer difference;
}
