package org.data2semantics.exp.ecml2013;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.data2semantics.proppred.kernels.Bucket;
import org.data2semantics.proppred.kernels.KernelUtils;
import org.data2semantics.proppred.kernels.graphkernels.GraphKernel;
import org.data2semantics.tools.graphs.DirectedMultigraphWithRoot;
import org.data2semantics.tools.graphs.Edge;
import org.data2semantics.tools.graphs.Vertex;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
/**
 * ECML 2013 version of the regular Weisfeiler-Lehman graph kernel.
 * Class implementing the Weisfeiler-Lehman graph kernel for Multigraphs with a root node, which occurs in the RDF use case.
 * 
 * @author Gerben
 */
public class ECML2013WLSubTreeKernel implements GraphKernel {
	private int iterations = 2;
	protected String label;
	protected boolean normalize;
	private int startLabel = 1;
	private int currentLabel = 1;


	/**
	 * Construct a WLSubTreeKernel. 
	 * 
	 * @param iterations
	 * @param normalize
	 */
	public ECML2013WLSubTreeKernel(int iterations, boolean normalize) {
		this.normalize = normalize;
		this.iterations = iterations;
		this.label = "WL SubTree Kernel, it=" + iterations;
	}	

	public ECML2013WLSubTreeKernel(int iterations) {
		this(iterations, true);
	}


	public String getLabel() {
		return label;
	}

	public void setNormalize(boolean normalize) {
		this.normalize = normalize;
	}


	public double[][] compute(List<DirectedMultigraphWithRoot<Vertex<String>, Edge<String>>> trainGraphs) {
		List<DirectedMultigraphWithRoot<Vertex<String>, Edge<String>>> graphs = copyGraphs(trainGraphs);

		double[][] featureVectors = new double[graphs.size()][];
		double[][] kernel = KernelUtils.initMatrix(graphs.size(), graphs.size());

		Map<String, String> labelDict = new HashMap<String,String>();

		// We change the original label of the root node of the graph to a generic label
		// This rootlabel identifies the graph uniquely, and we don't want that
		Set<String> rootLabels = new HashSet<String>();
		for (DirectedMultigraphWithRoot<Vertex<String>, Edge<String>> graph : graphs) {
			rootLabels.add(graph.getRootVertex().getLabel());
		}
		for (DirectedMultigraphWithRoot<Vertex<String>, Edge<String>> graph : graphs) {
			graph.getRootVertex().setLabel(KernelUtils.ROOTID);
			for (Vertex<String> vertex : graph.getVertices()) {
				if (rootLabels.contains(vertex.getLabel())) {
					vertex.setLabel(KernelUtils.ROOTID);
				}
			}
		}

		currentLabel = compressGraphLabels(graphs, labelDict, currentLabel);
		computeFVs(graphs, featureVectors);
		computeKernelMatrix(featureVectors, kernel, 1.0 / (iterations+1.0));


		for (int i = 0; i < this.iterations; i++) {
			relabelGraphs2MultisetLabels(graphs, startLabel, currentLabel);
			startLabel = currentLabel;
			currentLabel = compressGraphLabels(graphs, labelDict, currentLabel);
			computeFVs(graphs, featureVectors);
			computeKernelMatrix(featureVectors, kernel, (2.0+i) / (iterations+1.0));
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

		return null;
	}


	/**
	 * First step in the Weisfeiler-Lehman algorithm, applied to directedgraphs with edge labels.
	 * 
	 * @param graphs
	 * @param startLabel
	 * @param currentLabel
	 */
	private void relabelGraphs2MultisetLabels(List<DirectedMultigraphWithRoot<Vertex<String>, Edge<String>>> graphs, int startLabel, int currentLabel) {
		Map<String, Bucket<Vertex<String>>> bucketsV = new HashMap<String, Bucket<Vertex<String>>>();
		Map<String, Bucket<Edge<String>>> bucketsE   = new HashMap<String, Bucket<Edge<String>>>();

		// Initialize buckets
		for (int i = startLabel; i < currentLabel; i++) {
			bucketsV.put(Integer.toString(i), new Bucket<Vertex<String>>(Integer.toString(i)));
			bucketsE.put(Integer.toString(i), new Bucket<Edge<String>>(Integer.toString(i)));
		}

		// 1. Fill buckets 

				for (DirectedMultigraphWithRoot<Vertex<String>, Edge<String>> graph : graphs) {
				// Add each edge source (i.e.) start vertex to the bucket of the edge label
				for (Edge<String> edge : graph.getEdges()) {
					bucketsV.get(edge.getLabel().toString()).getContents().add(graph.getDest(edge));
				}

				// Add each incident edge to the bucket of the node label
				for (Vertex<String> vertex : graph.getVertices()) {			
					Collection<Edge<String>> v2 = graph.getOutEdges(vertex);	
					bucketsE.get(vertex.getLabel().toString()).getContents().addAll(v2);
				}	
			}

		// 2. add bucket labels to existing labels
		// Change the original label to a prefix label
		for (DirectedMultigraphWithRoot<Vertex<String>, Edge<String>> graph : graphs) {
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
				vertex.setLabel(vertex.getLabel() + bucketV.getLabel() ); // + "_"
			}
			// Process edges
			Bucket<Edge<String>> bucketE = bucketsE.get(Integer.toString(i));			
			for (Edge<String> edge : bucketE.getContents()) {
				edge.setLabel(edge.getLabel() + bucketE.getLabel() ); // + "_"
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
	private int compressGraphLabels(List<DirectedMultigraphWithRoot<Vertex<String>, Edge<String>>> graphs, Map<String, String> labelDict, int currentLabel) {
		String label;

		for (DirectedGraph<Vertex<String>, Edge<String>> graph : graphs) {

			for (Edge<String> edge : graph.getEdges()) {
				label = labelDict.get(edge.getLabel().toString());						
				if (label == null) {					
					label = Integer.toString(currentLabel);
					currentLabel++;
					labelDict.put(edge.getLabel().toString(), label);				
				}
				edge.setLabel(label);
			}

			for (Vertex<String> vertex : graph.getVertices()) {
				label = labelDict.get(vertex.getLabel().toString());
				if (label == null) {
					label = Integer.toString(currentLabel);
					currentLabel++;
					labelDict.put(vertex.getLabel().toString(), label);
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
	private void computeFVs(List<DirectedMultigraphWithRoot<Vertex<String>, Edge<String>>> graphs, double[][] featureVectors) {
		int index;
		for (int i = 0; i < graphs.size(); i++) {
			featureVectors[i] = new double[currentLabel - startLabel];		
			Arrays.fill(featureVectors[i], 0.0);

			for (Vertex<String> vertex : graphs.get(i).getVertices()) {
				index = Integer.parseInt(vertex.getLabel()) - startLabel;	
				featureVectors[i][index] += 1.0;
			}

			for (Edge<String> edge : graphs.get(i).getEdges()) {
				index = Integer.parseInt(edge.getLabel()) - startLabel;
				featureVectors[i][index] += 1.0;
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
	private void computeKernelMatrix(double[][] featureVectors, double[][] kernel, double factor) {
		for (int i = 0; i < featureVectors.length; i++) {
			for (int j = i; j < featureVectors.length; j++) {			
				kernel[i][j] += KernelUtils.dotProduct(featureVectors[i], featureVectors[j]) * factor;
				kernel[j][i] = kernel[i][j];
			}
		}
	}

	private List<DirectedMultigraphWithRoot<Vertex<String>, Edge<String>>> copyGraphs(List<DirectedMultigraphWithRoot<Vertex<String>, Edge<String>>> oldGraphs) {
		List<DirectedMultigraphWithRoot<Vertex<String>, Edge<String>>> graphs = new ArrayList<DirectedMultigraphWithRoot<Vertex<String>, Edge<String>>>();	
		for(DirectedMultigraphWithRoot<Vertex<String>, Edge<String>> graph : oldGraphs) {
			DirectedMultigraphWithRoot<Vertex<String>, Edge<String>> newGraph = new DirectedMultigraphWithRoot<Vertex<String>, Edge<String>>();
			String rootLabel = graph.getRootVertex().getLabel();

			Map<Vertex<String>, Vertex<String>> nodes = new HashMap<Vertex<String>, Vertex<String>>();

			for (Vertex<String> vertex : graph.getVertices()) {
				Vertex<String> newV = new Vertex<String>(vertex.getLabel());
				nodes.put(vertex, newV);

				if (vertex.getLabel().equals(rootLabel)) {
					newGraph.setRootVertex(newV);
				}
			}		
			for (Edge<String> edge : graph.getEdges()) {
				newGraph.addEdge(new Edge<String>(edge.getLabel()), nodes.get(graph.getSource(edge)), nodes.get(graph.getDest(edge)), EdgeType.DIRECTED);
			}
			graphs.add(newGraph);				
		}
		return graphs;
	}
}
