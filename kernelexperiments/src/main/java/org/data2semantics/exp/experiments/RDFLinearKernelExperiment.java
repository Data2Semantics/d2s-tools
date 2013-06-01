package org.data2semantics.exp.experiments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.data2semantics.proppred.kernels.Kernel;
import org.data2semantics.proppred.kernels.RDFFeatureVectorKernel;
import org.data2semantics.proppred.kernels.RDFGraphKernel;
import org.data2semantics.proppred.kernels.RDFWLSubTreeKernel;
import org.data2semantics.proppred.libsvm.LibLINEAR;
import org.data2semantics.proppred.libsvm.LibLINEARParameters;
import org.data2semantics.proppred.libsvm.LibSVM;
import org.data2semantics.proppred.libsvm.LibSVMParameters;
import org.data2semantics.proppred.libsvm.Prediction;
import org.data2semantics.proppred.libsvm.SparseVector;
import org.data2semantics.proppred.libsvm.evaluation.EvaluationFunction;
import org.data2semantics.proppred.libsvm.evaluation.Task1Score;
import org.data2semantics.tools.rdf.RDFDataSet;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.util.LiteralUtil;

public class RDFLinearKernelExperiment extends KernelExperiment<RDFFeatureVectorKernel> {
	private LibLINEARParameters linearParms;
	private List<Double> labels;
	private RDFDataSet dataset;
	private List<Resource> instances;
	private List<Statement> blackList;
	private List<EvaluationFunction> evalFunctions;
	private Result compR;
	private Map<EvaluationFunction, double[]> resultMap;
	



	public RDFLinearKernelExperiment(RDFFeatureVectorKernel kernel, long[] seeds,
			LibLINEARParameters linearParms, RDFDataSet dataset,
			List<Resource> instances, List<Double> labels, List<Statement> blackList, List<EvaluationFunction> evalFunctions) {
		super(kernel, seeds);
		this.linearParms = linearParms;
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
		SparseVector[] fv = kernel.computeFeatureVectors(dataset, instances, blackList);
		toc = System.currentTimeMillis();

		List<SparseVector> fvList = Arrays.asList(fv);

	
		compR.setLabel("kernel comp time");

		for (int j = 0; j < seeds.length; j++) {
			Collections.shuffle(fvList, new Random(seeds[j]));
			Collections.shuffle(tempLabels, new Random(seeds[j]));		
			fv = fvList.toArray(new SparseVector[1]);
			double[] target = new double[tempLabels.size()];
			for (int i = 0; i < target.length; i++) {
				target[i] = tempLabels.get(i);
			}

			
			/*
			// if we do regression, we need different targets
			if (linearParms.getAlgorithm() == linearParms.SVR_DUAL || linearParms.getAlgorithm() == linearParms.SVR_DUAL) {
				for (int i = 0; i < target.length; i++) {
					target[i] = LiteralUtil.getDoubleValue(tempLabels.get(i),0);
				}
			} else {
				// set the weights man
				Map<Double, Double> counts = LibSVM.computeClassCounts(target);
				int[] wLabels = new int[counts.size()];
				double[] weights = new double[counts.size()];

				for (double label : counts.keySet()) {
					wLabels[(int) label - 1] = (int) label;
					weights[(int) label - 1] = 1 / counts.get(label);
				}
				linearParms.setWeightLabels(wLabels);
				linearParms.setWeights(weights);
			}
			*/

			
			
			Prediction[] pred = LibLINEAR.trainTestSplit(fv, target, linearParms, linearParms.getSplitFraction());
			double[] targetSplit = LibLINEAR.splitTestTarget(target, linearParms.getSplitFraction());

			
			for (EvaluationFunction ef : evalFunctions) {
				resultMap.get(ef)[j] = ef.computeScore(targetSplit, pred);	
			}
			
			/*
			
			if (linearParms.getAlgorithm() == linearParms.SVR_DUAL || linearParms.getAlgorithm() == linearParms.SVR_DUAL) {
				accR.setLabel("task1score");
				f1R.setLabel("mae");
				//acc[j] = LibSVM.computeMeanSquaredError(targetSplit, LibSVM.extractLabels(pred));
				Task1Score scorer = new Task1Score();
				acc[j] = scorer.computeScore(targetSplit, pred);
				f1[j] = LibSVM.computeMeanAbsoluteError(targetSplit, LibSVM.extractLabels(pred));
				
				*/
				/*
				double[] pred2 = LibSVM.extractLabels(pred);
				for (int i = 0; i < targetSplit.length; i++) {
					System.out.println("Real: " + targetSplit[i] + ", Predicted: " + pred2[i]);
				}
				*/
				
			
			/*
			
			} else {
				accR.setLabel("acc");
				f1R.setLabel("f1");
				acc[j] = LibSVM.computeAccuracy(targetSplit, LibSVM.extractLabels(pred));
				f1[j]  = LibSVM.computeF1(targetSplit, LibSVM.extractLabels(pred));
			} */
		} 

		double[] comp = {toc - tic};
		compR.setScores(comp);
	}


}
