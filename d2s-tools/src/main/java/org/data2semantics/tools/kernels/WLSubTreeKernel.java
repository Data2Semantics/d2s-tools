package org.data2semantics.tools.kernels;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.data2semantics.tools.graphs.Edge;
import org.data2semantics.tools.graphs.Vertex;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;

// TODO rewrite featureVectors using arrays, and do kernel computation add each step

public class WLSubTreeKernel implements GraphKernel {
	private double[][] kernel;
	private List<DirectedGraph<Vertex<String>, Edge<String>>> graphs;
	private double[][] featureVectors;	
	private Map<String, String> labelDict;
	private int startLabel, currentLabel;
	private int iterations = 2;
	
	
	
	public WLSubTreeKernel(List<DirectedGraph<Vertex<String>, Edge<String>>> graphs) {
		kernel = new double[graphs.size()][graphs.size()];
		for (int i = 0; i < graphs.size(); i++) {
			Arrays.fill(kernel[i], 0.0);
		}
		this.graphs = graphs;
		featureVectors = new double[graphs.size()][];
		labelDict = new HashMap<String,String>();
		startLabel = 0;
		currentLabel = 0;
	}

	public void compute() {
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
	}
	
	
	private void relabelGraphs2MultisetLabels() {
		Map<String, Bucket<Vertex<String>>> bucketsV = new HashMap<String, Bucket<Vertex<String>>>();
		Map<String, Bucket<Edge<String>>> bucketsE   = new HashMap<String, Bucket<Edge<String>>>();
		
		// Initialize buckets
		for (int i = startLabel; i < currentLabel; i++) {
			bucketsV.put(Integer.toString(i), new Bucket<Vertex<String>>(Integer.toString(i)));
			bucketsE.put(Integer.toString(i), new Bucket<Edge<String>>(Integer.toString(i)));
		}
		
		// 1. Fill buckets 
		for (DirectedGraph<Vertex<String>, Edge<String>> graph : this.graphs) {
			// Add each edge source (i.e.) start vertex to the bucket of the edge label
			for (Edge<String> edge : graph.getEdges()) {
				bucketsV.get(edge.getLabel()).getContents().add(graph.getSource(edge));
			}
			// Add each incident edge to the bucket of the node label
			for (Vertex<String> vertex : graph.getVertices()) {
				bucketsE.get(vertex.getLabel()).getContents().addAll(graph.getIncidentEdges(vertex));
			}	
		}
		
		// 2. add bucket labels to existing labels
		// Change the original label to a prefix label
		for (DirectedGraph<Vertex<String>, Edge<String>> graph : this.graphs) {
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
	
	private void compressGraphLabels() {
		String label;
		startLabel = currentLabel;
		
		for (DirectedGraph<Vertex<String>, Edge<String>> graph : this.graphs) {
			
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
	}
	
	private void computeFeatureVectors() {
		int index;
		for (int i = 0; i < graphs.size(); i++) {
			featureVectors[i] = new double[currentLabel - startLabel];		
			Arrays.fill(featureVectors[i], 0.0);

			// for each vertex, use the label as index into the feature vector and do a + 1,
			for (Vertex<String> vertex : graphs.get(i).getVertices()) {
				index = Integer.parseInt(vertex.getLabel()) - startLabel;
				
				if (index < 0) {
					System.out.println("Label: " + vertex.getLabel());
					System.out.println("start: " + startLabel);
				}
				
				featureVectors[i][index] += 1.0;
			}
			
			for (Edge<String> edge : graphs.get(i).getEdges()) {
				index = Integer.parseInt(edge.getLabel()) - startLabel;
				featureVectors[i][index] += 1.0;;
			}
		}
	}

	
	private void computeKernelMatrix() {
		for (int i = 0; i < graphs.size(); i++) {
			for (int j = i; j < graphs.size(); j++) {
				kernel[i][j] += dotProduct(featureVectors[i], featureVectors[j]);
				kernel[j][i] = kernel[i][j];
			}
		}
	}
	
	private double dotProduct(double[] fv1, double[] fv2) {
		double sum = 0.0;		
		for (int i = 0; i < fv1.length && i < fv2.length; i++) {
			sum += fv1[i] * fv2[i];
		}	
		return sum;
	}
	
	public void normalize() {
		double[] ss = new double[kernel.length];
		
		for (int i = 0; i < ss.length; i++) {
			ss[i] = kernel[i][i];
		}
			
		for (int i = 0; i < kernel.length; i++) {
			for (int j = i; j < kernel[i].length; j++) {
				kernel[i][j] /= Math.sqrt(ss[i] * ss[j]);
				kernel[j][i] = kernel[i][j];
			}
		}
	}
	

	public double[][] getKernel() {
		return kernel;
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
