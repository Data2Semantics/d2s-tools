package org.data2semantics.tools.libsvm;

public class LibSVMModel {
	private svm_model model;
	
	LibSVMModel(svm_model model) {
		this.model = model;
	}
		
	svm_model getModel() {
		return model;
	}
	
	public double[] getRho() {
		return model.rho;
	}

}
