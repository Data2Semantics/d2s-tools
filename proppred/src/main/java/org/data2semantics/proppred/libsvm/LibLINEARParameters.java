package org.data2semantics.proppred.libsvm;


import org.data2semantics.proppred.libsvm.evaluation.Accuracy;
import org.data2semantics.proppred.libsvm.evaluation.EvaluationFunction;
import org.data2semantics.proppred.libsvm.evaluation.MeanSquaredError;

import de.bwaldvogel.liblinear.Parameter;
import de.bwaldvogel.liblinear.SolverType;

public class LibLINEARParameters {
	public static final int SVC_DUAL = 1;
	public static final int SVC_PRIMAL = 2;
	public static final int SVR_DUAL = 3;
	public static final int SVR_PRIMAL = 4;
	public static final int LR_DUAL = 5;
	public static final int LR_PRIMAL = 6;	

	private int[] weightLabels;
	private int algorithm;
	private Parameter params;	
	private double[] itParams;
	private boolean verbose;
	private double bias;
	
	private boolean doCrossValidation;
	private int numFolds;
	private float splitFraction;
	
	private EvaluationFunction evalFunction;
	

	public LibLINEARParameters(int algorithm, double[] itParams) {
		this(algorithm);
		this.itParams = itParams;
	}

	public LibLINEARParameters(int algorithm) {
		SolverType solver;
		this.algorithm = algorithm;
		
		switch (algorithm) {
		case SVC_DUAL: 	solver = SolverType.L2R_L2LOSS_SVC_DUAL;
		evalFunction = new Accuracy();
		break;
		case SVC_PRIMAL: solver = SolverType.L2R_L2LOSS_SVC;
		evalFunction = new Accuracy();
		break;
		case SVR_DUAL: solver = SolverType.L2R_L2LOSS_SVR_DUAL;
		evalFunction = new MeanSquaredError();
		break;
		case SVR_PRIMAL: solver = SolverType.L2R_L2LOSS_SVR;
		evalFunction = new MeanSquaredError();
		break;
		case LR_DUAL: solver = SolverType.L2R_LR_DUAL;
		evalFunction = new Accuracy();
		break;
		case LR_PRIMAL: solver = SolverType.L2R_LR;
		evalFunction = new Accuracy();
		break;
		default: solver = SolverType.L2R_L2LOSS_SVC_DUAL;
		evalFunction = new Accuracy();
		break;
		}

		verbose = false;
		bias = -1;
		doCrossValidation = true;
		numFolds = 5;
		splitFraction = (float) 0.7;
		
		params = new Parameter(solver, 1, 0.1);
	}

	public int getAlgorithm() {
		return algorithm;
	}

	public void setAlgorithm(int algorithm) {
		this.algorithm = algorithm;
	}

	public void setCs(double[] itParams) {
		this.itParams = itParams;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	public void setBias(double bias) {
		this.bias = bias;
	}

	public double getBias() {
		return bias;
	}

	public Parameter getParams() {
		return params;
	}

	public double[] getCs() {
		return itParams;
	}

	public void setP(double p) {
		params.setP(p);
	}

	public void setEps(double eps) {
		params.setEps(eps);
	}
	
	public boolean isVerbose() {
		return verbose;
	}
	
	public int getNumFolds() {
		return numFolds;
	}

	public float getSplitFraction() {
		return splitFraction;
	}

	public void setNumFolds(int numFolds) {
		this.numFolds = numFolds;
	}

	public void setSplitFraction(float splitFraction) {
		this.splitFraction = splitFraction;
	}
	

	public boolean isDoCrossValidation() {
		return doCrossValidation;
	}

	public void setDoCrossValidation(boolean doCrossValidation) {
		this.doCrossValidation = doCrossValidation;
	}

	public void setEvalFunction(EvaluationFunction evalFunc) {
		this.evalFunction = evalFunc;
	}

	public EvaluationFunction getEvalFunction() {
		return evalFunction;
	}
	
	public void setWeightLabels(int[] labels) {
		weightLabels = labels;
	}

	public void setWeights(double[] weights) {
		params.setWeights(weights, weightLabels);
	}
}
