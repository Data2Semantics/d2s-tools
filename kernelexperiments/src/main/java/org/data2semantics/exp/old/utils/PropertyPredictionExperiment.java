package org.data2semantics.exp.old.utils;

import java.io.OutputStream;
import java.io.PrintWriter;

import org.data2semantics.exp.old.utils.datasets.PropertyPredictionDataSet;
import org.data2semantics.exp.utils.Result;
import org.data2semantics.proppred.kernels.KernelUtils;
import org.data2semantics.proppred.kernels.graphkernels.GraphKernel;
import org.data2semantics.proppred.learners.libsvm.LibSVM;
import org.data2semantics.proppred.learners.libsvm.LibSVMParameters;

import cern.colt.Arrays;


public class PropertyPredictionExperiment implements Runnable {
	private PropertyPredictionDataSet dataSet;
	private GraphKernel kernel;
	private long[] seeds;
	private double[] cs;
	private PrintWriter output;
	private ExperimentResults results;
	private int maxClassSize;
	
	
	
	public PropertyPredictionExperiment(PropertyPredictionDataSet dataSet, GraphKernel kernel, long[] seeds, double[] cs) {
		this(dataSet, kernel, seeds,  cs, 0, System.out);
	}
	
	public PropertyPredictionExperiment(PropertyPredictionDataSet dataSet, GraphKernel kernel, long[] seeds, double[] cs, int maxClassSize) {
		this(dataSet, kernel, seeds,  cs, maxClassSize, System.out);
	}
			
	public PropertyPredictionExperiment(PropertyPredictionDataSet dataSet, GraphKernel kernel, long[] seeds, double[] cs, OutputStream outputStream) {
		this(dataSet, kernel, seeds,  cs, 0, outputStream);
	}
	
	public PropertyPredictionExperiment(PropertyPredictionDataSet dataSet, GraphKernel kernel, long[] seeds, double[] cs, int maxClassSize, OutputStream outputStream) {
		this.dataSet = dataSet;
		this.kernel = kernel;
		this.seeds = seeds;
		this.cs = cs;
		this.maxClassSize = maxClassSize;
		output = new PrintWriter(outputStream);
		results = new ExperimentResults();
		results.setAccuracy(new Result());
		results.setF1(new Result());
	}


	public void run() {			
		
		double[] accScores = new double[seeds.length];
		double[] fScores = new double[seeds.length];		
		double[][] matrix = new double[1][1];	
		double[] target;
		
		if (maxClassSize == 0) {
			matrix = kernel.compute(dataSet.getGraphs());		
		}

		for (int i = 0; i < seeds.length; i++) {
			if (maxClassSize == 0) {
				matrix = KernelUtils.shuffle(matrix, seeds[i]);
				dataSet.shuffle(seeds[i]);
				target = LibSVM.createTargets(dataSet.getLabels());
			} else {
				PropertyPredictionDataSet subset = dataSet.getSubSet(maxClassSize, seeds[i]);
				// shuffle the subset, since the creation of the subset might result in a too ordered dataset
				subset.shuffle(seeds[i]);
				matrix = kernel.compute(subset.getGraphs());
				target = LibSVM.createTargets(subset.getLabels());
			}			
				
			LibSVMParameters params = new LibSVMParameters(LibSVMParameters.C_SVC, cs);
			double[] prediction = LibSVM.extractLabels(LibSVM.crossValidate(matrix, target, params, 10));
			
			accScores[i] = LibSVM.computeAccuracy(target, prediction);
			fScores[i]   = LibSVM.computeF1(target, prediction);		
		}
		
		Result accRes = results.getAccuracy();
		Result fRes   = results.getF1();
		accRes.setLabel("Accuracy");
		fRes.setLabel("F1");
		accRes.setScores(accScores);
		fRes.setScores(fScores);
		
		output.println(dataSet.getLabel());
		output.println(kernel.getLabel() + ", Seeds=" + Arrays.toString(seeds) + ", C=" + Arrays.toString(cs));
		output.print("Overall Accuracy: " + accRes.getScore());
		output.print(", Average F1: " + fRes.getScore());
		output.println("");
		output.print("All acc: " + Arrays.toString(accScores));
		output.print(", All f1: " + Arrays.toString(fScores));
		output.println("");
		
		output.flush();
		
		results.setLabel(dataSet.getLabel() + ", Seeds=" + Arrays.toString(seeds) + ", C=" + Arrays.toString(cs) + ", " + kernel.getLabel() );
		results.setAccuracy(accRes);
		results.setF1(fRes);
	}


	public ExperimentResults getResults() {
		return results;
	}	
}
