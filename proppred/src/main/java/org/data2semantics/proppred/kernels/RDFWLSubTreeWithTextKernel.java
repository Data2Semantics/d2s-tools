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
import org.data2semantics.proppred.libsvm.text.TextUtils;
import org.data2semantics.tools.graphs.Edge;
import org.data2semantics.tools.graphs.Vertex;
import org.data2semantics.tools.rdf.RDFDataSet;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;

/**
 * This class implements a WL kernel directly on an RDF graph. The difference with a normal WL kernel is that subgraphs are not 
 * explicitly extracted. However we use the idea of subgraph implicitly by tracking for each vertex/edge the distance from an instance vertex.
 * For one thing, this leads to the fact that 1 black list is applied to the entire RDF graph, instead of 1 (small) blacklist per graph. 
 * 
 *
 * 
 * @author Gerben
 *
 */
public class RDFWLSubTreeWithTextKernel implements RDFGraphKernel, RDFFeatureVectorKernel {
	private static final String ROOT_LABEL = "1";
	private static final String LITERAL_LABEL = "2";
	private static final String BLANK_VERTEX_LABEL = "1";
	private static final String BLANK_EDGE_LABEL   = "2";

	private Map<String, String> labelMap;
	private Map<Vertex<Map<Integer,StringBuilder>>, Map<Integer, SparseVector>> vertexLiteralMap;

	private Map<String, Vertex<Map<Integer,StringBuilder>>> instanceVertices;
	private Map<String, Map<Vertex<Map<Integer,StringBuilder>>, Integer>> instanceVertexIndexMap;
	private Map<String, Map<Edge<Map<Integer,StringBuilder>>, Integer>> instanceEdgeIndexMap;

	private int labelCounter;
	private int depth;
	private int iterations;
	private boolean inference;
	private boolean blankLabels;
	private String label;
	private boolean normalize;


	public RDFWLSubTreeWithTextKernel(int iterations, int depth, boolean inference, boolean normalize, boolean blankLabels) {
		this(iterations, depth, inference, normalize);
		this.blankLabels = blankLabels;

	}

	public RDFWLSubTreeWithTextKernel(int iterations, int depth, boolean inference, boolean normalize) {
		this.normalize = normalize;
		this.label = "RDF_WL_Kernel_" + depth + "_" + iterations;
		this.blankLabels = false;

		labelMap = new HashMap<String, String>();
		vertexLiteralMap = new HashMap<Vertex<Map<Integer,StringBuilder>>, Map<Integer, SparseVector>>();

		instanceVertices = new HashMap<String, Vertex<Map<Integer,StringBuilder>>>();
		this.instanceVertexIndexMap = new HashMap<String, Map<Vertex<Map<Integer,StringBuilder>>, Integer>>();
		this.instanceEdgeIndexMap = new HashMap<String, Map<Edge<Map<Integer,StringBuilder>>, Integer>>();

		labelCounter = 3;
		this.depth = depth;
		this.inference = inference;
		this.iterations = iterations;
	}


	public RDFWLSubTreeWithTextKernel() {
		this(2, 2, false, true);
	}

	public String getLabel() {
		return label;
	}

	public void setNormalize(boolean normalize) {
		this.normalize = normalize;
	}

	public Map<String,String> getInverseLabelMap() {
		Map<String,String> invMap = new HashMap<String,String>();
		for (String k : labelMap.keySet()) {
			invMap.put(labelMap.get(k), k);
		}
		return invMap;
	}


	public SparseVector[] computeFeatureVectors(RDFDataSet dataset, List<Resource> instances, List<Statement> blackList) {
		SparseVector[] featureVectors = new SparseVector[instances.size()];
		SparseVector[] textFeatureVectors = new SparseVector[instances.size()];
		for (int i = 0; i < featureVectors.length; i++) {
			featureVectors[i] = new SparseVector();
			textFeatureVectors[i] = new SparseVector();
		}	

		DirectedGraph<Vertex<Map<Integer,StringBuilder>>,Edge<Map<Integer,StringBuilder>>> graph = createGraphFromRDF(dataset, instances, blackList);
		createInstanceIndexMaps(graph, instances);

		if (blankLabels) {
			setBlankLabels(graph);
		}

		computeFVs(graph, instances, Math.sqrt(1.0 / ((double) (iterations + 1))), featureVectors, textFeatureVectors);

		int startLabel = 1; // start at 1, since featureVectors need to start at index 1
		for (int i = 0; i < iterations; i++) {	
			relabelGraph2MultisetLabels(graph, startLabel);
			startLabel = labelCounter;
			compressGraphLabels(graph);
			computeFVs(graph, instances, Math.sqrt((2.0 + i) / ((double) (iterations + 1))), featureVectors, textFeatureVectors);
		}

		for (int i = 0; i < featureVectors.length; i++) {
			featureVectors[i].addVector(textFeatureVectors[i]);
		}

		if (this.normalize) {
			featureVectors = KernelUtils.normalize(featureVectors);
		}

		return featureVectors;
	}

	public double[][] compute(RDFDataSet dataset, List<Resource> instances, List<Statement> blackList) {
		SparseVector[] featureVectors = computeFeatureVectors(dataset, instances, blackList);
		//featureVectors = TextUtils.computeTFIDF(Arrays.asList(featureVectors)).toArray(new SparseVector[1]);
		//featureVectors = KernelUtils.normalize(featureVectors);
		double[][] kernel = KernelUtils.initMatrix(instances.size(), instances.size());
		computeKernelMatrix(instances, featureVectors, kernel);
		return kernel;
	}


	private DirectedGraph<Vertex<Map<Integer,StringBuilder>>,Edge<Map<Integer,StringBuilder>>> createGraphFromRDF(RDFDataSet dataset, List<Resource> instances, List<Statement> blackList) {
		Map<String, Vertex<Map<Integer,StringBuilder>>> literalMap = new HashMap<String, Vertex<Map<Integer, StringBuilder>>>();
		Map<String, Vertex<Map<Integer,StringBuilder>>> vertexMap = new HashMap<String, Vertex<Map<Integer, StringBuilder>>>();
		Map<String, Edge<Map<Integer,StringBuilder>>> edgeMap = new HashMap<String, Edge<Map<Integer, StringBuilder>>>();
		Map<Vertex<Map<Integer,StringBuilder>>, Map<Integer, String>> vertexLiteralStringMap = new HashMap<Vertex<Map<Integer,StringBuilder>>, Map<Integer, String>>();

		DirectedGraph<Vertex<Map<Integer,StringBuilder>>,Edge<Map<Integer,StringBuilder>>> graph = new DirectedSparseMultigraph<Vertex<Map<Integer,StringBuilder>>,Edge<Map<Integer,StringBuilder>>>();

		List<Resource> queryNodes = new ArrayList<Resource>();
		List<Resource> newQueryNodes;
		List<Statement> result;

		Vertex<Map<Integer,StringBuilder>> startV;
		Vertex<Map<Integer,StringBuilder>> newV;
		Edge<Map<Integer,StringBuilder>> newE;

		String idStr, idStr2;

		for (Resource instance : instances) {
			idStr = instance.toString();
			// If the instance is already part of the graph (because it was retrieved for an earlier instance), 
			// then we use that one, but we need to change the labels to the start label of instance nodes, for which we use ROOT_LABEL
			if (vertexMap.containsKey(idStr)) {
				startV = vertexMap.get(idStr);
				for (int di : startV.getLabel().keySet()) {
					startV.getLabel().put(di, new StringBuilder(ROOT_LABEL));
				}

				// Else we construct a new node for the instance, and label it with ROOT_LABEL for the provided depth
			} else {
				startV = new Vertex<Map<Integer,StringBuilder>>(new HashMap<Integer, StringBuilder>());
				vertexMap.put(idStr, startV);
				graph.addVertex(startV);
			}
			startV.getLabel().put(depth, new StringBuilder(ROOT_LABEL)); 
			labelMap.put(idStr, ROOT_LABEL); // This label is (re)set to ROOT_LABEL
			instanceVertices.put(idStr, startV); // So that we can reconstruct subgraphs later, we save the instance vertices

			queryNodes.add(instance);

			for (int i = depth - 1; i >= 0; i--) {
				newQueryNodes = new ArrayList<Resource>();

				for (Resource queryNode : queryNodes) {
					result = dataset.getStatements(queryNode, null, null, inference);

					for (Statement stmt : result) {
						newV = null;

						// literal
						if (stmt.getObject() instanceof Literal) {
							idStr = stmt.toString();
							idStr2 = stmt.getObject().toString();

							if (literalMap.containsKey(idStr)) {
								newV = literalMap.get(idStr);				 
								newV.getLabel().put(i, new StringBuilder(LITERAL_LABEL)); 
							} else {
								newV = new Vertex<Map<Integer,StringBuilder>>(new HashMap<Integer, StringBuilder>());
								labelMap.put(idStr2, LITERAL_LABEL);
								newV.getLabel().put(i, new StringBuilder(LITERAL_LABEL));
								literalMap.put(idStr, newV);
								graph.addVertex(newV);												
							}
							if (!vertexLiteralStringMap.containsKey(newV)) {
								vertexLiteralStringMap.put(newV, new HashMap<Integer,String>());
							}
							vertexLiteralStringMap.get(newV).put(i, idStr2); // save the original value of the literal for this vertex		

						} else { // Non-literal
							idStr = stmt.getObject().toString();
							if (vertexMap.containsKey(idStr)) { // existing vertex
								newV = vertexMap.get(idStr);				 
								newV.getLabel().put(i, new StringBuilder(labelMap.get(idStr))); // Set the label for depth i to the already existing label for this vertex
							} else { // New vertex
								newV = new Vertex<Map<Integer,StringBuilder>>(new HashMap<Integer, StringBuilder>());
								labelMap.put(idStr, Integer.toString(labelCounter));
								newV.getLabel().put(i, new StringBuilder(Integer.toString(labelCounter)));
								labelCounter++;
								vertexMap.put(idStr, newV);
								graph.addVertex(newV);
							}
						}


						// Process new Edge
						if (newV != null) {
							idStr = stmt.toString();
							idStr2 = stmt.getPredicate().toString();
							if (edgeMap.containsKey(idStr)) { // existing edge
								newE = edgeMap.get(idStr);
								newE.getLabel().put(i, new StringBuilder(labelMap.get(idStr2))); // Set the label for depth i to the already existing label for this edge

							} else { // new edge
								newE = new Edge<Map<Integer,StringBuilder>>(new HashMap<Integer,StringBuilder>());
								if (!labelMap.containsKey(idStr2)) { // Edge labels are not unique, in contrast to vertex labels, thus we need to check whether it exists already
									labelMap.put(idStr2, Integer.toString(labelCounter));
									labelCounter++;
								}
								newE.getLabel().put(i, new StringBuilder(labelMap.get(idStr2)));
								edgeMap.put(idStr, newE);	
								graph.addEdge(newE, vertexMap.get(stmt.getSubject().toString()), newV, EdgeType.DIRECTED);
							}
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


		// construct BoW vectors for the Literals
		List<String> text = new ArrayList<String>();
		for (Vertex<Map<Integer,StringBuilder>> v : vertexLiteralStringMap.keySet()) {
			text.addAll(vertexLiteralStringMap.get(v).values());
		}
		Map<String, SparseVector> bowfv = computeTextFVs(text);
		for (Vertex<Map<Integer,StringBuilder>> v : vertexLiteralStringMap.keySet()) {
			vertexLiteralMap.put(v, new HashMap<Integer,SparseVector>());
			for (int i : vertexLiteralStringMap.get(v).keySet()) {
				vertexLiteralMap.get(v).put(i, bowfv.get(vertexLiteralStringMap.get(v).get(i)));
			}
		}

		// Remove edges for statements on the blackList
		for (Statement stmt : blackList) {
			graph.removeEdge(edgeMap.get(stmt.toString()));
		}
		return graph;
	}


	private void createInstanceIndexMaps(DirectedGraph<Vertex<Map<Integer,StringBuilder>>, Edge<Map<Integer,StringBuilder>>> graph, List<Resource> instances) {
		Vertex<Map<Integer, StringBuilder>> startV;
		List<Vertex<Map<Integer, StringBuilder>>> frontV, newFrontV;
		Map<Vertex<Map<Integer, StringBuilder>>, Integer> vertexIndexMap;
		Map<Edge<Map<Integer, StringBuilder>>, Integer> edgeIndexMap;

		for (int i = 0; i < instances.size(); i++) {				
			vertexIndexMap = new HashMap<Vertex<Map<Integer, StringBuilder>>, Integer>();
			edgeIndexMap   = new HashMap<Edge<Map<Integer, StringBuilder>>, Integer>();

			instanceVertexIndexMap.put(instances.get(i).toString(), vertexIndexMap);
			instanceEdgeIndexMap.put(instances.get(i).toString(), edgeIndexMap);


			// Get the start node
			startV = instanceVertices.get(instances.get(i).toString());
			frontV = new ArrayList<Vertex<Map<Integer,StringBuilder>>>();
			frontV.add(startV);

			// Process the start node
			vertexIndexMap.put(startV, depth);

			for (int j = depth - 1; j >= 0; j--) {
				newFrontV = new ArrayList<Vertex<Map<Integer,StringBuilder>>>();
				for (Vertex<Map<Integer, StringBuilder>> qV : frontV) {
					for (Edge<Map<Integer, StringBuilder>> edge : graph.getOutEdges(qV)) {
						// Process the edge, if we haven't seen it before
						if (!edgeIndexMap.containsKey(edge)) {
							edgeIndexMap.put(edge, j);
						}

						// Process the vertex if we haven't seen it before
						if (!vertexIndexMap.containsKey(graph.getDest(edge))) {
							vertexIndexMap.put(graph.getDest(edge), j);
						}

						// Add the vertex to the new front, if we go into a new round
						if (j > 0) {
							newFrontV.add(graph.getDest(edge));
						}
					}
				}
				frontV = newFrontV;
			}
		}		
	}



	private void relabelGraph2MultisetLabels(DirectedGraph<Vertex<Map<Integer,StringBuilder>>, Edge<Map<Integer,StringBuilder>>> graph, int startLabel) {
		Map<String, Bucket<VertexIndexPair>> bucketsV = new HashMap<String, Bucket<VertexIndexPair>>();
		Map<String, Bucket<EdgeIndexPair>> bucketsE   = new HashMap<String, Bucket<EdgeIndexPair>>();

		// Initialize buckets
		for (int i = startLabel; i < labelCounter; i++) {
			bucketsV.put(Integer.toString(i), new Bucket<VertexIndexPair>(Integer.toString(i)));
			bucketsE.put(Integer.toString(i), new Bucket<EdgeIndexPair>(Integer.toString(i)));
		}

		// 1. Fill buckets 
		// Add each edge source (i.e.) start vertex to the bucket of the edge label
		for (Edge<Map<Integer,StringBuilder>> edge : graph.getEdges()) {

			// for each label we add a vertex-index-pair to the bucket
			for (int index : edge.getLabel().keySet()) {
				Vertex<Map<Integer,StringBuilder>> v = graph.getDest(edge);				
				bucketsV.get(edge.getLabel().get(index).toString()).getContents().add(new VertexIndexPair(v,index));
			}
		}

		// Add each incident edge to the bucket of the node label
		for (Vertex<Map<Integer,StringBuilder>> vertex : graph.getVertices()) {			
			Collection<Edge<Map<Integer,StringBuilder>>> v2 = graph.getOutEdges(vertex);	

			for (int index : vertex.getLabel().keySet()) {
				if (index > 0) { // If index is 0 then we treat it as a fringe node, thus the label will not be propagated to the edges
					for (Edge<Map<Integer,StringBuilder>> e2 : v2) {
						if (e2.getLabel().containsKey(index-1)) {
							bucketsE.get(vertex.getLabel().get(index).toString()).getContents().add(new EdgeIndexPair(e2, index-1));
						}
					}
				}
			}
		}

		// 2. add bucket labels to existing labels
		// Change the original label to a prefix label

		for (Edge<Map<Integer,StringBuilder>> edge : graph.getEdges()) {
			for (int i : edge.getLabel().keySet()) {
				edge.getLabel().get(i).append("_");   
			}

		}
		for (Vertex<Map<Integer,StringBuilder>> vertex : graph.getVertices()) {
			for (int i : vertex.getLabel().keySet()) {
				vertex.getLabel().get(i).append("_"); 
			}
		}

		// 3. Relabel to the labels in the buckets
		for (int i = startLabel; i < labelCounter; i++) {
			// Process vertices
			Bucket<VertexIndexPair> bucketV = bucketsV.get(Integer.toString(i));			
			for (VertexIndexPair vp : bucketV.getContents()) {
				vp.getVertex().getLabel().get(vp.getIndex()).append(bucketV.getLabel());//      .put(vp.getIndex(), vp.getVertex().getLabel().get(vp.getIndex()) + bucketV.getLabel());
				vp.getVertex().getLabel().get(vp.getIndex()).append("_");
			}
			// Process edges
			Bucket<EdgeIndexPair> bucketE = bucketsE.get(Integer.toString(i));			
			for (EdgeIndexPair ep : bucketE.getContents()) {
				ep.getEdge().getLabel().get(ep.getIndex()).append(bucketE.getLabel());// .put(ep.getIndex(), ep.getEdge().getLabel().get(ep.getIndex()) + bucketE.getLabel());
				ep.getEdge().getLabel().get(ep.getIndex()).append("_");
			}
		}
	}


	private void compressGraphLabels(DirectedGraph<Vertex<Map<Integer,StringBuilder>>, Edge<Map<Integer,StringBuilder>>> graph) {
		String label;

		for (Edge<Map<Integer,StringBuilder>> edge : graph.getEdges()) {
			for (int i : edge.getLabel().keySet()) {
				label = labelMap.get(edge.getLabel().get(i).toString());						
				if (label == null) {					
					label = Integer.toString(labelCounter);
					labelCounter++;
					labelMap.put(edge.getLabel().get(i).toString(), label);				
				}

				//edge.getLabel().get(i).delete(0, edge.getLabel().get(i).length());
				//edge.getLabel().get(i).append(label);
				edge.getLabel().put(i, new StringBuilder(label));
			}
		}

		for (Vertex<Map<Integer,StringBuilder>> vertex : graph.getVertices()) {
			for (int i : vertex.getLabel().keySet()) {
				label = labelMap.get(vertex.getLabel().get(i).toString());
				if (label == null) {
					label = Integer.toString(labelCounter);
					labelCounter++;
					labelMap.put(vertex.getLabel().get(i).toString(), label);
				}
				//vertex.getLabel().get(i).delete(0, vertex.getLabel().get(i).length());
				//vertex.getLabel().get(i).append(label);
				vertex.getLabel().put(i, new StringBuilder(label));
			}
		}
	}



	/**
	 * The computation of the feature vectors assumes that each edge and vertex is only processed once. We can encounter the same
	 * vertex/edge on different depths during computation, this could lead to multiple counts of the same vertex, possibly of different
	 * depth labels.
	 * 
	 * @param graph
	 * @param instances
	 * @param weight
	 * @param featureVectors
	 */
	private void computeFVs(DirectedGraph<Vertex<Map<Integer,StringBuilder>>, Edge<Map<Integer,StringBuilder>>> graph, List<Resource> instances, double weight, SparseVector[] featureVectors, SparseVector[] textFV) {
		int index;
		Map<Vertex<Map<Integer,StringBuilder>>, Integer> vertexIndexMap;
		Map<Edge<Map<Integer,StringBuilder>>, Integer> edgeIndexMap;

		// Extra stuff for the text FV
		Map<Integer, Integer> labelFVIndexMap = new HashMap<Integer, Integer>();
		Map<Integer, Map<Integer, SparseVector>> indexFVMap = new HashMap<Integer, Map<Integer, SparseVector>>();

		for (int i = 0; i < instances.size(); i++) {
			featureVectors[i].setLastIndex(labelCounter - 1);

			vertexIndexMap = instanceVertexIndexMap.get(instances.get(i).toString());
			for (Vertex<Map<Integer,StringBuilder>> vertex : vertexIndexMap.keySet()) {
				index = Integer.parseInt(vertex.getLabel().get(vertexIndexMap.get(vertex)).toString());
				featureVectors[i].setValue(index, featureVectors[i].getValue(index) + weight);

				// If the vertex has a SparseVector at the depth of this instance
				if (vertexLiteralMap.containsKey(vertex) && vertexLiteralMap.get(vertex).containsKey(vertexIndexMap.get(vertex))) {			
					Integer key = labelFVIndexMap.get(index);
					if (key == null) {
						key = new Integer(labelFVIndexMap.size());
						labelFVIndexMap.put(index, key);
						indexFVMap.put(key, new HashMap<Integer, SparseVector>());					
					}
					if (indexFVMap.get(key).containsKey(i)) {
						indexFVMap.get(key).get(i).sumVector(vertexLiteralMap.get(vertex).get(vertexIndexMap.get(vertex)));
					} else {
						indexFVMap.get(key).put(i, new SparseVector(vertexLiteralMap.get(vertex).get(vertexIndexMap.get(vertex))));
					}		
				}
			}
			edgeIndexMap = instanceEdgeIndexMap.get(instances.get(i).toString());
			for (Edge<Map<Integer,StringBuilder>> edge : edgeIndexMap.keySet()) {
				index = Integer.parseInt(edge.getLabel().get(edgeIndexMap.get(edge)).toString());
				featureVectors[i].setValue(index, featureVectors[i].getValue(index) + weight);

			}
		}
		
		// Process the literal feature vectors
		for (int k : indexFVMap.keySet()) {
			int lastIdx = textFV[0].getLastIndex();

			if (indexFVMap.get(k).size() > 1) { // we only need to do stuff if more than 1 instance actually has a FV for this label k
				
				for (int k2 : indexFVMap.get(k).keySet()) {
					textFV[k2].addVector(indexFVMap.get(k).get(k2));
					lastIdx = textFV[k2].getLastIndex();
				}
				// Set all feature vectors to the new lastIdx
				for (SparseVector fv : textFV) {
					fv.setLastIndex(lastIdx);
				}
			}
		}		
	}


	private void computeKernelMatrix(List<Resource> instances, SparseVector[] featureVectors, double[][] kernel) {
		for (int i = 0; i < instances.size(); i++) {
			for (int j = i; j < instances.size(); j++) {
				kernel[i][j] += featureVectors[i].dot(featureVectors[j]);
				kernel[j][i] = kernel[i][j];
			}
		}
	}

	private void setBlankLabels(DirectedGraph<Vertex<Map<Integer,StringBuilder>>, Edge<Map<Integer,StringBuilder>>> graph) {
		for (Vertex<Map<Integer,StringBuilder>> v : graph.getVertices()) {
			for (int k : v.getLabel().keySet()) {
				v.getLabel().put(k, new StringBuilder(BLANK_VERTEX_LABEL));
			}
		}

		for (Edge<Map<Integer,StringBuilder>> e : graph.getEdges()) {
			for (int k : e.getLabel().keySet()) {
				e.getLabel().put(k, new StringBuilder(BLANK_EDGE_LABEL));
			}
		}	
	}

	private Map<String, SparseVector> computeTextFVs(List<String> texts) {
		Map<String,SparseVector> map = new HashMap<String,SparseVector>();
		List<SparseVector> fv = TextUtils.computeTF(texts);

		for (int i = 0; i < texts.size(); i++) {
			map.put(texts.get(i), fv.get(i));
		}
		return map;
	}



	private class VertexIndexPair {
		private Vertex<Map<Integer,StringBuilder>> vertex;
		private int index;

		public VertexIndexPair(Vertex<Map<Integer, StringBuilder>> vertex, int index) {
			this.vertex = vertex;
			this.index = index;
		}

		public Vertex<Map<Integer, StringBuilder>> getVertex() {
			return vertex;
		}

		public int getIndex() {
			return index;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof VertexIndexPair) {
				return ((VertexIndexPair) obj).vertex == vertex && ((VertexIndexPair) obj).index == index;
			}
			return super.equals(obj);
		}	


	}

	private class EdgeIndexPair {
		private Edge<Map<Integer,StringBuilder>> edge;
		private int index;

		public EdgeIndexPair(Edge<Map<Integer, StringBuilder>> edge, int index) {
			this.edge = edge;
			this.index = index;
		}

		public Edge<Map<Integer, StringBuilder>> getEdge() {
			return edge;
		}

		public int getIndex() {
			return index;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof EdgeIndexPair) {
				return ((EdgeIndexPair) obj).edge == edge && ((EdgeIndexPair) obj).index == index;
			}
			return super.equals(obj);
		}
	}

}
