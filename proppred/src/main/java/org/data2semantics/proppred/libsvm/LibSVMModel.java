package org.data2semantics.proppred.libsvm;

/**
 * Very simple wrapper for the svm_model class, this just stores the model.
 * Contents are only accessible within the libsvm package.
 * 
 * @author Gerben
 *
 */
public class LibSVMModel {
	private svm_model model;
	
	LibSVMModel(svm_model model) {
		this.model = model;
	}
		
	svm_model getModel() {
		return model;
	}
	
	/*
	public double[] getRho() {
		return model.rho;
	}
	*/

}
