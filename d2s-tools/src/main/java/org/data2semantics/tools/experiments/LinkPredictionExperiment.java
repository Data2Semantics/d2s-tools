package org.data2semantics.tools.experiments;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.data2semantics.tools.graphs.Edge;
import org.data2semantics.tools.graphs.Vertex;
import org.data2semantics.tools.kernels.GraphKernel;
import org.data2semantics.tools.kernels.IntersectionSubTreeKernel;
import org.data2semantics.tools.libsvm.LibSVM;

import cern.colt.Arrays;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.util.Pair;

public class LinkPredictionExperiment implements Runnable {
	private LinkPredictionDataSet dataSet;
	private GraphKernel kernelA, kernelB;
	private double weightA, weightB;
	private List<DirectedGraph<Vertex<String>,Edge<String>>> graphsA;
	private List<DirectedGraph<Vertex<String>,Edge<String>>> graphsB;
	private List<Vertex<String>> rootVerticesA;
	private List<Vertex<String>> rootVerticesB;
	private List<Pair<DirectedGraph<Vertex<String>,Edge<String>>>> subSet;
	private long[] seeds;
	private double[] cs;
	private double accuracy;
	private double f1;
	private PrintWriter output;
	private ExperimentResults results;


	public LinkPredictionExperiment(LinkPredictionDataSet dataSet,
			GraphKernel kernelA, GraphKernel kernelB, double weightA, double weightB, long[] seeds,
			double[] cs) {
		this(dataSet, kernelA, kernelB, weightA, weightB, seeds, cs, System.out);
	}


	public LinkPredictionExperiment(LinkPredictionDataSet dataSet,
			GraphKernel kernelA, GraphKernel kernelB, double weightA, double weightB, long[] seeds,
			double[] cs, OutputStream outputStream) {
		this.dataSet = dataSet;
		this.kernelA = kernelA;
		this.kernelB = kernelB;
		this.weightA = weightA;
		this.weightB = weightB;
		this.seeds = seeds;
		this.cs = cs;
		output = new PrintWriter(outputStream);
		results = new ExperimentResults();
	}

	
	public ExperimentResults getResults() {
		return results;
	}	

	@Override
	public void run() {
		double acc = 0, f = 0;
		List<String> labels;
		
		for (int i = 0; i < seeds.length; i++) {
			createRandomSubSet(50, seeds[i]);
			
			double[][] matrixA = kernelA.compute(graphsA);
			double[][] matrixB = kernelB.compute(graphsB);
			
			Collections.shuffle(subSet, new Random(seeds[i]));
			labels = new ArrayList<String>();
			for (Pair<DirectedGraph<Vertex<String>, Edge<String>>> pair : subSet) {
				if (dataSet.getLabels().get(pair)) {
					labels.add("true");
				} else {
					labels.add("false");
				}
			}
			double[] target = LibSVM.createTargets(labels);
			double[][] matrix = combineKernels(matrixA, matrixB);
			double[] prediction = LibSVM.crossValidate(matrix, target, 10, cs);
			
			acc += LibSVM.computeAccuracy(target, prediction);
			f   += LibSVM.computeF1(target, prediction);
		}
		
		acc = acc / seeds.length;
		f = f / seeds.length;

		output.println(dataSet.getLabel());
		output.println(kernelA.getLabel() + " " + weightA + " AND " + kernelB.getLabel() + " " + weightB + ", Seeds=" + Arrays.toString(seeds) + ", C=" + Arrays.toString(cs));
		output.print("Overall Accuracy: " + acc);
		output.print(", Average F1: " + f);
		output.println("");
		output.flush();
	}
	
	
	private double[][] combineKernels(double[][] matrixA, double[][] matrixB) {
		double[][] matrix = new double[subSet.size()][subSet.size()];
		Pair<DirectedGraph<Vertex<String>, Edge<String>>> pairA, pairB;
		
		for (int i = 0; i < subSet.size(); i++) {
			pairA = subSet.get(i);
			for (int j = i; j < subSet.size(); j++) {
				pairB = subSet.get(j);
				matrix[i][j] = weightA * matrixA[graphsA.indexOf(pairA.getFirst())][graphsA.indexOf(pairB.getFirst())] +
							   weightB * matrixB[graphsB.indexOf(pairA.getSecond())][graphsB.indexOf(pairB.getSecond())];
				matrix[j][i] = matrix[i][j];
			}
		}
		return matrix;
	}
			
	private void createRandomSubSet(int classSize, long seed) {
		List<Pair<DirectedGraph<Vertex<String>,Edge<String>>>> posClass = new ArrayList<Pair<DirectedGraph<Vertex<String>,Edge<String>>>>();
		List<Pair<DirectedGraph<Vertex<String>,Edge<String>>>> negClass = new ArrayList<Pair<DirectedGraph<Vertex<String>,Edge<String>>>>();
		List<Pair<DirectedGraph<Vertex<String>,Edge<String>>>> allPairs = new ArrayList<Pair<DirectedGraph<Vertex<String>,Edge<String>>>>(dataSet.getLabels().keySet());
		subSet = new ArrayList<Pair<DirectedGraph<Vertex<String>,Edge<String>>>>();
		graphsA = new ArrayList<DirectedGraph<Vertex<String>,Edge<String>>>();
		graphsB = new ArrayList<DirectedGraph<Vertex<String>,Edge<String>>>();
		rootVerticesA = new ArrayList<Vertex<String>>();
		rootVerticesB = new ArrayList<Vertex<String>>();

		Random rand = new Random(seed);
		int index = 0;
		boolean classLabel = false;

		while (posClass.size() <= classSize || negClass.size() <= classSize) {
			index = (int) Math.floor(rand.nextDouble() * allPairs.size());
			classLabel = dataSet.getLabels().get(allPairs.get(index));
			
			if (classLabel && posClass.size() <= classSize && !posClass.contains(allPairs.get(index))) {
				posClass.add(allPairs.get(index));
				if (!graphsA.contains(allPairs.get(index).getFirst())) {
					graphsA.add(allPairs.get(index).getFirst());
					rootVerticesA.add(dataSet.getRootVerticesA().get(dataSet.getGraphsA().indexOf(allPairs.get(index).getFirst())));
					
				}
				if (!graphsB.contains(allPairs.get(index).getSecond())) {
					graphsB.add(allPairs.get(index).getSecond());
					rootVerticesB.add(dataSet.getRootVerticesB().get(dataSet.getGraphsB().indexOf(allPairs.get(index).getSecond())));
				}

			}

			if (!classLabel && negClass.size() <= classSize && !negClass.contains(allPairs.get(index))) {
				negClass.add(allPairs.get(index));
				if (!graphsA.contains(allPairs.get(index).getFirst())) {
					graphsA.add(allPairs.get(index).getFirst());
					rootVerticesA.add(dataSet.getRootVerticesA().get(dataSet.getGraphsA().indexOf(allPairs.get(index).getFirst())));
				}
				if (!graphsB.contains(allPairs.get(index).getSecond())) {
					graphsB.add(allPairs.get(index).getSecond());
					rootVerticesB.add(dataSet.getRootVerticesB().get(dataSet.getGraphsB().indexOf(allPairs.get(index).getSecond())));
				}
			}
		}
		subSet.addAll(posClass);
		subSet.addAll(negClass);
	}

}
