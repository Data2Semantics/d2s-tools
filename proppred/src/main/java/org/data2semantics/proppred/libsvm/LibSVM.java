package org.data2semantics.proppred.libsvm;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


/**
 * Wrapper class for the LibSVM library. This class provides methods to inferface with the libsvm library.
 * Even though this library is Java, it is a very ugly literal translation of the C code. 
 * Currently the library is focussed on using kernels with LibSVM.
 * 
 * @author Gerben
 *
 */
public class LibSVM {
		
	/** 
	 * This function trains an SVM using a kernel matrix and outputs an LibSVMModel
	 * 
	 * @param kernel, a symmetric kernel matrix
	 * @param target, an array of labels the same length of the height/width of the kernel matrix
	 * @param params, a LibSVMParameters object, supplying the parameters for the SVM
	 * 
	 * @return a trained LibSVMModel
	 *
	 */
	public static LibSVMModel trainSVMModel(double[][] kernel, double[] target, LibSVMParameters params) {
		if (!params.isVerbose()) {
			setNoOutput();
		}
		
		double[] prediction = new double[target.length];
		svm_parameter svmParams = params.getParams();
		svm_problem svmProb = createSVMProblem(kernel, target);
		
		double score, bestScore = 0, bestC = 1;
		double[] itParams;
		
		// If we use a C_SVC, we use the C's, else we use the Nu's 
		if (svmParams.svm_type == svmParams.C_SVC) {
			itParams = params.getCs();
		} else {
			itParams = params.getNus();
		}
		
		// Parameter selection
		for (double c : itParams) {
			if (svmParams.svm_type == svmParams.C_SVC) {
				svmParams.C = c;
			} else {
				svmParams.nu = c;
			}
			svm.svm_cross_validation(svmProb, svmParams, 10, prediction);
			score = computeAccuracy(target, prediction);
			
			if (score > bestScore) {
				bestC = c;
				bestScore = score;
			}
		}
		
		// Train the model for the best parameter setting
		svmParams.C = bestC;			
		return new LibSVMModel(svm.svm_train(svmProb, svmParams));
	}
	
	
	/**
	 * Use a trained LibSVMModel to generate a prediction for new instances.
	 *
	 * 
	 * @param model, the trained model
	 * @param kernel, a kernel matrix for the test instances, the rows (first index) in this matrix are the test instances and the columns (second index) the train instances
	 * @return An array of LibSVMPrediction's 
	 * 
	 */
	public static LibSVMPrediction[] testSVMModel(LibSVMModel model, double[][] kernel) {
		svm_node[][] testNodes = createTestProblem(kernel);
		LibSVMPrediction[] pred = new LibSVMPrediction[testNodes.length];
				
		for (int i = 0 ; i < testNodes.length; i++) {
			double[] decVal = new double[model.getModel().nr_class*(model.getModel().nr_class-1)/2];
			pred[i] = new LibSVMPrediction(svm.svm_predict_values(model.getModel(), testNodes[i], decVal), i);
			pred[i].setDecisionValue(decVal);
		}
		return pred;
	}
	
	
	/**
	 * Convenience method to do a cross-validation experiment with a kernel
	 * 
	 * @param kernel, a symmetric kernel matrix
	 * @param target, the labels, length of the height/width of the matrix
	 * @param params
	 * @param numberOfFolds
	 * @return An array of LibSVMPrediction's the length of the target
	 */
	public static LibSVMPrediction[] crossValidate(double[][] kernel, double[] target, LibSVMParameters params,  int numberOfFolds) {
		LibSVMPrediction[] pred = new LibSVMPrediction[target.length];
		
		for (int fold = 1; fold <= numberOfFolds; fold++) {
			double[][] trainKernel = createTrainFold(kernel, numberOfFolds, fold);
			double[][] testKernel  = createTestFold(kernel, numberOfFolds, fold);
			double[] trainTarget  = createTargetTrainFold(target, numberOfFolds, fold);
			
			pred = addFold2Prediction(testSVMModel(trainSVMModel(trainKernel, trainTarget, params), testKernel), pred, numberOfFolds, fold);
		}		
		return pred;
	}
	
	
	/**
	 * Convenience method to perform the most common type of cross-validation experiment. Directly takes an array of C's to optimize over and directly
	 * outputs the predicted labels as doubles
	 * 
	 * @param kernel
	 * @param target
	 * @param numberOfFolds
	 * @param c
	 * @return
	 */
	public static double[] crossValidate(double[][] kernel, double[] target, int numberOfFolds, double[] c) {
		return extractLabels(crossValidate(kernel, target, new LibSVMParameters(c), numberOfFolds));
	}
	
	
	/**
	 * Convert a list of String labels to an array of doubles
	 * 
	 * @param labels
	 * @return
	 */
	public static double[] createTargets(List<String> labels) {
		Map<String, Integer> labelMap = new TreeMap<String, Integer>();
		return createTargets(labels, labelMap);
	}
	
	
	/**
	 * Convert a list of String labels to an array of doubles, given a Map between strings and integers.
	 * This function is useful if you want to test on a separate test set (i.e. no cross-validation).
	 * 
	 * @param labels
	 * @param labelMap
	 * @return
	 */
	public static double[] createTargets(List<String> labels, Map<String, Integer> labelMap) {
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
	
	/**
	 * Compute the accuracy of a prediction
	 * 
	 * @param target
	 * @param prediction
	 * @return
	 */
	public static double computeAccuracy(double[] target, double[] prediction) {
		double correct = 0;	
		for (int i = 0; i < target.length; i++) {
			if (target[i] == prediction[i]) {
				correct += 1;
			}
		}
		return correct / ((double) target.length);		
	}

	/**
	 * Compute the mean accuracy, i.e. average the accuracy for each class
	 * 
	 * @param target
	 * @param prediction
	 * @return
	 */
	public static double computeMeanAccuracy(double[] target, double[] prediction) {
		Map<Double, Double> targetCounts = computeClassCounts(target);
		double acc = 0, accTemp = 0;
		
		for (double label : targetCounts.keySet()) {
			for (int i = 0; i < prediction.length; i++) {
				if ((prediction[i] == label && target[i] == label) || (prediction[i] != label && target[i] != label)) {
					accTemp += 1;
				}
			}
			acc += (accTemp / target.length);
			accTemp = 0;
		}	
		return acc / ((double) targetCounts.size());
	}
	
	
	/**
	 * Compute the F1 score for a prediction
	 * 
	 * @param target
	 * @param prediction
	 * @return
	 */
	public static double computeF1(double[] target, double[] prediction) {
		Map<Double, Double> targetCounts = computeClassCounts(target);
		double f1 = 0, temp1 = 0, temp2 = 0;
		
		for (double label : targetCounts.keySet()) {
			for (int i = 0; i < prediction.length; i++) {
				if ((prediction[i] == label && target[i] == label)) {
					temp1 += 1;
				}
				if ((prediction[i] == label || target[i] == label)) {
					temp2 += 1;
				}
			}
			f1 += temp1 / temp2;
			temp1 = 0;
			temp2 = 0;
		}	
		return f1 / ((double) targetCounts.size());
		
	}
	
	/**
	 * Compute how many times each class occurs in the target array. Useful if you want to know the distribution of the different labels
	 * 
	 * @param target
	 * @return
	 */
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
	
	/**
	 * Convenience method to extract the labels as a double array from an array of LibSVMPrediction objects
	 * 
	 * @param pred
	 * @return
	 */
	public static double[] extractLabels(LibSVMPrediction[] pred) {
		double[] predLabels = new double[pred.length];
		
		for (int i = 0; i < pred.length; i++) {
			predLabels[i] = pred[i].getLabel();
		}
		return predLabels;
	}
	
	
	// The outlying, highest ranking class is -1
	
	/**
	 * Compute a ranking based on sorting the predictions on decision values. This only works as intended for a binary decision 
	 * problem with the classes +1 and -1. The +1 elements are assumed to be ranking higher.
	 * 
	 * @param pred
	 * @return
	 */
	public static int[] computeRanking(LibSVMPrediction[] pred) {
		Arrays.sort(pred);
		int[] ranking = new int[pred.length];
		
		for (int i = 0; i < ranking.length; i++) {
			ranking[i] = pred[i].getIndex();
		}
				
		return ranking;
	}
	
	/**
	 * Computes the precision at for a ranking and a certain label, i.e. either +1 or -1.
	 * 
	 * @param target
	 * @param ranking
	 * @param at
	 * @param label
	 * @return
	 */
	public static double computePrecisionAt(double[] target, int[] ranking, int at, double label) {
		double precision = 0;
		for (int i = 0; i < at; i++) {
			if (target[ranking[i]] == label) {
				precision += 1;
			}
		}
		return precision / (double) at;
	}
	
	/**
	 * computes the R-Precision for a ranking and a label (+1 or -1)
	 * 
	 * @param target
	 * @param ranking
	 * @param label
	 * @return
	 */
	public static double computeRPrecision(double[] target, int[] ranking, double label) {
		int count = 0;
		for (double t : target) {
			if (t == label) {
				count++;
			}
		}
		
		return computePrecisionAt(target, ranking, count, label);
	}
	
	/**
	 * Computes the average precision for a ranking and label (+1,-1). 
	 * I.e. we compute the precision for each index of the ranking and average this.
	 * 
	 * @param target
	 * @param ranking
	 * @param label
	 * @return
	 */
	public static double computeAveragePrecision(double[] target, int[] ranking, double label) {
		double posClass = 0;
		double map = 0;
		
		for (int i = 0; i < ranking.length; i++) {
			if (target[ranking[i]] == label) {
				posClass++;
				map += posClass / ((double) i + 1);
			}
		}
		
		map /= posClass;
		return map;
	}
	
	
	/**
	 * Compute the NDCG measure, this is a simple variant. Since we use our rankings and labels can still only be (+1,-1)
	 * 
	 * 
	 * @param target
	 * @param ranking
	 * @param p, the position to compute the NDCG at
	 * @param label
	 * @return
	 */
	public static double computeNDCG(double[] target, int[] ranking, int p, double label) {
		double dcg = 0, idcg = 0;
		int count = 0;
		for (double t : target) {
			if (t == label) {
				count++;
			}
		}
		
		for (int i = 0; i < p && i < count; i++) {
			idcg += 1 / (Math.log(i+2) / Math.log(2));
		}
		
		for (int i = 0; i < p; i++) {
			if (label == target[ranking[i]]) {
				dcg += 1 / (Math.log(i+2) / Math.log(2));
			}
		}
		
		return dcg / idcg;		
	}
	
	
	
	
	
	/***********************************************
	* Privates									   * 											
	***********************************************/
	
	
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
	
	private static LibSVMPrediction[] addFold2Prediction(LibSVMPrediction[] foldPred, LibSVMPrediction[] pred, int numberOfFolds, int fold) {
		int foldStart = Math.round((pred.length / ((float) numberOfFolds)) * ((float) fold - 1));
		int foldEnd   = Math.round((pred.length / ((float) numberOfFolds)) * ((float) fold));
		
		for (int i = foldStart; i < foldEnd; i++) {
			pred[i] = foldPred[i - foldStart];
		}
		return pred;
	}
	
	
	private static void setNoOutput() {
		// New print interface, print nothing, i.e. unverbose
		svm.svm_set_print_string_function(new svm_print_interface()
		{
			public void print(String s)
			{
				; 
			}
		}
		);
	}
	
}
