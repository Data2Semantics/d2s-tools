package org.data2semantics.exp.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.data2semantics.proppred.kernels.KernelUtils;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFFeatureVectorKernel;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFWLSubTreeKernel;
import org.data2semantics.proppred.kernels.text.TextUtils;
import org.data2semantics.proppred.learners.Prediction;
import org.data2semantics.proppred.learners.SparseVector;
import org.data2semantics.proppred.learners.evaluation.EvaluationFunction;
import org.data2semantics.proppred.learners.liblinear.LibLINEAR;
import org.data2semantics.proppred.learners.liblinear.LibLINEARModel;
import org.data2semantics.proppred.learners.liblinear.LibLINEARParameters;
import org.data2semantics.tools.rdf.RDFDataSet;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;

public class RDFLinearKernelExperiment extends KernelExperiment<RDFFeatureVectorKernel> {
	private LibLINEARParameters linearParms;
	private List<Double> labels;
	private RDFDataSet dataset;
	private List<Resource> instances;
	private List<Statement> blackList;
	private List<EvaluationFunction> evalFunctions;
	private Result compR;
	private Map<EvaluationFunction, double[]> resultMap;
	private boolean doCV;
	private boolean doTFIDF;
	private boolean doBinary;
	



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
	
		doCV = false;
		doTFIDF = false;
		doBinary = false;
		
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
		SparseVector[] fv = kernel.computeFeatureVectors(dataset, instances, blackList);
		toc = System.currentTimeMillis();
		
		if (doTFIDF) {
			fv = TextUtils.computeTFIDF(Arrays.asList(fv)).toArray(new SparseVector[1]);
			fv = KernelUtils.normalize(fv);
		}
		
		if (doBinary) {
			fv = KernelUtils.convert2BinaryFeatureVectors(fv);
			fv = KernelUtils.normalize(fv);
		}

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

			Prediction[] pred = null;
			double[] targetSplit = null;
			
			if (doCV) {
				pred = LibLINEAR.crossValidate(fv, target, linearParms, linearParms.getNumFolds());
				targetSplit = target;
			} else {
				pred = LibLINEAR.trainTestSplit(fv, target, linearParms, linearParms.getSplitFraction());
				targetSplit = LibLINEAR.splitTestTarget(target, linearParms.getSplitFraction());
				
				// If we deal with on RDFWLSubTree, then we show the most used features, based on the featuremap created
				if (kernel instanceof RDFWLSubTreeKernel) {
					RDFWLSubTreeKernel k = (RDFWLSubTreeKernel) kernel;
					LibLINEARModel model = LibLINEAR.trainLinearModel(fv, target, linearParms);
					LibLINEARModel.WeightIndexPair[][] fw = model.getFeatureWeights();
					
					Map<String, String> lm = k.getInverseLabelMap();
					
					System.out.println("Map size: " + lm.size() + " fw length: " + fw[0].length + " fv max index: " + fv[0].getLastIndex());
					
					for (LibLINEARModel.WeightIndexPair[] fwc : fw) {
						Arrays.sort(fwc);
						for (int i = 0; i < 10 && i < fwc.length; i++) {
							System.out.print(lm.get(Integer.toString(fwc[i].getIndex())));
							System.out.print(", ");
						}
						System.out.println("");
					}
					
				}
				
			}
			
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

	public void setDoCV(boolean cv) {
		doCV = cv;
	}
	
	public void setDoTFIDF(boolean tfidf) {
		doTFIDF = tfidf;
	}
	
	public void setDoBinary(boolean binary) {
		doBinary = binary;
	}

}
