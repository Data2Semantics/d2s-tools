package org.data2semantics.exp.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.data2semantics.proppred.kernels.KernelUtils;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFGraphKernel;
import org.data2semantics.proppred.learners.Prediction;
import org.data2semantics.proppred.learners.evaluation.EvaluationFunction;
import org.data2semantics.proppred.learners.libsvm.LibSVM;
import org.data2semantics.proppred.learners.libsvm.LibSVMParameters;
import org.data2semantics.tools.rdf.RDFDataSet;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;

public class RDFGraphKernelExperiment extends KernelExperiment<RDFGraphKernel> {
	private LibSVMParameters svmParms;
	private List<Double> labels;
	private RDFDataSet dataset;
	private List<Resource> instances;
	private List<Statement> blackList;
	private List<EvaluationFunction> evalFunctions;
	private Result compR;
	private Map<EvaluationFunction, double[]> resultMap;
	



	public RDFGraphKernelExperiment(RDFGraphKernel kernel, long[] seeds,
			LibSVMParameters svmParms, RDFDataSet dataset,
			List<Resource> instances, List<Double> labels, List<Statement> blackList, List<EvaluationFunction> evalFunctions) {
		super(kernel, seeds);
		this.svmParms = svmParms;
		this.labels = labels;
		this.dataset = dataset;
		this.instances = instances;
		this.blackList = blackList;
		this.evalFunctions = evalFunctions;
		
		resultMap = new HashMap<EvaluationFunction,double[]>();
		
		for (EvaluationFunction evalFunc : evalFunctions) {
			Result res = new Result();
			double[] resA = new double[seeds.length];
			res.setLabel(evalFunc.getLabel());
			res.setHigherIsBetter(evalFunc.isHigherIsBetter());
			res.setScores(resA);
			results.add(res);
			resultMap.put(evalFunc, resA);
		}
		
		compR = new Result();
		results.add(compR);
	}



	public void run() {		
		long tic, toc;

		List<Double> tempLabels = new ArrayList<Double>();
		tempLabels.addAll(labels);

		tic = System.currentTimeMillis();
		double[][] matrix = kernel.compute(dataset, instances, blackList);
		toc = System.currentTimeMillis();

	
		compR.setLabel("kernel comp time");

		for (int j = 0; j < seeds.length; j++) {
			matrix = KernelUtils.shuffle(matrix, seeds[j]);
			Collections.shuffle(tempLabels, new Random(seeds[j]));		
			
			double[] target = new double[tempLabels.size()];
			for (int i = 0; i < target.length; i++) {
				target[i] = tempLabels.get(i);
			}


			Prediction[] pred = LibSVM.crossValidate(matrix, target, svmParms, svmParms.getNumFolds());
				
			for (EvaluationFunction ef : evalFunctions) {
				resultMap.get(ef)[j] = ef.computeScore(target, pred);	
			}	
		}

		double[] comp = {toc - tic};
		compR.setScores(comp);
	}
}
