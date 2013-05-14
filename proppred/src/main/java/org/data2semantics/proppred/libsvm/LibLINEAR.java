package org.data2semantics.proppred.libsvm;

import static org.data2semantics.proppred.libsvm.LibSVM.createFeatureVectorsTrainFold;

import java.io.FileWriter;
import java.util.Set;


import de.bwaldvogel.liblinear.Feature;
import de.bwaldvogel.liblinear.FeatureNode;
import de.bwaldvogel.liblinear.Linear;
import de.bwaldvogel.liblinear.Parameter;
import de.bwaldvogel.liblinear.Problem;

/**
 * Static methods to ease the access to the Java LibLINEAR package, similar to the {@link LibSVM} class.
 * Not all solvers are accessible, only the most common L2 types, i.e. primal/dual, of SVC, SVR and LR.
 * 
 * @author Gerben
 *
 */
public class LibLINEAR {


	public static LibLINEARModel trainLinearModel(SparseVector[] featureVectors, double[] target, LibLINEARParameters params) {
		if (!params.isVerbose()) {
			Linear.disableDebugOutput();
		}
		
		Problem prob = createLinearProblem(featureVectors, target, params.getBias());
		
		double prediction[] = new double[target.length];
		Parameter linearParams = params.getParams();
		
		double score = 0, bestScore = 0, bestC = 1;
		
		for (double c : params.getCs()) {
			linearParams.setC(c);
			
			Linear.crossValidation(prob, linearParams, 5, prediction);
			
			if (params.getEvalFunction() == LibSVM.ACCURACY) {
				score = LibSVM.computeAccuracy(target, prediction);
			}
			if (params.getEvalFunction() == LibSVM.F1) {
				score = LibSVM.computeF1(target, prediction);
			}
			if (params.getEvalFunction() == LibSVM.MSE) {
				score = 1 / LibSVM.computeMeanSquaredError(target, prediction);
			}
			if (params.getEvalFunction() == LibSVM.MAE) {
				score = 1 / LibSVM.computeMeanAbsoluteError(target, prediction);
			}
			
			if (score > bestScore) {
				bestC = c;
				bestScore = score;
			}	
		}
		
		linearParams.setC(bestC);	
		return new LibLINEARModel(Linear.train(prob, linearParams));
	}
	
	public static Prediction[] testLinearModel(LibLINEARModel model, SparseVector[] testVectors) {
		Feature[][] prob = createTestProblem(testVectors, model.getModel().getNrFeature(), model.getModel().getBias());
		
		Prediction[] pred = new Prediction[testVectors.length];		
		for (int i = 0; i < testVectors.length; i++) {
			double[] decVal = new double[(model.getModel().getNrClass() <= 2) ? 1 : model.getModel().getNrClass()];
			pred[i] = new Prediction(Linear.predictValues(model.getModel(), prob[i], decVal), i);
			pred[i].setDecisionValue(decVal);
		}
		return pred;
	}
	
	
	public static Prediction[] crossValidate(SparseVector[] featureVectors, double[] target, LibLINEARParameters params, int numberOfFolds) {
		Prediction[] pred = new Prediction[target.length];
		SparseVector[] trainFV, testFV;
		double[] trainTarget;
		
		for (int fold = 1; fold <= numberOfFolds; fold++) {
			if (featureVectors.length >=  10000) {
				System.out.println("CV fold: " + fold);
			}
			trainFV 	 = LibSVM.createFeatureVectorsTrainFold(featureVectors, numberOfFolds, fold);
			testFV  	 = LibSVM.createFeatureVectorsTestFold(featureVectors, numberOfFolds, fold);
			trainTarget  = LibSVM.createTargetTrainFold(target, numberOfFolds, fold);			
			pred = LibSVM.addFold2Prediction(testLinearModel(trainLinearModel(trainFV, trainTarget, params), testFV), pred, numberOfFolds, fold);
		}		
		return pred;
	}
	
	
	
	
	public static void featureVectors2File(SparseVector[] featureVectors, double[] target, String filename) {
		try {
			FileWriter fo = new FileWriter(filename);
			StringBuffer line;
			
			for (int i = 0; i < featureVectors.length; i++) {
				line = new StringBuffer();
				line.append(target[i]);
				line.append(" ");
				
				for (int index : featureVectors[i].getIndices()) {
					line.append(index);
					line.append(":");
					line.append(featureVectors[i].getValue(index));
					line.append(" ");
				}
				line.append("\n");
				fo.write(line.toString());
			}
			fo.close();
			
		} catch (Exception e) {
			e.getStackTrace();
		}
	}
	
	
	
	private static Feature[][] createTestProblem(SparseVector[] featureVectors, int numberOfFeatures, double bias) {
		Feature[][] nodes = new FeatureNode[featureVectors.length][];
		
		for (int i = 0; i < featureVectors.length; i++) {
			Set<Integer> indices = featureVectors[i].getIndices();
			nodes[i] = new FeatureNode[(bias >= 0) ? indices.size() + 1 : indices.size()];
			
			int j = 0;
			for (int index : indices) {
				nodes[i][j] = new FeatureNode(index, featureVectors[i].getValue(index));
				j++;
			}
			if (bias >= 0) {
				nodes[i][j] = new FeatureNode(numberOfFeatures, bias);
			}
		}	
		return nodes;	
	}
	
	
	private static Problem createLinearProblem(SparseVector[] featureVectors, double[] target, double bias) {
		Problem prob = new Problem();
		prob.y = target;
		prob.x = new FeatureNode[featureVectors.length][];	
		prob.l = featureVectors.length;
		
		int maxIndex = 0;
		for (int i = 0; i < featureVectors.length; i++) {
			Set<Integer> indices = featureVectors[i].getIndices();
			prob.x[i] = new FeatureNode[(bias >= 0) ? indices.size() + 1 : indices.size()];
			int j = 0;
			for (int index : indices) {
				prob.x[i][j] = new FeatureNode(index, featureVectors[i].getValue(index));		
				maxIndex = Math.max(maxIndex, index);
				j++;
			}
		}
		
		if (bias >= 0) {
			maxIndex++;
			for (int i = 0; i < featureVectors.length; i++) {
				prob.x[i][prob.x[i].length - 1] = new FeatureNode(maxIndex, bias);
			}
		}
		
		prob.n    = maxIndex;
		prob.bias = (bias >= 0) ? 1 : -1;
		
		return prob;		
	}

}
