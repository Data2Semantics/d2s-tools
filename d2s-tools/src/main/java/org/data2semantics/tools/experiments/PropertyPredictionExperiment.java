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
		results.setAccuracy(new Result());
		results.setF1(new Result());
	}


	public void run() {			
		
		double[] accScores = new double[seeds.length];
		double[] fScores = new double[seeds.length];		
				

		for (int i = 0; i < seeds.length; i++) {
			PropertyPredictionDataSet subset = dataSet.getSubSet(50, seeds[i]);
			
			//matrix = kernel.shuffle(matrix, seeds[i]);
			//dataSet.shuffle(seeds[i]);
		
			double[][] matrix = kernel.compute(subset.getGraphs());
			
			
			double[] target = LibSVM.createTargets(subset.getLabels());	
			double[] prediction = LibSVM.crossValidate(matrix, target, 10, cs);
			
			accScores[i] = LibSVM.computeAccuracy(target, prediction);
			fScores[i]   = LibSVM.computeF1(target, prediction);		
		}
		
		Result accRes = results.getAccuracy();
		Result fRes   = results.getF1();
		accRes.setLabel("acc");
		fRes.setLabel("f1");
		accRes.setScores(accScores);
		fRes.setScores(fScores);
		
		output.println(dataSet.getLabel());
		output.println(kernel.getLabel() + ", Seeds=" + Arrays.toString(seeds) + ", C=" + Arrays.toString(cs));
		output.print("Overall Accuracy: " + accRes.getScore());
		output.print(", Average F1: " + fRes.getScore());
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
