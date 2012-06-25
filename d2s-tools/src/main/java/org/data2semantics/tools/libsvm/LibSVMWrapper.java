package org.data2semantics.tools.libsvm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import cern.colt.Arrays;

public class LibSVMWrapper {

	public static double[] crossValidate(double[][] kernel, double[] target, int numberOfFolds, double[] c) {
		
		// New print interface, print nothing, i.e. unverbose
		svm.svm_set_print_string_function(new svm_print_interface()
		{
			public void print(String s)
			{
				; 
			}
		}
		);
			
		svm_parameter params = new svm_parameter();
		params.kernel_type = params.PRECOMPUTED;
		params.svm_type = params.C_SVC;
		params.nr_weight = 0;
		params.eps = 0.0001;
		params.shrinking = 1;
		params.probability = 0;
		
		double[] pred = new double[target.length];
		
		for (int fold = 1; fold <= numberOfFolds; fold++) {
			double[][] trainKernel = createTrainFold(kernel, numberOfFolds, fold);
			double[][] testKernel  = createTestFold(kernel, numberOfFolds, fold);
			double[] trainTarget  = createTargetTrainFold(target, numberOfFolds, fold);
			//double[] testTarget   = createTargetTestFold(target, numberOfFolds, fold);
			double score, bestScore = 0, bestC = 1;
			
			for (int i = 0; i < c.length; i++) {
				score = computeAccuracy(trainTarget, crossValidate(trainKernel, trainTarget, 4, c[i]));
	
				if (score > bestScore) {
					bestC = c[i];
					bestScore = score;
				}
			}
	
			params.C = bestC;
			pred = addFold2Prediction(testKernel(svm.svm_train(createSVMProblem(trainKernel, trainTarget), params), testKernel), pred, numberOfFolds, fold);
		}
		
		return pred;
	}
	
	
	public static double[] crossValidate(double[][] kernel, double[] target, int numberOfFolds, double c) {
		
		// New print interface, print nothing, i.e. unverbose
		svm.svm_set_print_string_function(new svm_print_interface()
		{
			public void print(String s)
			{
				; 
			}
		}
		);
		
		svm_parameter params = new svm_parameter();
		params.kernel_type = params.PRECOMPUTED;
		params.svm_type = params.C_SVC;
		params.C = c;
		params.nr_weight = 0;
		params.eps = 0.0001;
		params.shrinking = 1;
		params.probability = 0;
		double[] prediction = new double[target.length];
		
		svm.svm_cross_validation(createSVMProblem(kernel, target), params, numberOfFolds, prediction);
		
		return prediction;
	}
	

	public static double[] createTargets(List<String> labels) {
		Map<String, Integer> labelMap = new TreeMap<String, Integer>();
		double[] targets = new double[labels.size()];
		int t = 0;
		int i = 0;
		
		for (String label : labels) {
			if (!labelMap.containsKey(label)) {
				t += 1;
				labelMap.put(label, t);
			} 
			targets[i] = labelMap.get(label);	
			i++;
		}	
		return targets;
	}
	
	public static double computeAccuracy(double[] target, double[] prediction) {
		double correct = 0;	
		for (int i = 0; i < target.length; i++) {
			if (target[i] == prediction[i]) {
				correct += 1;
			}
		}
		return correct / ((double) target.length);		
	}

	public static double computeMeanAccuracy(double[] target, double[] prediction) {
		Map<Double, Double> targetCounts = computeClassCounts(target);
		double acc = 0, accTemp = 0;
		
		for (double label : targetCounts.keySet()) {
			for (int i = 0; i < prediction.length; i++) {
				if (prediction[i] == label && target[i] == label) {
					accTemp += 1;
				}
			}
			acc += (accTemp / targetCounts.get(label));
			accTemp = 0;
		}	
		return acc / ((double) targetCounts.size());
	}
	
	public static Map<Double, Double> computeClassCounts(double[] target) {
		Map<Double, Double> counts = new TreeMap<Double, Double>();

		for (int i = 0; i < target.length; i++) {
			if (!counts.containsKey(target[i])) {
				counts.put(target[i], 1.0);
			} else {
				counts.put(target[i], counts.get(target[i]) + 1);
			}
		}
		return counts;
	}
	
	
	
	/*
	 * Privates
	 * 
	 */	
	private static svm_problem createSVMProblem(double[][] kernel, double[] target) {
		svm_problem prob = new svm_problem();
		svm_node[][] nodes = new svm_node[target.length][target.length + 1];
		
		prob.l = target.length;
		prob.y = target;
		prob.x = nodes;
		
		for (int i = 0; i < nodes.length; i++) {
			nodes[i][0] = new svm_node();
			nodes[i][0].index = 0;
			nodes[i][0].value = i + 1;
			
			for (int j = 1; j < nodes[i].length; j++) {
				nodes[i][j] = new svm_node();
				nodes[i][j].index = 0;
				nodes[i][j].value = kernel[i][j-1];
			}
		}		
		return prob;		
	}
	
	private static svm_node[][] createTestProblem(double[][] testKernel) {
		svm_node[][] nodes = new svm_node[testKernel.length][];

		for (int i = 0; i < testKernel.length; i++) {
			nodes[i] = new svm_node[testKernel[i].length + 1];
			nodes[i][0] = new svm_node();
			nodes[i][0].index = 0;
			nodes[i][0].value = i + 1;

			for (int j = 1; j < nodes[i].length; j++) {
				nodes[i][j] = new svm_node();
				nodes[i][j].index = 0;
				nodes[i][j].value = testKernel[i][j-1];
			}			
		}	
		return nodes;		
	}
	
	
	// NOTE - replace svm_predict with svm_predict_values if we want to look at decision values
	private static double[] testKernel(svm_model model, double[][] testKernel) {
		svm_node[][] testNodes = createTestProblem(testKernel);
		double[] pred = new double[testNodes.length];
		
		for (int i = 0 ; i < testNodes.length; i++) {
			pred[i] = svm.svm_predict(model, testNodes[i]);
		}
		return pred;		
	}
	
	
	
	private static double[][] createTrainFold(double[][] kernel, int numberOfFolds, int fold) {
		int foldStart = Math.round((kernel.length / ((float) numberOfFolds)) * ((float) fold - 1));
		int foldEnd   = Math.round((kernel.length / ((float) numberOfFolds)) * ((float) fold));
		int foldLength = (foldEnd-foldStart);
		
		double[][] trainKernel = new double[kernel.length - foldLength][kernel.length - foldLength];
		
		for (int i = 0; i < foldStart; i++) {
			for (int j = 0; j < foldStart; j++) {
				trainKernel[i][j] = kernel[i][j];
			}
		}
		
		for (int i = foldEnd; i < kernel.length; i++) {
			for (int j = 0; j < foldStart; j++) {
				trainKernel[i - foldLength][j] = kernel[i][j];
			}
		}
		
		for (int i = 0; i < foldStart; i++) {
			for (int j = foldEnd; j < kernel.length; j++) {
				trainKernel[i][j - foldLength] = kernel[i][j];
			}
		}
		
		for (int i = foldEnd; i < kernel.length; i++) {
			for (int j = foldEnd; j < kernel.length; j++) {
				trainKernel[i - foldLength][j - foldLength] = kernel[i][j];
			}
		}
		
		return trainKernel;
	}
	
	private static double[][] createTestFold(double[][] kernel, int numberOfFolds, int fold) {
		int foldStart = Math.round((kernel.length / ((float) numberOfFolds)) * ((float) fold - 1));
		int foldEnd   = Math.round((kernel.length / ((float) numberOfFolds)) * ((float) fold));
		int foldLength = (foldEnd-foldStart);
		
		double[][] testKernel = new double[foldEnd - foldStart][kernel.length - foldLength];
	
		for (int i = 0; i < foldEnd - foldStart; i++) {
			for (int j = 0; j < foldStart; j++) {
				testKernel[i][j] = kernel[i + foldStart][j];
			}
			for (int j = foldEnd; j < kernel.length; j++) {
				testKernel[i][j - foldLength] = kernel[i + foldStart][j];
			}
		}
		
		return testKernel;
	}
	
	private static double[] createTargetTrainFold(double[] target, int numberOfFolds, int fold) {
		int foldStart = Math.round((target.length / ((float) numberOfFolds)) * ((float) fold - 1));
		int foldEnd   = Math.round((target.length / ((float) numberOfFolds)) * ((float) fold));
		int foldLength = (foldEnd-foldStart);
		
		double[] trainTargets = new double[target.length - foldLength];
		
		for (int i = 0; i < foldStart; i++) {
			trainTargets[i] = target[i];
		}	
		for (int i = foldEnd; i < target.length; i++) {
			trainTargets[i - foldLength] = target[i];
		}			
		return trainTargets;
	}
	
	private static double[] createTargetTestFold(double[] target, int numberOfFolds, int fold) {
		int foldStart = Math.round((target.length / ((float) numberOfFolds)) * ((float) fold - 1));
		int foldEnd   = Math.round((target.length / ((float) numberOfFolds)) * ((float) fold));
		int foldLength = (foldEnd-foldStart);
		
		double[] testTargets = new double[foldLength];
		
		for (int i = foldStart; i < foldEnd; i++) {
			testTargets[i - foldStart] = target[i];
		}			
		return testTargets;
	}
	
	private static double[] addFold2Prediction(double[] foldPred, double[] pred, int numberOfFolds, int fold) {
		int foldStart = Math.round((pred.length / ((float) numberOfFolds)) * ((float) fold - 1));
		int foldEnd   = Math.round((pred.length / ((float) numberOfFolds)) * ((float) fold));
		
		for (int i = foldStart; i < foldEnd; i++) {
			pred[i] = foldPred[i - foldStart];
		}
		return pred;
	}
	
}
