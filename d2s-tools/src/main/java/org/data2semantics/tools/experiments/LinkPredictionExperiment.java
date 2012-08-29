package org.data2semantics.tools.experiments;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import org.data2semantics.tools.graphs.Edge;
import org.data2semantics.tools.graphs.Vertex;
import org.data2semantics.tools.kernels.GraphKernel;
import org.data2semantics.tools.kernels.IntersectionSubTreeKernel;
import org.data2semantics.tools.libsvm.LibSVM;
import org.data2semantics.tools.libsvm.LibSVMModel;
import org.data2semantics.tools.libsvm.LibSVMParameters;
import org.data2semantics.tools.libsvm.LibSVMPrediction;

import cern.colt.Arrays;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.util.Pair;

public class LinkPredictionExperiment implements Runnable {
	private LinkPredictionDataSet dataSet;
	private GraphKernel kernelA, kernelB;
	private double weightA, weightB;
	private List<DirectedGraph<Vertex<String>,Edge<String>>> trainGraphsA;
	private List<DirectedGraph<Vertex<String>,Edge<String>>> trainGraphsB;
	private List<DirectedGraph<Vertex<String>,Edge<String>>> testGraphsA;
	private List<DirectedGraph<Vertex<String>,Edge<String>>> testGraphsB;
	private List<Pair<DirectedGraph<Vertex<String>,Edge<String>>>> trainSet;
	private List<Pair<DirectedGraph<Vertex<String>,Edge<String>>>> testSet;
	private long[] seeds;
	private double[] cs;
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
		double p5 = 0, p10 = 0, p20 = 0;
		double map = 0, rPrec = 0;
		List<String> labels;
		
		for (int i = 0; i < seeds.length; i++) {
			createRandomSubSet(200, 200, seeds[i]);
			
			double[][] matrixA = kernelA.compute(trainGraphsA);
			double[][] matrixB = kernelB.compute(trainGraphsB);
			
			double[][] testMatrixA = kernelA.compute(trainGraphsA, testGraphsA);
			double[][] testMatrixB = kernelB.compute(trainGraphsB, testGraphsB);
					
			double[][] matrix = combineTrainKernels(matrixA, matrixB);
			
			Collections.shuffle(trainSet, new Random(seeds[i]));
			
			labels = new ArrayList<String>();
			for (Pair<DirectedGraph<Vertex<String>, Edge<String>>> pair : trainSet) {
				if (dataSet.getLabels().get(pair)) {
					labels.add("true");
				} else {
					labels.add("false");
				}
			}
			
			Map<String, Integer> labelMap = new TreeMap<String, Integer>();
			labelMap.put("true", 1);
			labelMap.put("false", -1);
			
				
			LibSVMModel model = LibSVM.trainSVMModel(matrix, LibSVM.createTargets(labels, labelMap), new LibSVMParameters(cs));
			
			double[][] testMatrix = combineTestKernels(testMatrixA, testMatrixB);
						
			labels = new ArrayList<String>();
			for (Pair<DirectedGraph<Vertex<String>, Edge<String>>> pair : testSet) {
				if (dataSet.getLabels().get(pair)) {
					labels.add("true");
				} else {
					labels.add("false");
				}
			}
			
			LibSVMPrediction[] pred = LibSVM.testSVMModel(model, testMatrix);	
						
			double[] target = LibSVM.createTargets(labels, labelMap);
			
			acc  += LibSVM.computeAccuracy(target, LibSVM.extractLabels(pred));
			f    += LibSVM.computeF1(target, LibSVM.extractLabels(pred));
			p5   += LibSVM.computePrecisionAt(target, LibSVM.computeRanking(pred), 5, 1);
			p10  += LibSVM.computePrecisionAt(target, LibSVM.computeRanking(pred), 10, 1);
			p20  += LibSVM.computePrecisionAt(target, LibSVM.computeRanking(pred), 20, 1);	
			map  += LibSVM.computeAveragePrecision(target, LibSVM.computeRanking(pred), 1);
			rPrec += LibSVM.computeRPrecision(target, LibSVM.computeRanking(pred), 1);		
		}
		
		acc = acc / seeds.length;
		f = f / seeds.length;
		p5 = p5 / seeds.length;
		p10 = p10 / seeds.length;
		p20 = p20 / seeds.length;
		map = map / seeds.length;
		rPrec = rPrec / seeds.length;
		
		
		
		results.setAccuracy(acc);
		results.setF1(f);
		results.setAveragePrecision(map);
		results.setrPrecision(rPrec);

		output.println(dataSet.getLabel());
		output.println(kernelA.getLabel() + " " + weightA + " AND " + kernelB.getLabel() + " " + weightB + ", Seeds=" + Arrays.toString(seeds) + ", C=" + Arrays.toString(cs));
		output.print("Overall Accuracy: " + acc);
		output.print(", Average F1: " + f);
		output.print(", Average P5: " + p5);
		output.print(", Average P10: " + p10);
		output.print(", Average P20: " + p20);
		output.print(", Average AP: " + map);
		output.print(", Average R-prec: " + rPrec);
		output.println("");
		output.flush();
	}
	
	
	private double[][] combineTrainKernels(double[][] matrixA, double[][] matrixB) {
		double[][] matrix = new double[trainSet.size()][trainSet.size()];
		Pair<DirectedGraph<Vertex<String>, Edge<String>>> pairA, pairB;
		
		for (int i = 0; i < trainSet.size(); i++) {
			pairA = trainSet.get(i);
			for (int j = i; j < trainSet.size(); j++) {
				pairB = trainSet.get(j);
				matrix[i][j] = weightA * matrixA[trainGraphsA.indexOf(pairA.getFirst())][trainGraphsA.indexOf(pairB.getFirst())] +
							   weightB * matrixB[trainGraphsB.indexOf(pairA.getSecond())][trainGraphsB.indexOf(pairB.getSecond())];
				matrix[j][i] = matrix[i][j];
			}
		}
		return matrix;
	}
	
	private double[][] combineTestKernels(double[][] matrixA, double[][] matrixB) {
		double[][] matrix = new double[testSet.size()][trainSet.size()];
		Pair<DirectedGraph<Vertex<String>, Edge<String>>> pairA, pairB;
		
		for (int i = 0; i < testSet.size(); i++) {
			pairA = testSet.get(i);
			for (int j = i; j < trainSet.size(); j++) {
				pairB = trainSet.get(j);
				matrix[i][j] = weightA * matrixA[testGraphsA.indexOf(pairA.getFirst())][trainGraphsA.indexOf(pairB.getFirst())] +
							   weightB * matrixB[testGraphsB.indexOf(pairA.getSecond())][trainGraphsB.indexOf(pairB.getSecond())];
			}
		}
		return matrix;
	}
	
	private void createRandomSubSet(int trainSetSize, int testSetSize, long seed) {
		List<Pair<DirectedGraph<Vertex<String>,Edge<String>>>> allPairs = new ArrayList<Pair<DirectedGraph<Vertex<String>,Edge<String>>>>(dataSet.getLabels().keySet());
		trainGraphsA = new ArrayList<DirectedGraph<Vertex<String>,Edge<String>>>();
		trainGraphsB = new ArrayList<DirectedGraph<Vertex<String>,Edge<String>>>();
		testGraphsA = new ArrayList<DirectedGraph<Vertex<String>,Edge<String>>>();
		testGraphsB = new ArrayList<DirectedGraph<Vertex<String>,Edge<String>>>();
		trainSet = new ArrayList<Pair<DirectedGraph<Vertex<String>,Edge<String>>>>();
		testSet = new ArrayList<Pair<DirectedGraph<Vertex<String>,Edge<String>>>>();
		
		
		Collections.shuffle(allPairs, new Random(seed));
		boolean classLabel = false;
		int posClass = 0, testPosClass = 0;
		int negClass = 0, testNegClass = 0;
		
		int totalPos = 0;
		for (Pair<DirectedGraph<Vertex<String>,Edge<String>>> pair : allPairs) {
			if (dataSet.getLabels().get(pair)) {
				totalPos++;
			}
		}
		
		long testPosSize = Math.round(((double) totalPos / (double) allPairs.size()) * ((double) testSetSize));
		long testNegSize = Math.round(((double) (allPairs.size() - totalPos) / (double) allPairs.size()) * ((double) testSetSize));	
		
		for (Pair<DirectedGraph<Vertex<String>,Edge<String>>> pair : allPairs) {
			classLabel = dataSet.getLabels().get(pair);
			
			if (classLabel) {
				if (posClass < trainSetSize / 2) {
					trainSet.add(pair);
					posClass++;				
					if (!trainGraphsA.contains(pair.getFirst())) {
						trainGraphsA.add(pair.getFirst());
					}
					if (!trainGraphsB.contains(pair.getSecond())) {
						trainGraphsB.add(pair.getSecond());
					}
				} else if (testPosClass < testPosSize) {
					testSet.add(pair);
					testPosClass++;
					if (!testGraphsA.contains(pair.getFirst())) {
						testGraphsA.add(pair.getFirst());
					}
					if (!testGraphsB.contains(pair.getSecond())) {
						testGraphsB.add(pair.getSecond());
					}
				} 
			} else {
				if (negClass < trainSetSize / 2) {
					trainSet.add(pair);
					negClass++;	
					if (!trainGraphsA.contains(pair.getFirst())) {
						trainGraphsA.add(pair.getFirst());
					}
					if (!trainGraphsB.contains(pair.getSecond())) {
						trainGraphsB.add(pair.getSecond());
					}
				} else if (testNegClass < testNegSize) {
					testSet.add(pair);
					testNegClass++;
					if (!testGraphsA.contains(pair.getFirst())) {
						testGraphsA.add(pair.getFirst());
					}
					if (!testGraphsB.contains(pair.getSecond())) {
						testGraphsB.add(pair.getSecond());
					}
				}
				
				if (posClass == trainSetSize / 2 && negClass == trainSetSize / 2 && testPosClass == testPosSize && testNegClass == testNegSize) {
					break;
				}
				
			}
		}
		


	}

}
