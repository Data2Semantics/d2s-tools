package org.data2semantics.tools.experiments;

import org.data2semantics.tools.kernels.GraphKernel;
import org.data2semantics.tools.libsvm.LibSVMWrapper;

import cern.colt.Arrays;


public class ClassificationExperiment implements Runnable {
	private GraphClassificationDataSet dataSet;
	private GraphKernel kernel;
	private long[] seeds;


	public ClassificationExperiment(GraphClassificationDataSet dataSet, GraphKernel kernel, long[] seeds) {
		this.dataSet = dataSet;
		this.kernel = kernel;
		this.seeds = seeds;
	}


	public void run() {			
		double acc = 0;
		
		kernel.compute();
		kernel.normalize();

		for (int i = 0; i < seeds.length; i++) {
			kernel.shuffle(seeds[i]);
			dataSet.shuffle(seeds[i]);
			
			double[][] matrix = kernel.getKernel();
			double[] target = LibSVMWrapper.createTargets(dataSet.getLabels());	

			double[] cs = {0.01, 0.1, 1, 10, 100};	
			double[] prediction = LibSVMWrapper.crossValidate(matrix, target, 10, cs);
			
			acc += LibSVMWrapper.computeAccuracy(target, prediction);
		}
		
		System.out.println(dataSet.getLabel());
		System.out.println(kernel.getLabel() + " Seeds: " + Arrays.toString(seeds));
		System.out.println("Overall Accuracy:  " + acc / seeds.length);
		System.out.println("");

	}


}
