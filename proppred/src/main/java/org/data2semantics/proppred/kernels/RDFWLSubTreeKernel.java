package org.data2semantics.proppred.kernels;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.data2semantics.tools.graphs.Edge;
import org.data2semantics.tools.graphs.Vertex;
import org.data2semantics.tools.rdf.RDFDataSet;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;

public class RDFWLSubTreeKernel {

	private Map<String, String> labelMap;
	private Map<String, Vertex<Map<Integer,String>>> instanceVertices;
	private int labelCounter;
	private int depth;
	private int iterations;
	private boolean inference;


	public RDFWLSubTreeKernel() {
		labelMap = new TreeMap<String, String>();
		instanceVertices = new TreeMap<String, Vertex<Map<Integer,String>>>();
		labelCounter = 1;
		depth = 3;
		inference = false;
		iterations = 2;
	}

	public double[][] compute(RDFDataSet dataset, List<Resource> instances, List<Statement> blackList) {
		double[][] featureVectors = new double[instances.size()][];
		double[][] kernel = new double[instances.size()][instances.size()];
		for (int i = 0; i < instances.size(); i++) {
			Arrays.fill(kernel[i], 0.0);
		}

		int startLabel = 0;

		DirectedGraph<Vertex<Map<Integer,String>>,Edge<Map<Integer,String>>> graph = createGraphFromRDF(dataset, instances, blackList);

		computeFeatureVectors(graph, instances, startLabel, featureVectors);
		computeKernelMatrix(instances, featureVectors, kernel, 1);

		for (int i = 0; i < iterations; i++) {
			relabelGraph2MultisetLabels(graph, startLabel);
			startLabel = labelCounter;
			compressGraphLabels(graph);
			computeFeatureVectors(graph, instances, startLabel, featureVectors);
			computeKernelMatrix(instances, featureVectors, kernel, i+2);
		}

		kernel = normalize(kernel);

		for (int i = 0; i < kernel.length; i++) {
			System.out.println(Arrays.toString(kernel[i]));
		}

		return kernel;
	}


	private DirectedGraph<Vertex<Map<Integer,String>>,Edge<Map<Integer,String>>> createGraphFromRDF(RDFDataSet dataset, List<Resource> instances, List<Statement> blackList) {
		Map<String, Vertex<Map<Integer,String>>> vertexMap = new TreeMap<String, Vertex<Map<Integer, String>>>();
		Map<String, Edge<Map<Integer,String>>> edgeMap = new TreeMap<String, Edge<Map<Integer, String>>>();

		DirectedGraph<Vertex<Map<Integer,String>>,Edge<Map<Integer,String>>> graph = new DirectedSparseMultigraph<Vertex<Map<Integer,String>>,Edge<Map<Integer,String>>>();

		List<Resource> queryNodes = new ArrayList<Resource>();
		List<Resource> newQueryNodes;
		List<Statement> result;

		for (Resource instance : instances) {
			Vertex<Map<Integer, String>> startV;
			// If the instance is already part of the graph (because it was retrieved for an earlier instance), 
			// then we use that one, but we need to change the labels to the start label of instance nodes, for which we use '0'
			if (vertexMap.containsKey(instance.toString())) {
				startV = vertexMap.get(instance.toString());
				for (int di : startV.getLabel().keySet()) {
					startV.getLabel().put(di, "0");
				}
				// Else we construct a new node for the instance, and label it with '0' for the provided depth
			} else {
				startV = new Vertex<Map<Integer,String>>(new TreeMap<Integer, String>());
				vertexMap.put(instance.toString(), startV);
				graph.addVertex(startV);
			}
			startV.getLabel().put(depth, "0"); 
			labelMap.put(instance.toString(), "0"); // This label is (re)set to '0'
			instanceVertices.put(instance.toString(), startV); // So that we can reconstruct subgraphs later, we save the instance vertices

			queryNodes.add(instance);

			for (int i = depth - 1; i >= 0; i--) {
				newQueryNodes = new ArrayList<Resource>();

				for (Resource queryNode : queryNodes) {
					Vertex<Map<Integer,String>> newV;
					Edge<Map<Integer,String>> newE;
					result = dataset.getStatements(queryNode, null, null, inference);

					for (Statement stmt : result) {

						// Process new vertex
						if (vertexMap.containsKey(stmt.getObject().toString())) { // existing vertex
							newV = vertexMap.get(stmt.getObject().toString());				 
							newV.getLabel().put(i, labelMap.get(stmt.getObject().toString())); // Set the label for depth i to the already existing label for this vertex

						} else { // New vertex
							newV = new Vertex<Map<Integer,String>>(new TreeMap<Integer, String>());
							labelMap.put(stmt.getObject().toString(), Integer.toString(labelCounter));
							newV.getLabel().put(i, Integer.toString(labelCounter));
							labelCounter++;
							vertexMap.put(stmt.getObject().toString(), newV);
							graph.addVertex(newV);
						}

						// Process new Edge
						if (edgeMap.containsKey(stmt.toString())) { // existing edge
							newE = edgeMap.get(stmt.toString());
							newE.getLabel().put(i, labelMap.get(stmt.toString())); // Set the label for depth i to the already existing label for this edge
						} else { // new edge
							newE = new Edge<Map<Integer,String>>(new TreeMap<Integer,String>());
							labelMap.put(stmt.toString(), Integer.toString(labelCounter));
							newE.getLabel().put(i, Integer.toString(labelCounter));
							labelCounter++;
							edgeMap.put(stmt.toString(), newE);
							graph.addEdge(newE, vertexMap.get(stmt.getSubject().toString()), newV, EdgeType.DIRECTED);
						}



						// Store the object nodes if the loop continues (i>0) and if its a Resource
						if (i > 0 && stmt.getObject() instanceof Resource) {
							newQueryNodes.add((Resource) stmt.getObject());
						}
					}
				}

				queryNodes = newQueryNodes;
			}		
		}

		// Remove edges for statements on the blackList
		for (Statement stmt : blackList) {
			graph.removeEdge(edgeMap.get(stmt.toString()));
		}

		/* ON SECOND thought, we don't need to do this
		// Remove orphaned vertices
		List<Vertex<Map<Integer, String>>> rem = new ArrayList<Vertex<Map<Integer, String>>>();
		for (Vertex<Map<Integer, String>> v : graph.getVertices()) {
			if (graph.inDegree(v) == 0 && graph.outDegree(v) == 0) {
				rem.add(v);
			}
		}
		for (Vertex<Map<Integer, String>> v : rem) {
			graph.removeVertex(v);
		}*/

		return graph;
	}



	private void relabelGraph2MultisetLabels(DirectedGraph<Vertex<Map<Integer,String>>, Edge<Map<Integer,String>>> graph, int startLabel) {
		Map<String, Bucket<VertexIndexPair>> bucketsV = new TreeMap<String, Bucket<VertexIndexPair>>();
		Map<String, Bucket<EdgeIndexPair>> bucketsE   = new TreeMap<String, Bucket<EdgeIndexPair>>();

		// Initialize buckets
		for (int i = startLabel; i < labelCounter; i++) {
			bucketsV.put(Integer.toString(i), new Bucket<VertexIndexPair>(Integer.toString(i)));
			bucketsE.put(Integer.toString(i), new Bucket<EdgeIndexPair>(Integer.toString(i)));
		}

		// 1. Fill buckets 
		// Add each edge source (i.e.) start vertex to the bucket of the edge label
		for (Edge<Map<Integer,String>> edge : graph.getEdges()) {
			// for each label we add a vertex-index-pair to the bucket
			for (int index : edge.getLabel().keySet()) {
				bucketsV.get(edge.getLabel().get(index)).getContents().add(new VertexIndexPair(graph.getDest(edge), index));
			}
		}

		// Add each incident edge to the bucket of the node label
		for (Vertex<Map<Integer,String>> vertex : graph.getVertices()) {			
			Collection<Edge<Map<Integer,String>>> v2 = graph.getOutEdges(vertex);	

			for (int index : vertex.getLabel().keySet()) {
				if (index > 0) { // If index is 0 then we treat it as a fringe node, thus the label will not be propagated to the edges
					for (Edge<Map<Integer,String>> e2 : v2) {
						bucketsE.get(vertex.getLabel().get(index)).getContents().add(new EdgeIndexPair(e2, index - 1));
					}
				}
			}
		}	

		// 2. add bucket labels to existing labels
		// Change the original label to a prefix label

		for (Edge<Map<Integer,String>> edge : graph.getEdges()) {
			for (int i : edge.getLabel().keySet()) {
				edge.getLabel().put(i, edge.getLabel().get(i) + "_");
			}
		}
		for (Vertex<Map<Integer,String>> vertex : graph.getVertices()) {
			for (int i : vertex.getLabel().keySet()) {
				vertex.getLabel().put(i, vertex.getLabel().get(i) + "_");
			}
		}

		// 3. Relabel to the labels in the buckets
		for (int i = startLabel; i < labelCounter; i++) {
			// Process vertices
			Bucket<VertexIndexPair> bucketV = bucketsV.get(Integer.toString(i));			
			for (VertexIndexPair vp : bucketV.getContents()) {
				vp.getVertex().getLabel().put(vp.getIndex(), vp.getVertex().getLabel().get(vp.getIndex()) + bucketV.getLabel());
			}
			// Process edges
			Bucket<EdgeIndexPair> bucketE = bucketsE.get(Integer.toString(i));			
			for (EdgeIndexPair ep : bucketE.getContents()) {
				ep.getEdge().getLabel().put(ep.getIndex(), ep.getEdge().getLabel().get(ep.getIndex()) + bucketE.getLabel());
			}
		}
	}


	private void compressGraphLabels(DirectedGraph<Vertex<Map<Integer,String>>, Edge<Map<Integer,String>>> graph) {
		String label;

		for (Edge<Map<Integer,String>> edge : graph.getEdges()) {
			for (int i : edge.getLabel().keySet()) {
				label = labelMap.get(edge.getLabel().get(i));						
				if (label == null) {					
					label = Integer.toString(labelCounter);
					labelCounter++;
					labelMap.put(edge.getLabel().get(i), label);				
				}
				edge.getLabel().put(i, label);
			}
		}

		for (Vertex<Map<Integer,String>> vertex : graph.getVertices()) {
			for (int i : vertex.getLabel().keySet()) {
				label = labelMap.get(vertex.getLabel().get(i));
				if (label == null) {
					label = Integer.toString(labelCounter);
					labelCounter++;
					labelMap.put(vertex.getLabel().get(i), label);
				}
				vertex.getLabel().put(i, label);
			}
		}
	}


	private void computeFeatureVectors(DirectedGraph<Vertex<Map<Integer,String>>, Edge<Map<Integer,String>>> graph, List<Resource> instances, int startLabel, double[][] featureVectors) {
		int index;
		Vertex<Map<Integer, String>> startV;
		List<Vertex<Map<Integer, String>>> frontV, newFrontV;
		List<Vertex<Map<Integer, String>>> proccedV;
		List<Edge<Map<Integer, String>>> proccedE;


		for (int i = 0; i < instances.size(); i++) {		
			proccedV = new ArrayList<Vertex<Map<Integer, String>>>();
			proccedE = new ArrayList<Edge<Map<Integer, String>>>();

			startV = instanceVertices.get(instances.get(i).toString());
			frontV = new ArrayList<Vertex<Map<Integer,String>>>();
			frontV.add(startV);

			featureVectors[i] = new double[labelCounter - startLabel];		
			Arrays.fill(featureVectors[i], 0.0);
			index = Integer.parseInt(startV.getLabel().get(depth)) - startLabel;		
			featureVectors[i][index] += 1.0;

			for (int j = depth - 1; j >= 0; j--) {
				newFrontV = new ArrayList<Vertex<Map<Integer,String>>>();
				for (Vertex<Map<Integer, String>> qV : frontV) {
					for (Edge<Map<Integer, String>> edge : graph.getOutEdges(qV)) {
						if (!proccedE.contains(edge)) {
							index = Integer.parseInt(edge.getLabel().get(j)) - startLabel;
							featureVectors[i][index] += 1.0;
							proccedE.add(edge);
						}

						if (!proccedV.contains(graph.getDest(edge))) {
							index = Integer.parseInt(graph.getDest(edge).getLabel().get(j)) - startLabel;
							featureVectors[i][index] += 1.0;
							proccedV.add(graph.getDest(edge));
						}

						if (j > 0) {
							newFrontV.add(graph.getDest(edge));
						}
					}
				}
				frontV = newFrontV;
			}
		}
	}


	private void computeKernelMatrix(List<Resource> instances, double[][] featureVectors, double[][] kernel, int iteration) {
		for (int i = 0; i < instances.size(); i++) {
			for (int j = i; j < instances.size(); j++) {
				kernel[i][j] += dotProduct(featureVectors[i], featureVectors[j]) * (((double) iteration) / ((double) this.iterations+1));
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

	protected double[][] normalize(double[][] kernel) {
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
		return kernel;
	}

	private class VertexIndexPair {
		private Vertex<Map<Integer,String>> vertex;
		private int index;

		public VertexIndexPair(Vertex<Map<Integer, String>> vertex, int index) {
			this.vertex = vertex;
			this.index = index;
		}

		public Vertex<Map<Integer, String>> getVertex() {
			return vertex;
		}
		public int getIndex() {
			return index;
		}		
	}

	private class EdgeIndexPair {
		private Edge<Map<Integer,String>> edge;
		private int index;

		public EdgeIndexPair(Edge<Map<Integer, String>> edge, int index) {
			this.edge = edge;
			this.index = index;
		}

		public Edge<Map<Integer, String>> getEdge() {
			return edge;
		}
		public int getIndex() {
			return index;
		}		
	}

}
