package org.data2semantics.exp.experiments;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import org.data2semantics.proppred.kernels.GraphKernel;
import org.data2semantics.proppred.libsvm.LibSVM;
import org.data2semantics.proppred.libsvm.LibSVMModel;
import org.data2semantics.proppred.libsvm.LibSVMParameters;
import org.data2semantics.proppred.libsvm.Prediction;
import org.data2semantics.tools.graphs.DirectedMultigraphWithRoot;
import org.data2semantics.tools.graphs.Edge;
import org.data2semantics.tools.graphs.Vertex;

import cern.colt.Arrays;
import edu.uci.ics.jung.graph.util.Pair;

public class LinkPredictionExperiment implements Runnable {
	private LinkPredictionDataSet dataSet;
	private GraphKernel kernelA, kernelB;
	private double weightA, weightB;
	private List<DirectedMultigraphWithRoot<Vertex<String>,Edge<String>>> trainGraphsA;
	private List<DirectedMultigraphWithRoot<Vertex<String>,Edge<String>>> trainGraphsB;
	private List<DirectedMultigraphWithRoot<Vertex<String>,Edge<String>>> testGraphsA;
	private List<DirectedMultigraphWithRoot<Vertex<String>,Edge<String>>> testGraphsB;
	private List<Pair<DirectedMultigraphWithRoot<Vertex<String>,Edge<String>>>> trainSet;
	private List<Pair<DirectedMultigraphWithRoot<Vertex<String>,Edge<String>>>> testSet;
	private long[] seeds;
	private double[] cs;
	private PrintWriter output;
	private ExperimentResults results;
	private int maxClassSize;

	

	public LinkPredictionExperiment(LinkPredictionDataSet dataSet,
			GraphKernel kernelA, GraphKernel kernelB, double weightA, double weightB, long[] seeds, 
			double[] cs) {
		this(dataSet, kernelA, kernelB, weightA, weightB, seeds, cs, 50, System.out);
	}	
	
	public LinkPredictionExperiment(LinkPredictionDataSet dataSet,
			GraphKernel kernelA, GraphKernel kernelB, double weightA, double weightB, long[] seeds, 
			double[] cs, OutputStream outputStream) {
		this(dataSet, kernelA, kernelB, weightA, weightB, seeds, cs, 50, outputStream);
	}	
	
	public LinkPredictionExperiment(LinkPredictionDataSet dataSet,
			GraphKernel kernelA, GraphKernel kernelB, double weightA, double weightB, long[] seeds, 
			double[] cs, int maxClassSize) {
		this(dataSet, kernelA, kernelB, weightA, weightB, seeds, cs, maxClassSize, System.out);
	}

	public LinkPredictionExperiment(LinkPredictionDataSet dataSet,
			GraphKernel kernelA, GraphKernel kernelB, double weightA, double weightB, long[] seeds, 
			double[] cs, int maxClassSize, OutputStream outputStream) {
		this.dataSet = dataSet;
		this.kernelA = kernelA;
		this.kernelB = kernelB;
		this.weightA = weightA;
		this.weightB = weightB;
		this.seeds = seeds;
		this.maxClassSize = maxClassSize;
		this.cs = cs;
		output = new PrintWriter(outputStream);
		results = new ExperimentResults();
		results.setAccuracy(new Result());
		results.setF1(new Result());
		results.setAveragePrecision(new Result());
		results.setrPrecision(new Result());
		results.setNdcg(new Result());
	}

	
	public ExperimentResults getResults() {
		return results;
	}	

	public void run() {
		double acc = 0, f = 0;
		double p5 = 0, p10 = 0, p20 = 0;
		double map = 0, rPrec = 0;
		double ndcg = 0;
		
		double[] accScores = new double[seeds.length];
		double[] fScores = new double[seeds.length];
		double[] mapScores = new double[seeds.length];
		double[] rPrecScores = new double[seeds.length];
		double[] ndcgScores = new double[seeds.length];
		
		List<String> labels;
		
		for (int i = 0; i < seeds.length; i++) {
			createRandomSubSet(maxClassSize, maxClassSize, seeds[i], true);
			
			double[][] matrixA = kernelA.compute(trainGraphsA);
			double[][] matrixB = kernelB.compute(trainGraphsB);
			
			double[][] testMatrixA = kernelA.compute(trainGraphsA, testGraphsA);
			double[][] testMatrixB = kernelB.compute(trainGraphsB, testGraphsB);
					
			double[][] matrix = combineTrainKernels(matrixA, matrixB);
			
			// Shuffle the trainSet, else it is ordered too much
			Collections.shuffle(trainSet, new Random(seeds[i]));
			
			labels = new ArrayList<String>();
			for (Pair<DirectedMultigraphWithRoot<Vertex<String>, Edge<String>>> pair : trainSet) {
				if (dataSet.getLabels().get(pair)) {
					labels.add("true");
				} else {
					labels.add("false");
				}
			}
			
			Map<String, Integer> labelMap = new TreeMap<String, Integer>();
			labelMap.put("true", -1);
			labelMap.put("false", 1);
			
			LibSVMParameters param = new LibSVMParameters(LibSVMParameters.NU_SVC, cs);
			//param.setVerbose(true);
			int[] weightLabels = {-1, 1};
			double[] weights = {1,1};
			param.setWeightLabels(weightLabels);
			param.setWeights(weights);
				
			LibSVMModel model = LibSVM.trainSVMModel(matrix, LibSVM.createTargets(labels, labelMap), param);
			
			double[][] testMatrix = combineTestKernels(testMatrixA, testMatrixB);
			//double[][] testMatrix = matrix;	
			
			labels = new ArrayList<String>();
			for (Pair<DirectedMultigraphWithRoot<Vertex<String>, Edge<String>>> pair : testSet) {
				if (dataSet.getLabels().get(pair)) {
					labels.add("true");
				} else {
					labels.add("false");
				}
			}
			
					
			Prediction[] pred = LibSVM.testSVMModel(model, testMatrix);	
						
			double[] target = LibSVM.createTargets(labels, labelMap);
			
			/*
			int[] ranking = LibSVM.computeRanking(pred);
			for (int j = 0; j < 20; j++) {
				System.out.print(ranking[j] + "->" + target[ranking[j]] + ", ");
			}
			System.out.println("");
			System.out.println(LibSVM.computeClassCounts(target));
			System.out.println(Arrays.toString(pred));
			*/
			
			accScores[i] = LibSVM.computeAccuracy(target, LibSVM.extractLabels(pred));
			fScores[i]   = LibSVM.computeF1(target, LibSVM.extractLabels(pred));
			p5   += LibSVM.computePrecisionAt(target, LibSVM.computeRanking(pred), 5, -1);
			p10  += LibSVM.computePrecisionAt(target, LibSVM.computeRanking(pred), 10, -1);
			p20  += LibSVM.computePrecisionAt(target, LibSVM.computeRanking(pred), 20, -1);	
			mapScores[i] = LibSVM.computeAveragePrecision(target, LibSVM.computeRanking(pred), -1);
			rPrecScores[i] = LibSVM.computeRPrecision(target, LibSVM.computeRanking(pred), -1);
			ndcgScores[i] = LibSVM.computeNDCG(target, LibSVM.computeRanking(pred), target.length, -1);
		}
		
		acc = acc / seeds.length;
		f = f / seeds.length;
		p5 = p5 / seeds.length;
		p10 = p10 / seeds.length;
		p20 = p20 / seeds.length;
		map = map / seeds.length;
		rPrec = rPrec / seeds.length;
		ndcg = ndcg / seeds.length;
		
		
		results.setLabel(dataSet.getLabel() + ", Seeds=" + Arrays.toString(seeds) + ", C=" + Arrays.toString(cs) + ", " + kernelA.getLabel() + ", " + kernelB.getLabel());
		results.getAccuracy().setLabel("acc");
		results.getAccuracy().setScores(accScores);
		results.getF1().setLabel("f1");
		results.getF1().setScores(fScores);
		results.getAveragePrecision().setLabel("map");
		results.getAveragePrecision().setScores(mapScores);
		results.getrPrecision().setLabel("Rpr");
		results.getrPrecision().setScores(rPrecScores);
		results.getNdcg().setLabel("ndcg");
		results.getNdcg().setScores(ndcgScores);

		output.println(dataSet.getLabel());
		output.println(kernelA.getLabel() + " " + weightA + " AND " + kernelB.getLabel() + " " + weightB + ", Seeds=" + Arrays.toString(seeds) + ", C=" + Arrays.toString(cs));
		output.print("Overall Accuracy: " + acc);
		output.print(", Average F1: " + f);
		output.print(", Average P5: " + p5);
		output.print(", Average P10: " + p10);
		output.print(", Average P20: " + p20);
		output.print(", Average AP: " + map);
		output.print(", Average R-prec: " + rPrec);
		output.print(", Average NDCG: " + ndcg);
		output.println("");
		output.print("All acc: " + Arrays.toString(accScores));
		output.print(", All f1: " + Arrays.toString(fScores));
		output.print(", All map: " + Arrays.toString(mapScores));
		output.print(", All Rpr: " + Arrays.toString(rPrecScores));
		output.print(", All ndcg: " + Arrays.toString(ndcgScores));
		output.println("");
		output.flush();
	}
	
	
	private double[][] combineTrainKernels(double[][] matrixA, double[][] matrixB) {
		double[][] matrix = new double[trainSet.size()][trainSet.size()];
		Pair<DirectedMultigraphWithRoot<Vertex<String>, Edge<String>>> pairA, pairB;
		
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
		Pair<DirectedMultigraphWithRoot<Vertex<String>, Edge<String>>> pairA, pairB;
		
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
	
	private void createRandomSubSet(int trainSetSize, int testSetSize, long seed, boolean equalSize) {
		List<Pair<DirectedMultigraphWithRoot<Vertex<String>,Edge<String>>>> allPairs = new ArrayList<Pair<DirectedMultigraphWithRoot<Vertex<String>,Edge<String>>>>(dataSet.getLabels().keySet());
		trainGraphsA = new ArrayList<DirectedMultigraphWithRoot<Vertex<String>,Edge<String>>>();
		trainGraphsB = new ArrayList<DirectedMultigraphWithRoot<Vertex<String>,Edge<String>>>();
		testGraphsA = new ArrayList<DirectedMultigraphWithRoot<Vertex<String>,Edge<String>>>();
		testGraphsB = new ArrayList<DirectedMultigraphWithRoot<Vertex<String>,Edge<String>>>();
		trainSet = new ArrayList<Pair<DirectedMultigraphWithRoot<Vertex<String>,Edge<String>>>>();
		testSet = new ArrayList<Pair<DirectedMultigraphWithRoot<Vertex<String>,Edge<String>>>>();
		
		
		Collections.shuffle(allPairs, new Random(seed));
		boolean classLabel = false;
		int posClass = 0, testPosClass = 0;
		int negClass = 0, testNegClass = 0;
		
		int totalPos = 0;
		for (Pair<DirectedMultigraphWithRoot<Vertex<String>,Edge<String>>> pair : allPairs) {
			if (dataSet.getLabels().get(pair)) {
				totalPos++;
			}
		}
		
		long trainPosSize, trainNegSize;
		if (!equalSize) {
			trainPosSize = Math.round(((double) totalPos / (double) allPairs.size()) * ((double) trainSetSize));
			trainNegSize = Math.round(((double) (allPairs.size() - totalPos) / (double) allPairs.size()) * ((double) trainSetSize));	
		} else {
			trainPosSize = trainSetSize / 2;
			trainNegSize = trainSetSize / 2;
		}
		
		
		long testPosSize = Math.round(((double) totalPos / (double) allPairs.size()) * ((double) testSetSize));
		long testNegSize = Math.round(((double) (allPairs.size() - totalPos) / (double) allPairs.size()) * ((double) testSetSize));	
		
		for (Pair<DirectedMultigraphWithRoot<Vertex<String>,Edge<String>>> pair : allPairs) {
			classLabel = dataSet.getLabels().get(pair);
			
			if (classLabel) {
				if (posClass < trainPosSize) {
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
				if (negClass < trainNegSize) {
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
				
				if (posClass == trainPosSize && negClass == trainNegSize && testPosClass == testPosSize && testNegClass == testNegSize) {
					break;
				}
				
			}
		}
		


	}

}
