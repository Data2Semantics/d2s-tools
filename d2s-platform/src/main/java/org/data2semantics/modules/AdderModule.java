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
	public Integer result()	{
		difference = first - second;
		return first + second;
	}

	@Out(name="product", description="The product (ie. the result of multiplication) of the two arguments.")
	public Integer product(){
		return first*second;
	}
	
	@Out(name="difference", description="The first argument subtracted by the second.")
	public Integer difference;
}
