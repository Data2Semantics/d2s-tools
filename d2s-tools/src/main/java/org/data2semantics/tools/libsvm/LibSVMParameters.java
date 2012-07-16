package org.data2semantics.tools.libsvm;

public class LibSVMParameters {
	private svm_parameter params;
	private double[] cs;
	private double[] nus;
	private boolean verbose;
		
	public LibSVMParameters(double[] optValues) {
		this();
		this.cs = optValues;
		this.nus = optValues;
	}	
	
	public LibSVMParameters() {
		params = new svm_parameter();
		params.kernel_type = params.PRECOMPUTED;
		params.svm_type = params.C_SVC;
		params.nr_weight = 0;
		params.eps = 0.0001;
		params.shrinking = 1;
		params.probability = 0;
		verbose = false;
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
	
}
