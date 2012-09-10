package org.data2semantics.tools.experiments;

import java.io.OutputStream;
import java.io.PrintWriter;

import org.data2semantics.tools.graphs.DirectedMultigraphWithRoot;
import org.data2semantics.tools.graphs.Edge;
import org.data2semantics.tools.graphs.Vertex;
import org.data2semantics.tools.kernels.GraphKernel;
import org.data2semantics.tools.libsvm.LibSVM;

import cern.colt.Arrays;
import edu.uci.ics.jung.graph.DirectedGraph;


public class PropertyPredictionExperiment implements Runnable {
	private PropertyPredictionDataSet dataSet;
	private GraphKernel kernel;
	private long[] seeds;
	private double[] cs;
	private double accuracy;
	private double f1;
	private PrintWriter output;
	private ExperimentResults results;
	
	public PropertyPredictionExperiment(PropertyPredictionDataSet dataSet, GraphKernel kernel, long[] seeds, double[] cs) {
		this(dataSet, kernel, seeds,  cs, System.out);
	}
	
	
	public PropertyPredictionExperiment(PropertyPredictionDataSet dataSet, GraphKernel kernel, long[] seeds, double[] cs, OutputStream outputStream) {
		this.dataSet = dataSet;
		this.kernel = kernel;
		this.seeds = seeds;
		this.cs = cs;
		output = new PrintWriter(outputStream);
		results = new ExperimentResults();
	}


	public void run() {			
		double acc = 0, meanAcc = 0, f = 0;
		
		double[][] matrix = kernel.compute(dataSet.getGraphs());
		

		for (int i = 0; i < seeds.length; i++) {
			matrix = kernel.shuffle(matrix, seeds[i]);
			dataSet.shuffle(seeds[i]);

			double[] target = LibSVM.createTargets(dataSet.getLabels());	
			double[] prediction = LibSVM.crossValidate(matrix, target, 10, cs);
			
			acc += LibSVM.computeAccuracy(target, prediction);
			f +=  LibSVM.computeF1(target, prediction);
		}
		
		accuracy = acc / seeds.length;
		f1 = f / seeds.length;
		
		output.println(dataSet.getLabel());
		output.println(kernel.getLabel() + ", Seeds=" + Arrays.toString(seeds) + ", C=" + Arrays.toString(cs));
		output.print("Overall Accuracy: " + accuracy);
		output.print(", Average F1: " + f1);
		output.println("");
		output.flush();
		
		results.setLabel(dataSet.getLabel() + ", Seeds=" + Arrays.toString(seeds) + ", C=" + Arrays.toString(cs) + ", " + kernel.getLabel() );
		results.setAccuracy(accuracy);
		results.setF1(f1);

	}

	public double getAccuracy() {
		return accuracy;
	}

	public double getF1() {
		return f1;
	}

	public ExperimentResults getResults() {
		return results;
	}	
}
