package org.data2semantics.tools.libsvm;

public class LibSVMParameters {
	private svm_parameter params;
	private double[] cs;
	private double[] nus;
	private boolean verbose;
		
	public LibSVMParameters(double[] optValues, boolean oneClass) {
		this(oneClass);
		this.cs = optValues;
		this.nus = optValues;
	}	
	
	public LibSVMParameters(double[] optValues) {
		this(optValues, false);
	}	
	
	public LibSVMParameters(boolean oneClass) {
		params = new svm_parameter();
		params.kernel_type = params.PRECOMPUTED;
		params.nr_weight = 0;
		params.eps = 0.0001;
		params.shrinking = 0;
		params.probability = 0;
		params.cache_size = 300;
		verbose = false;
		
		if (oneClass) {
			params.svm_type = params.ONE_CLASS;
		} else {
			params.svm_type = params.C_SVC;
		}
	}
	
	svm_parameter getParams() {
		return params;
	}
	
	public double[] getCs() {
		return cs;
	}
	
	public double[] getNus() {
		return nus;
	}
	
	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}
	
	public boolean isVerbose() {
		return verbose;
	}

	public void setOneClass(boolean oneClass) {
		
		if (oneClass) {
			params.svm_type = params.ONE_CLASS;
		} else {
			params.svm_type = params.C_SVC;
		}
	}	
	
	public void setWeightLabels(int[] labels) {
		params.weight_label = labels;
		params.nr_weight = labels.length;
	}
	
	public void setWeights(double[] weight) {
		params.weight = weight;
		params.nr_weight = weight.length;
	}
}
