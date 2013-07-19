package org.data2semantics.modules;


import org.data2semantics.platform.annotation.InputParameter;
import org.data2semantics.platform.annotation.MainMethod;
import org.data2semantics.platform.annotation.Module;


@Module(name="Test module")
public class SimpleAnnotatedModule {


	@MainMethod
	public String mainExperiment(@InputParameter(name="population size") int popsize, @InputParameter(name="initial variance") double initVar){
		return "Executing experiment with population size " + popsize + " initial variance "+initVar;
	}

}


