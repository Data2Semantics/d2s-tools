package org.data2semantics.exp.molecules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.data2semantics.exp.utils.KernelExperiment;
import org.data2semantics.exp.utils.Result;
import org.data2semantics.proppred.kernels.KernelUtils;
import org.data2semantics.proppred.learners.Prediction;
import org.data2semantics.proppred.learners.evaluation.EvaluationFunction;
import org.data2semantics.proppred.learners.libsvm.LibSVM;
import org.data2semantics.proppred.learners.libsvm.LibSVMParameters;
import org.nodes.UGraph;

public class MoleculeGraphExperiment<G> extends KernelExperiment<MoleculeKernel<G>> {
	private LibSVMParameters svmParms;
	private List<Double> labels;
	private List<G> graphs;
	private List<EvaluationFunction> evalFunctions;
	private Result compR;
	private Map<EvaluationFunction, double[]> resultMap;
	
	
	
	public MoleculeGraphExperiment(MoleculeKernel<G> kernel, long[] seeds, LibSVMParameters svmParms, List<G> graphs, List<Double> labels, List<EvaluationFunction> evalFunctions) {
		super(kernel, seeds);
		this.svmParms = svmParms;
		this.labels = labels;
		this.graphs = graphs;
		this.evalFunctions = evalFunctions;
		
		resultMap = new HashMap<EvaluationFunction,double[]>();
		
		for (EvaluationFunction evalFunc : evalFunctions) {
			Result res = new Result();
			double[] resA = new double[seeds.length];
			res.setLabel(evalFunc.getLabel());
			res.setScores(resA);
			res.setHigherIsBetter(evalFunc.isHigherIsBetter());
			results.add(res);
			resultMap.put(evalFunc, resA);
		}
		
		compR = new Result();
		results.add(compR);
	}

	@Override
	public void run() {
		long tic, toc;

		List<Double> tempLabels = new ArrayList<Double>();
		tempLabels.addAll(labels);

		tic = System.currentTimeMillis();
		System.out.println("Computing kernel...");
		double[][] matrix = kernel.compute(graphs);
		toc = System.currentTimeMillis();

		compR.setLabel("kernel comp time");

		System.out.println("Performing CV...");
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
