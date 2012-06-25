package org.data2semantics.tools.experiments;

import org.data2semantics.tools.kernels.GraphKernel;
import org.data2semantics.tools.libsvm.LibSVMWrapper;


public class ClassificationExperiment implements Runnable {
	private GraphClassificationDataSet dataSet;
	private GraphKernel kernel;
	
	
	public ClassificationExperiment(GraphClassificationDataSet dataSet, GraphKernel kernel) {
		this.dataSet = dataSet;
		this.kernel = kernel;
	}
	
	
	public void run() {			
		kernel.compute();
		kernel.normalize();
		
		double[][] matrix = kernel.getKernel();
		double[] target = LibSVMWrapper.createTargets(dataSet.getLabels());	
		
		double[] cs = {0.01, 0.1, 1, 10, 100};	
		double[] prediction = LibSVMWrapper.crossValidate(matrix, target, 10, cs);
		System.out.println(dataSet.getLabel());
		System.out.println(kernel.getLabel());
		
		System.out.println("Overall Accuracy:  " + LibSVMWrapper.computeAccuracy(target, prediction));
		System.out.println("Mean Accuracy:     " + LibSVMWrapper.computeMeanAccuracy(target, prediction));
		System.out.println("Target Counts:     " + LibSVMWrapper.computeClassCounts(target));
		System.out.println("Prediction Counts: " + LibSVMWrapper.computeClassCounts(prediction));
		System.out.println("");
		
	}
	
	
}
