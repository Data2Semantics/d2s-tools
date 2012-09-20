package org.data2semantics.tools.kernels;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.data2semantics.tools.graphs.DirectedMultigraphWithRoot;
import org.data2semantics.tools.graphs.Edge;
import org.data2semantics.tools.graphs.GraphFactory;
import org.data2semantics.tools.graphs.Vertex;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;

// TODO rewrite featureVectors using arrays, and do kernel computation add each step

public class WLSubTreeKernel extends GraphKernel<DirectedMultigraphWithRoot<Vertex<String>, Edge<String>>> {
	//private double[][] featureVectors;	
	//private Map<String, String> labelDict;
	//private int startLabel, currentLabel;
	private int iterations = 2;
	private boolean skipFirst;
	
	
	public WLSubTreeKernel(int iterations, boolean normalize, boolean skipFirst) {
		this(iterations, normalize);
		this.skipFirst = skipFirst;
	}
	
	public WLSubTreeKernel(int iterations, boolean normalize) {
		super(normalize);
		this.iterations = iterations;
		this.label = "WL SubTree Kernel, it=" + iterations;
		skipFirst = false;
		
		//this(new ArrayList<DirectedGraph<Vertex<String>, Edge<String>>>(), iterations);
	}	
	
	public WLSubTreeKernel(int iterations) {
		this(iterations, true);
	}
	
	/*
	public WLSubTreeKernel(List<DirectedGraph<Vertex<String>, Edge<String>>> graphs, int iterations) {
		super(graphs);
		copyGraphs();
		featureVectors = new double[graphs.size()][];
		labelDict = new HashMap<String,String>();
		
		this.label = "WL SubTree Kernel, it=" + iterations;
		startLabel = 0;
		currentLabel = 0;
		this.iterations = iterations;
	}

	*/
	
	public void compute() {
		;
		/*
		// Change original labels to numeric labels and setup the first part of the feature vectors
		compressGraphLabels();
		computeFeatureVectors();
		computeKernelMatrix();
		
		for (int i = 0; i < iterations; i++) {
			relabelGraphs2MultisetLabels();
			compressGraphLabels();
			computeFeatureVectors();
			computeKernelMatrix();
		}
		*/
	}
	

		
	@Override
	public double[][] compute(List<? extends DirectedMultigraphWithRoot<Vertex<String>, Edge<String>>> trainGraphs) {
		
		List<DirectedGraph<Vertex<String>, Edge<String>>> graphs = copyGraphs(trainGraphs);
		double[][] featureVectors = new double[graphs.size()][];
		Map<String, String> labelDict = new HashMap<String,String>();
		double[][] kernel = initMatrix(graphs.size(), graphs.size());
		
		int startLabel = 0;
		int currentLabel = 0;
		
		for (DirectedMultigraphWithRoot<Vertex<String>, Edge<String>> graph : trainGraphs) {
			graph.getRootVertex().setLabel(ROOTID);
		}
		
		currentLabel = compressGraphLabels(graphs, labelDict, currentLabel);
		
		if (!skipFirst) {
			computeFeatureVectors(graphs, featureVectors, startLabel, currentLabel);
			computeKernelMatrix(graphs, featureVectors, kernel, 1);
		}
		
		for (int i = 0; i < this.iterations; i++) {
			relabelGraphs2MultisetLabels(graphs, startLabel, currentLabel);
			startLabel = currentLabel;
			currentLabel = compressGraphLabels(graphs, labelDict, currentLabel);
			computeFeatureVectors(graphs, featureVectors, startLabel, currentLabel);
			computeKernelMatrix(graphs, featureVectors, kernel, i+1);	
		}
		
		if (normalize) {
			return normalize(kernel);
		} else {		
			return kernel;
		}
	}

	@Override
	public double[][] compute(
			List<? extends DirectedMultigraphWithRoot<Vertex<String>, Edge<String>>> trainGraphs,
			List<? extends DirectedMultigraphWithRoot<Vertex<String>, Edge<String>>> testGraphs) {
		
		List<DirectedGraph<Vertex<String>, Edge<String>>> graphs = copyGraphs(testGraphs);
		graphs.addAll(copyGraphs(trainGraphs));
		double[][] featureVectors = new double[graphs.size()][];
		Map<String, String> labelDict = new HashMap<String,String>();
		double[][] kernel = initMatrix(testGraphs.size(), trainGraphs.size());
		double[] ss = new double[testGraphs.size() + trainGraphs.size()];
		
		int startLabel = 0;
		int currentLabel = 0;
		
		for (DirectedMultigraphWithRoot<Vertex<String>, Edge<String>> graph : trainGraphs) {
			graph.getRootVertex().setLabel(ROOTID);
		}
		for (DirectedMultigraphWithRoot<Vertex<String>, Edge<String>> graph : testGraphs) {
			graph.getRootVertex().setLabel(ROOTID);
		}
		
		currentLabel = compressGraphLabels(graphs, labelDict, currentLabel);
		
		if (!skipFirst) {
			computeFeatureVectors(graphs, featureVectors, startLabel, currentLabel);
			computeKernelMatrix(trainGraphs, testGraphs, featureVectors, kernel, ss);
		}
		
		for (int i = 0; i < this.iterations; i++) {
			relabelGraphs2MultisetLabels(graphs, startLabel, currentLabel);
			startLabel = currentLabel;
			currentLabel = compressGraphLabels(graphs, labelDict, currentLabel);
			computeFeatureVectors(graphs, featureVectors, startLabel, currentLabel);
			computeKernelMatrix(trainGraphs, testGraphs, featureVectors, kernel, ss);	
		}
		
		if (normalize) {
			double[] ssTest = Arrays.copyOfRange(ss, 0, testGraphs.size());
			double[] ssTrain = Arrays.copyOfRange(ss, testGraphs.size(), ss.length);			
			return normalize(kernel, ssTrain, ssTest);
			
		} else {		
			return kernel;
		}
	}

	/*
	public void setGraphs(List<DirectedGraph<Vertex<String>, Edge<String>>> graphs) {
		this.graphs = graphs;
		copyGraphs();
		initMatrix();
		featureVectors = new double[graphs.size()][];
		labelDict = new HashMap<String,String>();
	}
	*/
	
	
	private void relabelGraphs2MultisetLabels(List<DirectedGraph<Vertex<String>, Edge<String>>> graphs, int startLabel, int currentLabel) {
		Map<String, Bucket<Vertex<String>>> bucketsV = new HashMap<String, Bucket<Vertex<String>>>();
		Map<String, Bucket<Edge<String>>> bucketsE   = new HashMap<String, Bucket<Edge<String>>>();
		
		// Initialize buckets
		for (int i = startLabel; i < currentLabel; i++) {
			bucketsV.put(Integer.toString(i), new Bucket<Vertex<String>>(Integer.toString(i)));
			bucketsE.put(Integer.toString(i), new Bucket<Edge<String>>(Integer.toString(i)));
		}
		
		// 1. Fill buckets 
		for (DirectedGraph<Vertex<String>, Edge<String>> graph : graphs) {
			// Add each edge source (i.e.) start vertex to the bucket of the edge label
			for (Edge<String> edge : graph.getEdges()) {
				bucketsV.get(edge.getLabel()).getContents().add(graph.getDest(edge));
			}
						
			// Add each incident edge to the bucket of the node label
			for (Vertex<String> vertex : graph.getVertices()) {			
				Collection<Edge<String>> v2 = graph.getOutEdges(vertex);	
				bucketsE.get(vertex.getLabel()).getContents().addAll(v2);
			}	
		}
		
		// 2. add bucket labels to existing labels
		// Change the original label to a prefix label
		for (DirectedGraph<Vertex<String>, Edge<String>> graph : graphs) {
			for (Edge<String> edge : graph.getEdges()) {
				edge.setLabel(edge.getLabel() + "_");
			}
			for (Vertex<String> vertex : graph.getVertices()) {
				vertex.setLabel(vertex.getLabel() + "_");
			}
		}

		for (int i = startLabel; i < currentLabel; i++) {
			// Process vertices
			Bucket<Vertex<String>> bucketV = bucketsV.get(Integer.toString(i));			
			for (Vertex<String> vertex : bucketV.getContents()) {
				vertex.setLabel(vertex.getLabel() + bucketV.getLabel());
			}
			// Process edges
			Bucket<Edge<String>> bucketE = bucketsE.get(Integer.toString(i));			
			for (Edge<String> edge : bucketE.getContents()) {
				edge.setLabel(edge.getLabel() + bucketE.getLabel());
			}
		}
		


	}
	
	private int compressGraphLabels(List<DirectedGraph<Vertex<String>, Edge<String>>> graphs, Map<String, String> labelDict, int currentLabel) {
		String label;
				
		for (DirectedGraph<Vertex<String>, Edge<String>> graph : graphs) {
			
			for (Edge<String> edge : graph.getEdges()) {
				label = labelDict.get(edge.getLabel());						
				if (label == null) {					
					label = Integer.toString(currentLabel);
					currentLabel++;
					labelDict.put(edge.getLabel(), label);				
				}
				edge.setLabel(label);
			}

			for (Vertex<String> vertex : graph.getVertices()) {
				label = labelDict.get(vertex.getLabel());
				if (label == null) {
					label = Integer.toString(currentLabel);
					currentLabel++;
					labelDict.put(vertex.getLabel(), label);
				}
				vertex.setLabel(label);
			}
		}
		return currentLabel;
	}
	
	private void computeFeatureVectors(List<DirectedGraph<Vertex<String>, Edge<String>>> graphs, double[][] featureVectors, int startLabel, int currentLabel) {
		int index;
		for (int i = 0; i < graphs.size(); i++) {
			featureVectors[i] = new double[currentLabel - startLabel];		
			Arrays.fill(featureVectors[i], 0.0);

			// for each vertex, use the label as index into the feature vector and do a + 1,
			for (Vertex<String> vertex : graphs.get(i).getVertices()) {
				index = Integer.parseInt(vertex.getLabel()) - startLabel;				
				featureVectors[i][index] += 1.0;
			}
			
			for (Edge<String> edge : graphs.get(i).getEdges()) {
				index = Integer.parseInt(edge.getLabel()) - startLabel;
				featureVectors[i][index] += 1.0;;
			}
		}
	}

	
	private void computeKernelMatrix(List<DirectedGraph<Vertex<String>, Edge<String>>> graphs, double[][] featureVectors, double[][] kernel, int iteration) {
		for (int i = 0; i < graphs.size(); i++) {
			for (int j = i; j < graphs.size(); j++) {
				kernel[i][j] += dotProduct(featureVectors[i], featureVectors[j]) * ((double) iteration / (double) this.iterations);
				kernel[j][i] = kernel[i][j];
			}
		}
	}
	
	
	private void computeKernelMatrix(List<? extends DirectedGraph<Vertex<String>, Edge<String>>> trainGraphs, List<? extends DirectedGraph<Vertex<String>, Edge<String>>> testGraphs, double[][] featureVectors, double[][] kernel, double[] ss) {
		for (int i = 0; i < testGraphs.size(); i++) {
			for (int j = 0; j < trainGraphs.size(); j++) {
				kernel[i][j] += dotProduct(featureVectors[i], featureVectors[j + testGraphs.size()]); 
			}
		}
		for (int i = 0; i < testGraphs.size() + trainGraphs.size(); i++) {
			ss[i] += dotProduct(featureVectors[i], featureVectors[i]);
		}
		
	}
	
	private double dotProduct(double[] fv1, double[] fv2) {
		double sum = 0.0;		
		for (int i = 0; i < fv1.length && i < fv2.length; i++) {
			sum += fv1[i] * fv2[i];
		}	
		return sum;
	}	
	

	private List<DirectedGraph<Vertex<String>, Edge<String>>> copyGraphs(List<? extends DirectedGraph<Vertex<String>, Edge<String>>> oldGraphs) {
		List<DirectedGraph<Vertex<String>, Edge<String>>> graphs = new ArrayList<DirectedGraph<Vertex<String>, Edge<String>>>();	
		for(DirectedGraph<Vertex<String>, Edge<String>> graph : oldGraphs) {
			graphs.add(GraphFactory.copyDirectedGraph(graph));				
		}
		return graphs;
	}
	
	private class Bucket<T> {
		private String label;
		private List<T> contents;
		
		public Bucket(String label) {
			this.label = label;
			contents = new ArrayList<T>();
		}

		public List<T> getContents() {
			return contents;
		}

		public String getLabel() {
			return label;
		}
	}
}
