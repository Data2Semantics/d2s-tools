package org.data2semantics.tools.experiments;

import org.data2semantics.tools.kernels.GraphKernel;
import org.data2semantics.tools.libsvm.LibSVMWrapper;

import cern.colt.Arrays;


public class ClassificationExperiment implements Runnable {
	private GraphClassificationDataSet dataSet;
	private GraphKernel kernel;
	private long[] seeds;
	private double accuracy;
	private double meanAccuracy;
	private double f1;

	public ClassificationExperiment(GraphClassificationDataSet dataSet, GraphKernel kernel, long[] seeds) {
		this.dataSet = dataSet;
		this.kernel = kernel;
		this.seeds = seeds;
	}


	public void run() {			
		double acc = 0, meanAcc = 0, f = 0;
		
		kernel.compute();
		kernel.normalize();
		
		//double[] cs = {0.01, 0.1, 1, 10, 100};	
		double[] cs = {1};
		

		for (int i = 0; i < seeds.length; i++) {
			kernel.shuffle(seeds[i]);
			dataSet.shuffle(seeds[i]);
			
			double[][] matrix = kernel.getKernel();
			double[] target = LibSVMWrapper.createTargets(dataSet.getLabels());	
			double[] prediction = LibSVMWrapper.crossValidate(matrix, target, 10, cs);
			
			acc += LibSVMWrapper.computeAccuracy(target, prediction);
			meanAcc += LibSVMWrapper.computeMeanAccuracy(target, prediction);
			f +=  LibSVMWrapper.computeF1(target, prediction);
		}
		
		accuracy = acc / seeds.length;
		meanAccuracy = meanAcc / seeds.length;
		f1 = f / seeds.length;
		
		System.out.println(dataSet.getLabel());
		System.out.println(kernel.getLabel() + ", Seeds: " + Arrays.toString(seeds) + ", C chosen from: " + Arrays.toString(cs));
		System.out.println("Overall Accuracy:  " + accuracy);
		System.out.println("Mean Accuracy:     " + meanAccuracy);
		System.out.println("F1:                " + f1);
		
		System.out.println("");

	}


	public double getAccuracy() {
		return accuracy;
	}
	
	public double getMeanAccuracy() {
		return meanAccuracy;
	}

	public double getF1() {
		return f1;
	}
}
