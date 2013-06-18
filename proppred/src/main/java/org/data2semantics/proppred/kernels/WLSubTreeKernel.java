package org.data2semantics.proppred.kernels;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.data2semantics.proppred.libsvm.SparseVector;
import org.data2semantics.tools.graphs.DirectedMultigraphWithRoot;
import org.data2semantics.tools.graphs.Edge;
import org.data2semantics.tools.graphs.GraphFactory;
import org.data2semantics.tools.graphs.Vertex;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
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
public class WLSubTreeKernel implements GraphKernel, FeatureVectorKernel {
	private int iterations = 2;
	protected String label;
	protected boolean normalize;


	/**
	 * Construct a WLSubTreeKernel. 
	 * 
	 * @param iterations
	 * @param normalize
	 */
	public WLSubTreeKernel(int iterations, boolean normalize) {
		this.normalize = normalize;
		this.iterations = iterations;
		this.label = "WL SubTree Kernel, it=" + iterations;
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

	
	
	public SparseVector[] computeFeatureVectors(
			List<DirectedMultigraphWithRoot<Vertex<String>, Edge<String>>> trainGraphs) {
		List<DirectedMultigraphWithRoot<Vertex<StringBuilder>, Edge<StringBuilder>>> graphs = copyGraphs(trainGraphs);
		SparseVector[] featureVectors = new SparseVector[graphs.size()];
		for (int i = 0; i < featureVectors.length; i++) {
			featureVectors[i] = new SparseVector();
		}	
		//double[][] featureVectors = new double[graphs.size()][];
		Map<String, String> labelDict = new HashMap<String,String>();
		
		int startLabel = 1;
		int currentLabel = 1;
		
		/* Code for setting all root vertices in all the graphs to the generic label (tested on aff. pred. task to give less performance)
		Set<String> instances = new HashSet<String>();
		for (DirectedMultigraphWithRoot<Vertex<StringBuilder>, Edge<StringBuilder>> graph : graphs) {
			instances.add(graph.getRootVertex().toString());
		}
		
		for (DirectedMultigraphWithRoot<Vertex<StringBuilder>, Edge<StringBuilder>> graph : graphs) {
			for (Vertex<StringBuilder> vertex : graph.getVertices()) {
				if (instances.contains(vertex.getLabel().toString())) {
					vertex.setLabel(new StringBuilder(KernelUtils.ROOTID));
				}
			}
		}
		*/
		

		// We change the original label of the root node of the graph to a generic label
		// This rootlabel identifies the graph uniquely, and we don't want that
		for (DirectedMultigraphWithRoot<Vertex<StringBuilder>, Edge<StringBuilder>> graph : graphs) {
			graph.getRootVertex().setLabel(new StringBuilder(KernelUtils.ROOTID));
		}

		currentLabel = compressGraphLabels(graphs, labelDict, currentLabel);
		computeFVs(graphs, featureVectors, Math.sqrt(1.0 / ((double) (iterations + 1))), currentLabel-1);
		
		
		for (int i = 0; i < this.iterations; i++) {
			relabelGraphs2MultisetLabels(graphs, startLabel, currentLabel);
			startLabel = currentLabel;
			currentLabel = compressGraphLabels(graphs, labelDict, currentLabel);
			computeFVs(graphs, featureVectors, Math.sqrt((2.0 + i) / ((double) (iterations + 1))), currentLabel-1);
		}
		
		if (normalize) {
			featureVectors = KernelUtils.normalize(featureVectors);
		}

		return featureVectors;
	}

	public SparseVector[] computeFeatureVectors(
			List<DirectedMultigraphWithRoot<Vertex<String>, Edge<String>>> trainGraphs,
			List<DirectedMultigraphWithRoot<Vertex<String>, Edge<String>>> testGraphs) {
		// TODO Auto-generated method stub
		return null;
	}

	public double[][] compute(List<DirectedMultigraphWithRoot<Vertex<String>, Edge<String>>> trainGraphs) {
		double[][] kernel = KernelUtils.initMatrix(trainGraphs.size(), trainGraphs.size());
		computeKernelMatrix(computeFeatureVectors(trainGraphs), kernel);				
		return kernel;
	}

	public double[][] compute(
			List<DirectedMultigraphWithRoot<Vertex<String>, Edge<String>>> trainGraphs,
					List<DirectedMultigraphWithRoot<Vertex<String>, Edge<String>>> testGraphs) {

		List<DirectedMultigraphWithRoot<Vertex<StringBuilder>, Edge<StringBuilder>>> graphs = copyGraphs(testGraphs);
		graphs.addAll(copyGraphs(trainGraphs));
		SparseVector[] featureVectors = new SparseVector[graphs.size()];
		for (int i = 0; i < featureVectors.length; i++) {
			featureVectors[i] = new SparseVector();
		}	
		//double[][] featureVectors = new double[graphs.size()][];
		Map<String, String> labelDict = new HashMap<String,String>();
		double[][] kernel = KernelUtils.initMatrix(testGraphs.size(), trainGraphs.size());
		double[] ss = new double[testGraphs.size() + trainGraphs.size()];

		int startLabel = 1;
		int currentLabel = 1;

		for (DirectedMultigraphWithRoot<Vertex<StringBuilder>, Edge<StringBuilder>> graph : graphs) {
			graph.getRootVertex().setLabel(new StringBuilder(KernelUtils.ROOTID));
		}
	
		currentLabel = compressGraphLabels(graphs, labelDict, currentLabel);
		computeFVs(graphs, featureVectors, Math.sqrt(1.0 / ((double) (iterations + 1))), currentLabel-1);
		

		for (int i = 0; i < this.iterations; i++) {
			relabelGraphs2MultisetLabels(graphs, startLabel, currentLabel);
			startLabel = currentLabel;
			currentLabel = compressGraphLabels(graphs, labelDict, currentLabel);
			computeFVs(graphs, featureVectors, Math.sqrt((2.0 + i) / ((double) (iterations + 1))), currentLabel-1);	
		}
		
		computeKernelMatrix(trainGraphs, testGraphs, featureVectors, kernel, ss);

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
	private void relabelGraphs2MultisetLabels(List<DirectedMultigraphWithRoot<Vertex<StringBuilder>, Edge<StringBuilder>>> graphs, int startLabel, int currentLabel) {
		Map<String, Bucket<Vertex<StringBuilder>>> bucketsV = new HashMap<String, Bucket<Vertex<StringBuilder>>>();
		Map<String, Bucket<Edge<StringBuilder>>> bucketsE   = new HashMap<String, Bucket<Edge<StringBuilder>>>();

		// Initialize buckets
		for (int i = startLabel; i < currentLabel; i++) {
			bucketsV.put(Integer.toString(i), new Bucket<Vertex<StringBuilder>>(Integer.toString(i)));
			bucketsE.put(Integer.toString(i), new Bucket<Edge<StringBuilder>>(Integer.toString(i)));
		}

		// 1. Fill buckets 
		for (DirectedMultigraphWithRoot<Vertex<StringBuilder>, Edge<StringBuilder>> graph : graphs) {
			// Add each edge source (i.e.) start vertex to the bucket of the edge label
			for (Edge<StringBuilder> edge : graph.getEdges()) {
				bucketsV.get(edge.getLabel().toString()).getContents().add(graph.getDest(edge));
			}

			// Add each incident edge to the bucket of the node label
			for (Vertex<StringBuilder> vertex : graph.getVertices()) {			
				Collection<Edge<StringBuilder>> v2 = graph.getOutEdges(vertex);	
				bucketsE.get(vertex.getLabel().toString()).getContents().addAll(v2);
			}	
		}

		// 2. add bucket labels to existing labels
		// Change the original label to a prefix label
		for (DirectedMultigraphWithRoot<Vertex<StringBuilder>, Edge<StringBuilder>> graph : graphs) {
			for (Edge<StringBuilder> edge : graph.getEdges()) {
				edge.getLabel().append("_");
			}
			for (Vertex<StringBuilder> vertex : graph.getVertices()) {
				vertex.getLabel().append("_");
			}
		}

		// 3. Relabel to the labels in the buckets
		for (int i = startLabel; i < currentLabel; i++) {
			// Process vertices
			Bucket<Vertex<StringBuilder>> bucketV = bucketsV.get(Integer.toString(i));			
			for (Vertex<StringBuilder> vertex : bucketV.getContents()) {
				vertex.getLabel().append(bucketV.getLabel());
				vertex.getLabel().append("_");
			}
			// Process edges
			Bucket<Edge<StringBuilder>> bucketE = bucketsE.get(Integer.toString(i));			
			for (Edge<StringBuilder> edge : bucketE.getContents()) {
				edge.getLabel().append(bucketE.getLabel());
				edge.getLabel().append("_");
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
	private int compressGraphLabels(List<DirectedMultigraphWithRoot<Vertex<StringBuilder>, Edge<StringBuilder>>> graphs, Map<String, String> labelDict, int currentLabel) {
		String label;

		for (DirectedGraph<Vertex<StringBuilder>, Edge<StringBuilder>> graph : graphs) {

			for (Edge<StringBuilder> edge : graph.getEdges()) {
				label = labelDict.get(edge.getLabel().toString());						
				if (label == null) {					
					label = Integer.toString(currentLabel);
					currentLabel++;
					labelDict.put(edge.getLabel().toString(), label);				
				}
				edge.setLabel(new StringBuilder(label));
			}

			for (Vertex<StringBuilder> vertex : graph.getVertices()) {
				label = labelDict.get(vertex.getLabel().toString());
				if (label == null) {
					label = Integer.toString(currentLabel);
					currentLabel++;
					labelDict.put(vertex.getLabel().toString(), label);
				}
				vertex.setLabel(new StringBuilder(label));
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
	private void computeFVs(List<DirectedMultigraphWithRoot<Vertex<StringBuilder>, Edge<StringBuilder>>> graphs, SparseVector[] featureVectors, double weight, int lastIndex) {
		int index;
		for (int i = 0; i < graphs.size(); i++) {
			featureVectors[i].setLastIndex(lastIndex);
			//featureVectors[i] = new SparseVector();
			//featureVectors[i] = new double[currentLabel - startLabel];		
			//Arrays.fill(featureVectors[i], 0.0);

			// for each vertex, use the label as index into the feature vector and do a + 1,
			for (Vertex<StringBuilder> vertex : graphs.get(i).getVertices()) {
				index = Integer.parseInt(vertex.getLabel().toString());	
				featureVectors[i].setValue(index, featureVectors[i].getValue(index) + weight);
				//featureVectors[i][index] += 1.0;
			}

			for (Edge<StringBuilder> edge : graphs.get(i).getEdges()) {
				index = Integer.parseInt(edge.getLabel().toString());
				featureVectors[i].setValue(index, featureVectors[i].getValue(index) + weight);
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
	private void computeKernelMatrix(SparseVector[] featureVectors, double[][] kernel) {
		for (int i = 0; i < featureVectors.length; i++) {
			for (int j = i; j < featureVectors.length; j++) {
				kernel[i][j] += featureVectors[i].dot(featureVectors[j]);
				//kernel[i][j] += dotProduct(featureVectors[i], featureVectors[j]) * (((double) iteration) / ((double) this.iterations+1));
				kernel[j][i] = kernel[i][j];
			}
		}
	}


	private void computeKernelMatrix(List<? extends DirectedMultigraphWithRoot<Vertex<String>, Edge<String>>> trainGraphs, List<? extends DirectedMultigraphWithRoot<Vertex<String>, Edge<String>>> testGraphs, SparseVector[] featureVectors, double[][] kernel, double[] ss) {
		for (int i = 0; i < testGraphs.size(); i++) {
			for (int j = 0; j < trainGraphs.size(); j++) {
				kernel[i][j] += featureVectors[i].dot(featureVectors[j + testGraphs.size()]);
				//kernel[i][j] += dotProduct(featureVectors[i], featureVectors[j + testGraphs.size()]) * (((double) iteration) / ((double) this.iterations+1)); 
			}
		}
		for (int i = 0; i < testGraphs.size() + trainGraphs.size(); i++) {
			ss[i] += featureVectors[i].dot(featureVectors[i]);
			//ss[i] += dotProduct(featureVectors[i], featureVectors[i]) * (((double) iteration) / ((double) this.iterations+1));
		}

	}

	private List<DirectedMultigraphWithRoot<Vertex<StringBuilder>, Edge<StringBuilder>>> copyGraphs(List<DirectedMultigraphWithRoot<Vertex<String>, Edge<String>>> oldGraphs) {
		List<DirectedMultigraphWithRoot<Vertex<StringBuilder>, Edge<StringBuilder>>> graphs = new ArrayList<DirectedMultigraphWithRoot<Vertex<StringBuilder>, Edge<StringBuilder>>>();	
		for(DirectedMultigraphWithRoot<Vertex<String>, Edge<String>> graph : oldGraphs) {
			DirectedMultigraphWithRoot<Vertex<StringBuilder>, Edge<StringBuilder>> newGraph = new DirectedMultigraphWithRoot<Vertex<StringBuilder>, Edge<StringBuilder>>();
			String rootLabel = graph.getRootVertex().getLabel();
			
			Map<Vertex<String>, Vertex<StringBuilder>> nodes = new HashMap<Vertex<String>, Vertex<StringBuilder>>();

			for (Vertex<String> vertex : graph.getVertices()) {
				Vertex<StringBuilder> newV = new Vertex<StringBuilder>(new StringBuilder(vertex.getLabel()));
				nodes.put(vertex, newV);
				
				if (vertex.getLabel().equals(rootLabel)) {
					newGraph.setRootVertex(newV);
				}
			}		
			for (Edge<String> edge : graph.getEdges()) {
				newGraph.addEdge(new Edge<StringBuilder>(new StringBuilder(edge.getLabel())), nodes.get(graph.getSource(edge)), nodes.get(graph.getDest(edge)), EdgeType.DIRECTED);
			}
			graphs.add(newGraph);				
		}
		return graphs;
	}
}
