package org.data2semantics.proppred.libsvm;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.HashMap;


/**
 * Wrapper class for the LibSVM library. This class provides static methods to interface with the libsvm library.
 * Even though this library is Java, it is a very ugly literal translation of the C code. 
 * Currently the library is focused on using precomputed kernels with LibSVM.
 * 
 * @author Gerben
 *
 */
public class LibSVM {
	
	/**
	 * This function trains an SVM using a feature vector of SparseVector's and outputs a LibSVMModel.
	 * Via the params object all the 5 svm types can be trained and either the C or nu parameter
	 * can be optimized over with different performance functions, which can be set in the params object.
	 * The C/nu settings to use during optimization have to be supplied in the params object.
	 * 
	 * @param featureVectors, an array of SparseVector's
	 * @param target, an array of labels, of the same length as featureVectors
	 * @param params, a LibSVMParameters object, supplying the parameters for the SVM, note that this cannot be set to the precomputed kernel setting
	 * @return
	 */
	public static LibSVMModel trainSVMModel(SparseVector[] featureVectors, double[] target, LibSVMParameters params) {
		svm_problem svmProb = createSVMProblem(featureVectors, target);
		return trainSVMModel(svmProb, target, params);
	}
	
	
	/** 
	 * This function trains an SVM using a kernel matrix and outputs a LibSVMModel.
	 * Via the params object all the 5 svm types can be trained and either the C or nu parameter
	 * can be optimized over with different performance functions, which can be set in the params object.
	 * The C/nu settings to use during optimization have to be supplied in the params object.
	 * 
	 * @param kernel, a symmetric kernel matrix
	 * @param target, an array of labels the same length of the height/width of the kernel matrix
	 * @param params, a LibSVMParameters object, supplying the parameters for the SVM, not that this must be set to the precomputed kernel setting
	 * 
	 * @return a trained LibSVMModel
	 *
	 */
	public static LibSVMModel trainSVMModel(double[][] kernel, double[] target, LibSVMParameters params) {
		svm_problem svmProb = createSVMProblem(kernel, target);
		return trainSVMModel(svmProb, target, params);
		
	}
	
	private static LibSVMModel trainSVMModel(svm_problem svmProb, double[] target, LibSVMParameters params) {	
		if (!params.isVerbose()) {
			setNoOutput();
		}
		
		double[] prediction = new double[target.length];
		svm_parameter svmParams = params.getParams();
		
		
		double score = 0, bestScore = 0, bestC = 1;
		
		// Parameter selection
		for (double c : params.getItParams()) {
			if (svmParams.svm_type == LibSVMParameters.C_SVC || svmParams.svm_type == LibSVMParameters.EPSILON_SVR) {
				svmParams.C = c;
			} else {
				svmParams.nu = c;
			}
			svm.svm_cross_validation(svmProb, svmParams, 10, prediction);
			
			if (params.getEvalFunction() == LibSVMParameters.ACCURACY) {
				score = computeAccuracy(target, prediction);
			}
			if (params.getEvalFunction() == LibSVMParameters.F1) {
				score = computeF1(target, prediction);
			}
			if (params.getEvalFunction() == LibSVMParameters.MSE) {
				score = 1 / computeMeanSquaredError(target, prediction);
			}
			if (params.getEvalFunction() == LibSVMParameters.MAE) {
				score = 1 / computeMeanAbsoluteError(target, prediction);
			}
			
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
	 * @param model
	 * @param testVectors
	 * @return
	 */
	public static LibSVMPrediction[] testSVMModel(LibSVMModel model, SparseVector[] testVectors) {
		return testSVMModel(model, createTestProblem(testVectors));
	}
	
	
	/**
	 * Use a trained LibSVMModel to generate a prediction for new instances.
	 * 
	 * @param model, the trained model
	 * @param kernel, a kernel matrix for the test instances, the rows (first index) in this matrix are the test instances and the columns (second index) the train instances
	 * @return An array of LibSVMPrediction's 
	 */
	public static LibSVMPrediction[] testSVMModel(LibSVMModel model, double[][] kernel) {
		return testSVMModel(model, createTestProblem(kernel));
	}
	
	
	private static LibSVMPrediction[] testSVMModel(LibSVMModel model, svm_node[][] testNodes) {
		LibSVMPrediction[] pred = new LibSVMPrediction[testNodes.length];
				
		for (int i = 0 ; i < testNodes.length; i++) {
			double[] decVal = new double[model.getModel().nr_class*(model.getModel().nr_class-1)/2];
			pred[i] = new LibSVMPrediction(svm.svm_predict_values(model.getModel(), testNodes[i], decVal), i);
			pred[i].setDecisionValue(decVal);
		}
		return pred;
	}
	
	/**
	 * Convenience method to do a cross-validation experiment with feature vectors
	 * 
	 * @param featureVectors
	 * @param target
	 * @param params
	 * @param numberOfFolds
	 * @return
	 */
	public static LibSVMPrediction[] crossValidate(SparseVector[] featureVectors, double[] target, LibSVMParameters params,  int numberOfFolds) {
		LibSVMPrediction[] pred = new LibSVMPrediction[target.length];
		
		for (int fold = 1; fold <= numberOfFolds; fold++) {
			SparseVector[] trainFV = createFeatureVectorsTrainFold(featureVectors, numberOfFolds, fold);
			SparseVector[] testFV  = createFeatureVectorsTestFold(featureVectors, numberOfFolds, fold);
			double[] trainTarget  = createTargetTrainFold(target, numberOfFolds, fold);
			
			pred = addFold2Prediction(testSVMModel(trainSVMModel(trainFV, trainTarget, params), testFV), pred, numberOfFolds, fold);
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
	/*
	public static double[] crossValidate(double[][] kernel, double[] target, int numberOfFolds, double[] c) {
		return extractLabels(crossValidate(kernel, target, new LibSVMParameters(c), numberOfFolds));
	}
	*/
	
	/**
	 * Convert a list of label Objects to an array of doubles
	 * 
	 * @param labels
	 * @return
	 */
	public static <L> double[] createTargets(List<L> labels) {
		Map<L, Integer> labelMap = new HashMap<L, Integer>();
		return createTargets(labels, labelMap);
	}
	
	
	/**
	 * Convert a list of label objects to an array of doubles, given a Map between label objects of type L and integers.
	 * This function is useful if you want to test on a separate test set (i.e. no cross-validation).
	 * 
	 * @param labels
	 * @param labelMap
	 * @return
	 */
	public static <L> double[] createTargets(List<L> labels, Map<L, Integer> labelMap) {
		double[] targets = new double[labels.size()];
		int t = 0;
		int i = 0;
		
		for (L label : labels) {
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
	 * This function reverses the labelMap created with createTargets. Useful if we want to transform a prediction back to the original label Objects.
	 * This works because the mapping from objects to labels is bijective.
	 * 
	 * @param labelMap
	 * @return
	 */
	public static <L> Map<Integer, L> reverseLabelMap(Map<L, Integer> labelMap) {
		Map<Integer, L> reverseMap = new HashMap<Integer, L>();
		
		for (L label : labelMap.keySet()) {
			reverseMap.put(labelMap.get(label), label);
		}
		return reverseMap;
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
	 * Compute the F1 score for a prediction. 
	 * It computes the F1 per class and returns the average of these F1's, this is called the macro F1
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
	 * Compute the mean squared error for a prediction, useful for regression
	 * 
	 * @param target
	 * @param prediction
	 * @return
	 */
	public static double computeMeanSquaredError(double[] target, double[] prediction) {
		double error = 0;
		for (int i = 0; i < target.length; i++) {
			error += (target[i] - prediction[i]) * (target[i] - prediction[i]);
		}
		return error / ((double) target.length);
	}
	
	/**
	 * compute the mean absolute error for a prediction, for regression
	 * 
	 * @param target
	 * @param prediction
	 * @return
	 */
	public static double computeMeanAbsoluteError(double[] target, double[] prediction) {
		double error = 0;
		for (int i = 0; i < target.length; i++) {
			error += Math.abs(target[i] - prediction[i]);
		}
		return error / ((double) target.length);
	}
	
	
	/**
	 * Compute how many times each class occurs in the target array. Useful if you want to know the distribution of the different labels
	 * 
	 * @param target
	 * @return
	 */
	public static Map<Double, Double> computeClassCounts(double[] target) {
		Map<Double, Double> counts = new HashMap<Double, Double>();

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
	
	
	private static svm_problem createSVMProblem(SparseVector[] featureVectors, double[] target) {
		svm_problem prob = new svm_problem();
		svm_node[][] nodes = new svm_node[target.length][];
		
		prob.l = target.length;
		prob.y = target;
		prob.x = nodes;
		
		for (int i = 0; i < nodes.length; i++) {
			nodes[i] = featureVectors[i].convert2svmNodes();
		}		
		return prob;		
	}
	
	
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
	
	private static svm_node[][] createTestProblem(SparseVector[] testVectors) {
		svm_node[][] nodes = new svm_node[testVectors.length][];
		
		for (int i = 0; i < testVectors.length; i++) {
			nodes[i] = testVectors[i].convert2svmNodes();
		}
		return nodes;
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
	
	private static SparseVector[] createFeatureVectorsTrainFold(SparseVector[] featureVectors, int numberOfFolds, int fold) {
		int foldStart = Math.round((featureVectors.length / ((float) numberOfFolds)) * ((float) fold - 1));
		int foldEnd   = Math.round((featureVectors.length / ((float) numberOfFolds)) * ((float) fold));
		int foldLength = (foldEnd-foldStart);
		
		SparseVector[] trainFV = new SparseVector[featureVectors.length - foldLength];
		
		for (int i = 0; i < foldStart; i++) {
			trainFV[i] = featureVectors[i];
		}	
		for (int i = foldEnd; i < featureVectors.length; i++) {
			trainFV[i - foldLength] = featureVectors[i];
		}			
		return trainFV;
	}
	
	private static SparseVector[] createFeatureVectorsTestFold(SparseVector[] featureVectors, int numberOfFolds, int fold) {
		int foldStart = Math.round((featureVectors.length / ((float) numberOfFolds)) * ((float) fold - 1));
		int foldEnd   = Math.round((featureVectors.length / ((float) numberOfFolds)) * ((float) fold));
		int foldLength = (foldEnd-foldStart);
		
		SparseVector[] testFV = new SparseVector[foldLength];
		
		for (int i = foldStart; i < foldEnd; i++) {
			testFV[i - foldStart] = featureVectors[i];
		}			
		return testFV;
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
