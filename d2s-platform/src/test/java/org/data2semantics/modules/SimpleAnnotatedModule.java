package org.data2semantics.modules;


import org.data2semantics.platform.annotation.In;
import org.data2semantics.platform.annotation.Main;
import org.data2semantics.platform.annotation.Module;


@Module(name="Test module")
public class SimpleAnnotatedModule {


	@Main
	public String mainExperiment(
			@In(name="population size") int popsize, 
			@In(name="initial variance") double initVar){
		return "Executing experiment with population size " + popsize + " initial variance "+initVar;
	}

}


