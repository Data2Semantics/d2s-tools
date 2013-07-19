package org.data2semantics.modules;

import java.util.ArrayList;
import java.util.List;

import org.data2semantics.exp.utils.RDFLinearKernelExperiment;
import org.data2semantics.platform.annotation.InputParameter;
import org.data2semantics.platform.annotation.MainMethod;
import org.data2semantics.platform.annotation.Module;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFWLSubTreeKernel;
import org.data2semantics.proppred.learners.evaluation.Accuracy;
import org.data2semantics.proppred.learners.evaluation.EvaluationFunction;
import org.data2semantics.proppred.learners.evaluation.F1;
import org.data2semantics.proppred.learners.liblinear.LibLINEARParameters;
import org.data2semantics.tools.rdf.RDFDataSet;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;

@Module(name="LinearKernelExperiment")
public class LinearKernelExperimentModule {
	
	@MainMethod
	public void mainKernelMethod(
			@InputParameter(name="linParms") LibLINEARParameters linParms,
			@InputParameter(name="dataset") RDFDataSet dataset,
			@InputParameter(name="instances") ArrayList<Resource> instances, 
			@InputParameter(name="labels") ArrayList<Value> labels, 
			@InputParameter(name="blacklist")  ArrayList<Statement> blackList,
			@InputParameter(name="target") List<Double> target,
			@InputParameter(name="seed") int seed,
			 @InputParameter(name="iteration") int iteration,
			 @InputParameter(name="depth") int depth
			){
		
		List<EvaluationFunction> evalFuncs = new ArrayList<EvaluationFunction>();
		evalFuncs.add(new Accuracy());
		evalFuncs.add(new F1());
		long[] seeds2={seed};
		System.out.println(dataset + " " + labels.size() + " " + instances.size() + " " + blackList.size());
		
		RDFLinearKernelExperiment exp = new RDFLinearKernelExperiment(new RDFWLSubTreeKernel(iteration, depth, true, true), seeds2, linParms, dataset, instances, target, blackList, evalFuncs);
		
		exp.setDoCV(true);
		exp.run();
		
		System.out.println(exp.getResults());
	}
}
