package org.data2semantics.modules;

import java.util.ArrayList;
import java.util.List;

import org.data2semantics.exp.utils.RDFLinearKernelExperiment;
import org.data2semantics.platform.annotation.Factory;
import org.data2semantics.platform.annotation.In;
import org.data2semantics.platform.annotation.Main;
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
	
	private LibLINEARParameters linParms;
	private RDFDataSet dataset;
	private ArrayList<Resource> instances;
	private ArrayList<Value> labels;
	private ArrayList<Statement> blackList;
	private List<Double> target;
	private int seed;
	private int iteration;
	private int depth;
	
	public LinearKernelExperimentModule(
			LibLINEARParameters linParms,
			RDFDataSet dataset, ArrayList<Resource> instances,
			ArrayList<Value> labels, ArrayList<Statement> blackList,
			List<Double> target, int seed, int iteration, int depth
			
			) {
		super();
		this.linParms = linParms;
		this.dataset = dataset;
		this.instances = instances;
		this.labels = labels;
		this.blackList = blackList;
		this.target = target;
		this.seed = seed;
		this.iteration = iteration;
		this.depth = depth;
	}

	@Factory
	public static LinearKernelExperimentModule getLinearKernelExperiment(
			@In(name="linParms") LibLINEARParameters linParms,
			@In(name="dataset") RDFDataSet dataset,
			@In(name="instances") ArrayList<Resource> instances, 
			@In(name="labels") ArrayList<Value> labels, 
			@In(name="blacklist")  ArrayList<Statement> blackList,
			@In(name="target") List<Double> target,
			@In(name="seed") int seed,
			@In(name="iteration") int iteration,
			@In(name="depth") int depth
			){
		
		return new LinearKernelExperimentModule(linParms, dataset, instances, labels, blackList, target, seed, iteration, depth);
	}
	
	@Main
	public void mainKernelMethod(){
		
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
