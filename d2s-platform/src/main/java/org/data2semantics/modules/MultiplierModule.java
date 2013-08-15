package org.data2semantics.modules;

import org.data2semantics.platform.annotation.Factory;
import org.data2semantics.platform.annotation.In;
import org.data2semantics.platform.annotation.Main;
import org.data2semantics.platform.annotation.Module;

@Module(name="Multiplier")
public class MultiplierModule {
	
	int first, second;
	
	MultiplierModule(int f, int s){
		first =f; second = s;
	}
	
	@Main
	public int result(){
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return first*second;
	}
	
	@Factory
	public static MultiplierModule multiplierFactory(@In(name="first") int first, @In(name="second") int second){
		return new MultiplierModule(first,second);
	}

}
