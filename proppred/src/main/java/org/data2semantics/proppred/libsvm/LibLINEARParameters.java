package org.data2semantics.proppred.libsvm;


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
	private int evalFunction;
	private double[] cs;
	private boolean verbose;
	private double bias;
	private int numFolds;
	private float splitFraction;

	public LibLINEARParameters(int algorithm, double[] cs) {
		this(algorithm);
		this.cs = cs;
	}

	public LibLINEARParameters(int algorithm) {
		SolverType solver;
		this.algorithm = algorithm;
		
		switch (algorithm) {
		case SVC_DUAL: 	solver = SolverType.L2R_L2LOSS_SVC_DUAL;
		evalFunction = LibSVM.ACCURACY;
		break;
		case SVC_PRIMAL: solver = SolverType.L2R_L2LOSS_SVC;
		evalFunction = LibSVM.ACCURACY;
		break;
		case SVR_DUAL: solver = SolverType.L2R_L2LOSS_SVR_DUAL;
		evalFunction = LibSVM.MSE;
		break;
		case SVR_PRIMAL: solver = SolverType.L2R_L2LOSS_SVR;
		evalFunction = LibSVM.MSE;
		break;
		case LR_DUAL: solver = SolverType.L2R_LR_DUAL;
		evalFunction = LibSVM.ACCURACY;
		break;
		case LR_PRIMAL: solver = SolverType.L2R_LR;
		evalFunction = LibSVM.ACCURACY;
		break;
		default: solver = SolverType.L2R_L2LOSS_SVC_DUAL;
		evalFunction = LibSVM.ACCURACY;
		break;
		}

		verbose = false;
		bias = -1;
		numFolds = 5;
		splitFraction = 0;
		
		params = new Parameter(solver, 1, 0.1);
	}

	public int getAlgorithm() {
		return algorithm;
	}

	public void setAlgorithm(int algorithm) {
		this.algorithm = algorithm;
	}

	public void setCs(double[] cs) {
		this.cs = cs;
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
		return cs;
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
	

	/**
	 * Set the evaluation function used during the parameter optimization (e.g. C or nu).
	 * This currently use 4 options, defined by constants in the LibSVM class.
	 * By default this is ACCURACY for classification and one-class, and MSE for regression.
	 * 
	 * @param evalFunction, one of the 4 constant values
	 */
	public void setEvalFunction(int evalFunction) {
		this.evalFunction = evalFunction;
	}

	public int getEvalFunction() {
		return evalFunction;
	}
	
	public void setWeightLabels(int[] labels) {
		weightLabels = labels;
	}

	public void setWeights(double[] weights) {
		params.setWeights(weights, weightLabels);
	}
}
