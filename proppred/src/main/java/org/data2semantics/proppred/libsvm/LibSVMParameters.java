package org.data2semantics.proppred.libsvm;

/**
 * Wrapper class around the LibSVM svm_parameter object.
 * We allow the choice of all the SVM algorithms implemented in libsvm.
 * However, we only use the precomputed kernel setting and do not support the probability estimates setting.
 * 
 * 
 * @author Gerben
 *
 */
public class LibSVMParameters {
	public static final int ONE_CLASS = svm_parameter.ONE_CLASS;
	public static final int C_SVC = svm_parameter.C_SVC;
	public static final int NU_SVC = svm_parameter.NU_SVC;
	public static final int EPSILON_SVR = svm_parameter.EPSILON_SVR;
	public static final int NU_SVR = svm_parameter.NU_SVR;	

	public static final int ACCURACY = 1;
	public static final int F1 = 2;
	public static final int MSE = 3;
	public static final int MAE = 4;
	
	
	private svm_parameter params;
	private double[] itParams;
	private int evalFunction;
	private boolean verbose;

	public LibSVMParameters(int algorithm, double[] itParams) {
		this(algorithm);
		this.itParams = itParams;
	}
	
	public LibSVMParameters(int algorithm) {
		params = new svm_parameter();
		params.svm_type = algorithm;
		
		// Fixed parameters
		params.kernel_type = svm_parameter.PRECOMPUTED;
		params.eps = 0.0001;
		params.shrinking = 0;
		params.probability = 0;
		params.cache_size = 300;
		
		// Weights, SVR epsilon, verbosity, evalFunction can be changed afterwards via setters
		params.nr_weight = 0;
		params.p = 0.1;
		evalFunction = ACCURACY;
		verbose = false;	

	}

	svm_parameter getParams() {
		return params;
	}

	public void setItParams(double[] itParams) {
		this.itParams = itParams;
	}

	public double[] getItParams() {
		return itParams;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	public boolean isVerbose() {
		return verbose;
	}

	public void setP(double p) {
		this.params.p = p;
	}	
	
	public void setWeightLabels(int[] labels) {
		params.weight_label = labels;
		params.nr_weight = labels.length;
	}

	public void setWeights(double[] weight) {
		params.weight = weight;
		params.nr_weight = weight.length;
	}
	
	public void setEvalFunction(int evalFunction) {
		this.evalFunction = evalFunction;
	}
	
	public int getEvalFunction() {
		return evalFunction;
	}
}
