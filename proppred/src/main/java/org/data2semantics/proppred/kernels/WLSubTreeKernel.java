package org.data2semantics.proppred.kernels;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.data2semantics.proppred.libsvm.SparseVector;
import org.data2semantics.tools.graphs.DirectedMultigraphWithRoot;
import org.data2semantics.tools.graphs.Edge;
import org.data2semantics.tools.graphs.GraphFactory;
import org.data2semantics.tools.graphs.Vertex;

import edu.uci.ics.jung.graph.DirectedGraph;
/**
 * Class implementing the Weisfeiler-Lehman graph kernel for Multigraphs with a root node, which occurs in the RDF use case.
 * The current implementation can be made more efficient, since the compute function for test examples recomputes the label dictionary, instead
 * of reusing the one created during training. This makes the applicability of the implementation slightly more general.
 * 
 * TODO include a boolean for saving the labelDict to speed up computation of the kernel in the test phase.
 * 
 * 
 * @author Gerben
 *
 */
public class WLSubTreeKernel implements GraphKernel {
	private int iterations = 2;
	private boolean skipFirst;
	protected String label;
	protected boolean normalize;


	/**
	 * Construct a WLSubTreeKernel. The skipFirst parameter is used to not include the original labels in the kernel computation, i.e. 
	 * we skip the bag of labels as part of the kernel.
	 * 
	 * @param iterations
	 * @param normalize
	 * @param skipFirst
	 */
	public WLSubTreeKernel(int iterations, boolean normalize, boolean skipFirst) {
		this(iterations, normalize);
		this.skipFirst = skipFirst;
	}

	public WLSubTreeKernel(int iterations, boolean normalize) {
		this.normalize = normalize;
		this.iterations = iterations;
		this.label = "WL SubTree Kernel, it=" + iterations;
		skipFirst = false;
	}	

	public WLSubTreeKernel(int iterations) {
		this(iterations, true);
	}



	public String getLabel() {
		return label;
	}

	public void setNormalize(boolean normalize) {
		this.normalize = normalize;
	}

	public double[][] compute(List<DirectedMultigraphWithRoot<Vertex<String>, Edge<String>>> trainGraphs) {

		List<DirectedGraph<Vertex<String>, Edge<String>>> graphs = copyGraphs(trainGraphs);
		SparseVector[] featureVectors = new SparseVector[graphs.size()];
		//double[][] featureVectors = new double[graphs.size()][];
		Map<String, String> labelDict = new HashMap<String,String>();
		double[][] kernel = KernelUtils.initMatrix(graphs.size(), graphs.size());

		int startLabel = 1;
		int currentLabel = 1;

		// We change the original label of the root node of the graph to a generic label
		// This rootlabel identifies the graph uniquely, and we don't want that
		for (DirectedMultigraphWithRoot<Vertex<String>, Edge<String>> graph : trainGraphs) {
			graph.getRootVertex().setLabel(KernelUtils.ROOTID);
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
			computeKernelMatrix(graphs, featureVectors, kernel, i+2);
		}

		if (normalize) {
			return KernelUtils.normalize(kernel);
		} else {		
			return kernel;
		}
	}

	public double[][] compute(
			List<DirectedMultigraphWithRoot<Vertex<String>, Edge<String>>> trainGraphs,
					List<DirectedMultigraphWithRoot<Vertex<String>, Edge<String>>> testGraphs) {

		List<DirectedGraph<Vertex<String>, Edge<String>>> graphs = copyGraphs(testGraphs);
		graphs.addAll(copyGraphs(trainGraphs));
		SparseVector[] featureVectors = new SparseVector[graphs.size()];
		//double[][] featureVectors = new double[graphs.size()][];
		Map<String, String> labelDict = new HashMap<String,String>();
		double[][] kernel = KernelUtils.initMatrix(testGraphs.size(), trainGraphs.size());
		double[] ss = new double[testGraphs.size() + trainGraphs.size()];

		int startLabel = 1;
		int currentLabel = 1;

		for (DirectedMultigraphWithRoot<Vertex<String>, Edge<String>> graph : trainGraphs) {
			graph.getRootVertex().setLabel(KernelUtils.ROOTID);
		}
		for (DirectedMultigraphWithRoot<Vertex<String>, Edge<String>> graph : testGraphs) {
			graph.getRootVertex().setLabel(KernelUtils.ROOTID);
		}

		currentLabel = compressGraphLabels(graphs, labelDict, currentLabel);

		if (!skipFirst) {
			computeFeatureVectors(graphs, featureVectors, startLabel, currentLabel);
			computeKernelMatrix(trainGraphs, testGraphs, featureVectors, kernel, ss, 1);
		}

		for (int i = 0; i < this.iterations; i++) {
			relabelGraphs2MultisetLabels(graphs, startLabel, currentLabel);
			startLabel = currentLabel;
			currentLabel = compressGraphLabels(graphs, labelDict, currentLabel);
			computeFeatureVectors(graphs, featureVectors, startLabel, currentLabel);
			computeKernelMatrix(trainGraphs, testGraphs, featureVectors, kernel, ss, i+2);	
		}

		if (normalize) {
			double[] ssTest = Arrays.copyOfRange(ss, 0, testGraphs.size());
			double[] ssTrain = Arrays.copyOfRange(ss, testGraphs.size(), ss.length);			
			return KernelUtils.normalize(kernel, ssTrain, ssTest);

		} else {		
			return kernel;
		}
	}


	/**
	 * First step in the Weisfeiler-Lehman algorithm, applied to directedgraphs with edge labels.
	 * 
	 * @param graphs
	 * @param startLabel
	 * @param currentLabel
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

		// 3. Relabel to the labels in the buckets
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

	/**
	 * Second step in the WL algorithm. We compress the long labels into new short labels
	 * 
	 * @param graphs
	 * @param labelDict
	 * @param currentLabel
	 * @return
	 */
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


	/**
	 * Compute feature vector for the graphs based on the label dictionary created in the previous two steps
	 * 
	 * @param graphs
	 * @param featureVectors
	 * @param startLabel
	 * @param currentLabel
	 */
	private void computeFeatureVectors(List<DirectedGraph<Vertex<String>, Edge<String>>> graphs, SparseVector[] featureVectors, int startLabel, int currentLabel) {
		int index;
		for (int i = 0; i < graphs.size(); i++) {
			featureVectors[i] = new SparseVector();
			//featureVectors[i] = new double[currentLabel - startLabel];		
			//Arrays.fill(featureVectors[i], 0.0);

			// for each vertex, use the label as index into the feature vector and do a + 1,
			for (Vertex<String> vertex : graphs.get(i).getVertices()) {
				index = Integer.parseInt(vertex.getLabel()) - startLabel + 1;	// Indices in SparseVector start at 1
				featureVectors[i].setValue(index, featureVectors[i].getValue(index) + 1);
				//featureVectors[i][index] += 1.0;
			}

			for (Edge<String> edge : graphs.get(i).getEdges()) {
				index = Integer.parseInt(edge.getLabel()) - startLabel + 1;
				featureVectors[i].setValue(index, featureVectors[i].getValue(index) + 1);
				//featureVectors[i][index] += 1.0;;
			}
		}
	}



	/**
	 * Use the feature vectors to compute a kernel matrix.
	 * 
	 * @param graphs
	 * @param featureVectors
	 * @param kernel
	 * @param iteration
	 */
	private void computeKernelMatrix(List<DirectedGraph<Vertex<String>, Edge<String>>> graphs, SparseVector[] featureVectors, double[][] kernel, int iteration) {
		for (int i = 0; i < graphs.size(); i++) {
			for (int j = i; j < graphs.size(); j++) {
				kernel[i][j] += featureVectors[i].dot(featureVectors[j]) * (((double) iteration) / ((double) this.iterations+1));
				//kernel[i][j] += dotProduct(featureVectors[i], featureVectors[j]) * (((double) iteration) / ((double) this.iterations+1));
				kernel[j][i] = kernel[i][j];
			}
		}
	}


	private void computeKernelMatrix(List<? extends DirectedGraph<Vertex<String>, Edge<String>>> trainGraphs, List<? extends DirectedGraph<Vertex<String>, Edge<String>>> testGraphs, SparseVector[] featureVectors, double[][] kernel, double[] ss, int iteration) {
		for (int i = 0; i < testGraphs.size(); i++) {
			for (int j = 0; j < trainGraphs.size(); j++) {
				kernel[i][j] += featureVectors[i].dot(featureVectors[j + testGraphs.size()]) * (((double) iteration) / ((double) this.iterations+1));
				//kernel[i][j] += dotProduct(featureVectors[i], featureVectors[j + testGraphs.size()]) * (((double) iteration) / ((double) this.iterations+1)); 
			}
		}
		for (int i = 0; i < testGraphs.size() + trainGraphs.size(); i++) {
			ss[i] += featureVectors[i].dot(featureVectors[i]) * (((double) iteration) / ((double) this.iterations+1));
			//ss[i] += dotProduct(featureVectors[i], featureVectors[i]) * (((double) iteration) / ((double) this.iterations+1));
		}

	}

	private List<DirectedGraph<Vertex<String>, Edge<String>>> copyGraphs(List<? extends DirectedGraph<Vertex<String>, Edge<String>>> oldGraphs) {
		List<DirectedGraph<Vertex<String>, Edge<String>>> graphs = new ArrayList<DirectedGraph<Vertex<String>, Edge<String>>>();	
		for(DirectedGraph<Vertex<String>, Edge<String>> graph : oldGraphs) {
			graphs.add(GraphFactory.copyDirectedGraph(graph));				
		}
		return graphs;
	}
}
