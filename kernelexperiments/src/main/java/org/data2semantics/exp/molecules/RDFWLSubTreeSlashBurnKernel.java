package org.data2semantics.exp.molecules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.data2semantics.proppred.kernels.Bucket;
import org.data2semantics.proppred.kernels.KernelUtils;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFFeatureVectorKernel;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFGraphKernel;
import org.data2semantics.proppred.learners.SparseVector;
import org.data2semantics.tools.rdf.RDFDataSet;
import org.nodes.DTGraph;
import org.nodes.DTLink;
import org.nodes.DTNode;
import org.nodes.MapDTGraph;
import org.nodes.algorithms.SlashBurn;
import org.nodes.util.Functions.Dir;
import org.nodes.util.Pair;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;



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
public class RDFWLSubTreeSlashBurnKernel implements RDFGraphKernel, RDFFeatureVectorKernel {
	private static final String ROOT_LABEL = "1";
	private static final String BLANK_VERTEX_LABEL = "1";
	private static final String BLANK_EDGE_LABEL   = "2";

	private Map<String, String> labelMap;
	private Map<String, DTNode<MapLabel,MapLabel>> instanceVertices;
	private Map<String, Map<DTNode<MapLabel,MapLabel>, Integer>> instanceVertexIndexMap;
	private Map<String, Map<DTLink<MapLabel,MapLabel>, Integer>> instanceEdgeIndexMap;
	private Map<String, Integer> hubMap;

	private int labelCounter;
	private int startLabel;
	private int depth;
	private int iterations;
	private boolean inference;
	private String label;
	private boolean normalize;
	private boolean ignoreLiterals;
	private boolean reverse;

	private boolean relabel;


	public RDFWLSubTreeSlashBurnKernel(int iterations, int depth, boolean inference, boolean normalize, boolean reverse) {
		this(iterations, depth, inference, normalize);
		this.reverse = reverse;
	}


	public RDFWLSubTreeSlashBurnKernel(int iterations, int depth, boolean inference, boolean normalize) {
		this.normalize = normalize;
		this.label = "RDF_WL_Kernel_" + depth + "_" + iterations;
		this.ignoreLiterals = false;
		this.reverse = false;

		labelMap = new HashMap<String, String>();
		instanceVertices = new HashMap<String, DTNode<MapLabel,MapLabel>>();
		this.instanceVertexIndexMap = new HashMap<String, Map<DTNode<MapLabel,MapLabel>, Integer>>();
		this.instanceEdgeIndexMap = new HashMap<String, Map<DTLink<MapLabel,MapLabel>, Integer>>();

		this.hubMap = new HashMap<String,Integer>();
		this.relabel = true;

		startLabel = 1; // start at 1, since featureVectors need to start at index 1
		labelCounter = 2;

		this.depth = depth;
		this.inference = inference;
		this.iterations = iterations;
	}


	public RDFWLSubTreeSlashBurnKernel() {
		this(2, 4, false, true);
	}

	public String getLabel() {
		return label;
	}

	public void setNormalize(boolean normalize) {
		this.normalize = normalize;
	}


	public void setHubMap(Map<String,Integer> hm) {
		hubMap = hm;
	}

	public void setIgnoreLiterals(boolean ignore) {
		ignoreLiterals = ignore;
	}

	public void setReverse(boolean reverse) {
		this.reverse = reverse;
	}

	public void setRelabel(boolean re) {
		this.relabel = re;
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
		for (int i = 0; i < featureVectors.length; i++) {
			featureVectors[i] = new SparseVector();
		}	

		DTGraph<MapLabel,MapLabel> graph = createGraphFromRDF(dataset, instances, new HashSet<Statement>(blackList));
		// separate method to remove root labels, because slash burn will also do this in a different way, we want to play with this.
		//removeRootLabels(instances);


		createInstanceIndexMaps(graph, instances);


		computeFVs(graph, instances, Math.sqrt(1.0 / ((double) (iterations + 1))), featureVectors);

		for (int i = 0; i < iterations; i++) {	
			relabelGraph2MultisetLabels(graph);
			startLabel = labelCounter;
			compressGraphLabels(graph);
			computeFVs(graph, instances, Math.sqrt((2.0 + i) / ((double) (iterations + 1))), featureVectors);
		}
		if (this.normalize) {
			featureVectors = KernelUtils.normalize(featureVectors);
		}

		return featureVectors;
	}

	public double[][] compute(RDFDataSet dataset, List<Resource> instances, List<Statement> blackList) {
		SparseVector[] featureVectors = computeFeatureVectors(dataset, instances, blackList);
		double[][] kernel = KernelUtils.initMatrix(instances.size(), instances.size());
		computeKernelMatrix(instances, featureVectors, kernel);
		return kernel;
	}


	private DTGraph<MapLabel,MapLabel> createGraphFromRDF(RDFDataSet dataset, List<Resource> instances, Set<Statement> blackList) {
		Map<String, DTNode<MapLabel,MapLabel>> literalMap = new HashMap<String, DTNode<MapLabel,MapLabel>>();
		Map<String, DTNode<MapLabel,MapLabel>> vertexMap = new HashMap<String, DTNode<MapLabel,MapLabel>>();
		Map<String, DTLink<MapLabel,MapLabel>> edgeMap = new HashMap<String, DTLink<MapLabel,MapLabel>>();
		Map<DTNode<MapLabel,MapLabel>, Integer> rewriteMap = new HashMap<DTNode<MapLabel,MapLabel>, Integer>();

		DTGraph<MapLabel,MapLabel> graph = new MapDTGraph<MapLabel,MapLabel>();

		List<Resource> queryNodes = new ArrayList<Resource>();
		List<Resource> newQueryNodes;
		List<Statement> result;

		DTNode<MapLabel,MapLabel> startV;
		DTNode<MapLabel,MapLabel> newV;
		DTLink<MapLabel,MapLabel> newE;

		String idStr, idStr2;

		for (Resource instance : instances) {
			idStr = instance.toString();
			// If the instance is already part of the graph (because it was retrieved for an earlier instance), then we use that one
			if (vertexMap.containsKey(idStr)) {
				startV = vertexMap.get(idStr);			
			} else { // Else we construct a new node for the instance
				startV = graph.add(new MapLabel());								
				vertexMap.put(idStr, startV);
				labelMap.put(idStr, Integer.toString(labelCounter));
				labelCounter++;
			}
			startV.label().put(depth, new StringBuilder(labelMap.get(idStr))); 
			instanceVertices.put(idStr, startV); // So that we can reconstruct subgraphs later, we save the instance vertices

			queryNodes.add(instance);

			for (int i = depth - 1; i >= 0; i--) {
				newQueryNodes = new ArrayList<Resource>();

				for (Resource queryNode : queryNodes) {
					result = dataset.getStatements(queryNode, null, null, inference);

					for (Statement stmt : result) {
						if (!blackList.contains(stmt)) { // relation should not be in the blacklist
							newV = null;

							// check for In link Hub
							idStr = stmt.getPredicate().toString() + stmt.getObject().toString();
							if (hubMap.containsKey(idStr)) {
								if (relabel) {
									DTNode<MapLabel,MapLabel> sn = vertexMap.get(stmt.getSubject().toString());
									if (rewriteMap.get(sn) == null || rewriteMap.get(sn) < hubMap.get(idStr)) { // hub is non-existent or on deeper level, then change label
										if (!labelMap.containsKey(idStr)) {
											labelMap.put(idStr, Integer.toString(labelCounter));
											labelCounter++;
										}
										sn.label().put(i+1, new StringBuilder(labelMap.get(idStr)));
									} 
								}
							} else { // if not an IN link hub, then we can at least add the object as a regular vertex

								if (stmt.getObject() instanceof Literal) { // literal
									if (!ignoreLiterals) { // do nothing if we ignore literals, if's are nested, because we do not want to go to the non-literal stage
										idStr = stmt.toString();
										idStr2 = stmt.getObject().toString();

										if (literalMap.containsKey(idStr)) {
											newV = literalMap.get(idStr);				 
											newV.label().put(i, new StringBuilder(labelMap.get(idStr2))); // Set the label for depth i to the already existing label for this vertex
										} else {
											newV = graph.add(new MapLabel());
											if (!labelMap.containsKey(idStr2)) { 
												labelMap.put(idStr2, Integer.toString(labelCounter));
												labelCounter++;
											}
											newV.label().put(i, new StringBuilder(labelMap.get(idStr2)));
											literalMap.put(idStr, newV);
										}
									}
								} else { // Non-literal
									idStr = stmt.getObject().toString();
									if (vertexMap.containsKey(idStr)) { // existing vertex
										newV = vertexMap.get(idStr);				 
										newV.label().put(i, new StringBuilder(labelMap.get(idStr))); // Set the label for depth i to the already existing label for this vertex
									} else { // New vertex
										newV = graph.add(new MapLabel());
										labelMap.put(idStr, Integer.toString(labelCounter));
										newV.label().put(i, new StringBuilder(Integer.toString(labelCounter)));
										labelCounter++;
										vertexMap.put(idStr, newV);
									}
								}

								// Check for Out link hub
								idStr = stmt.getSubject().toString() + stmt.getPredicate().toString();
								if (hubMap.containsKey(idStr)) {
									if (relabel) {
										if (rewriteMap.get(newV) == null || rewriteMap.get(newV) < hubMap.get(idStr)) { // hub is non-existent or on deeper level, then change label
											if (!labelMap.containsKey(idStr)) {
												labelMap.put(idStr, Integer.toString(labelCounter));
												labelCounter++;
											}
											newV.label().put(i, new StringBuilder(labelMap.get(idStr)));

											//System.out.println("removed OUT hub: " + idStr);
										}
									}
								} else { // if not an Out link hub, then we can also add the edge

									// Process new Edge
									if (newV != null) { // need a check, because vertex might have been an ignored literal
										idStr = stmt.toString();
										idStr2 = stmt.getPredicate().toString();
										if (edgeMap.containsKey(idStr)) { // existing edge
											newE = edgeMap.get(idStr);
											newE.tag().put(i, new StringBuilder(labelMap.get(idStr2))); // Set the label for depth i to the already existing label for this edge

										} else { // new edge
											if (!labelMap.containsKey(idStr2)) { // Edge labels are not unique, in contrast to vertex labels, thus we need to check whether it exists already
												labelMap.put(idStr2, Integer.toString(labelCounter));
												labelCounter++;
											}
											newE = vertexMap.get(stmt.getSubject().toString()).connect(newV, new MapLabel());
											newE.tag().put(i, new StringBuilder(labelMap.get(idStr2)));
											edgeMap.put(idStr, newE);	
										}
									}
								}


								// Store the object nodes if the loop continues (i>0) and if its a Resource
								if (i > 0 && stmt.getObject() instanceof Resource) {
									newQueryNodes.add((Resource) stmt.getObject());
								}
							}

						}
					}
				}

				queryNodes = newQueryNodes;
			}		
		}

		return graph;
	}

	private void removeRootLabels(List<Resource> instances) {
		for (Resource inst : instances) {
			for (int key : instanceVertices.get(inst.toString()).label().keySet()) {
				labelMap.put(instanceVertices.get(inst.toString()).label().get(key).toString(), ROOT_LABEL);
				instanceVertices.get(inst.toString()).label().put(key, new StringBuilder(ROOT_LABEL));
			}
		}
	}

	private void removeHubs(DTGraph<MapLabel,MapLabel> graph, List<Resource> instances) {
		//
	}


	private void createInstanceIndexMaps(DTGraph<MapLabel,MapLabel> graph, List<Resource> instances) {
		DTNode<MapLabel,MapLabel> startV;
		List<DTNode<MapLabel,MapLabel>> frontV, newFrontV;
		Map<DTNode<MapLabel,MapLabel>, Integer> vertexIndexMap;
		Map<DTLink<MapLabel,MapLabel>, Integer> edgeIndexMap;

		for (int i = 0; i < instances.size(); i++) {				
			vertexIndexMap = new HashMap<DTNode<MapLabel,MapLabel>, Integer>();
			edgeIndexMap   = new HashMap<DTLink<MapLabel,MapLabel>, Integer>();

			instanceVertexIndexMap.put(instances.get(i).toString(), vertexIndexMap);
			instanceEdgeIndexMap.put(instances.get(i).toString(), edgeIndexMap);


			// Get the start node
			startV = instanceVertices.get(instances.get(i).toString());
			frontV = new ArrayList<DTNode<MapLabel,MapLabel>>();
			frontV.add(startV);

			// Process the start node
			vertexIndexMap.put(startV, depth);

			for (int j = depth - 1; j >= 0; j--) {
				newFrontV = new ArrayList<DTNode<MapLabel,MapLabel>>();
				for (DTNode<MapLabel,MapLabel> qV : frontV) {
					for (DTLink<MapLabel,MapLabel> edge : qV.linksOut()) {
						// Process the edge, if we haven't seen it before
						if (!edgeIndexMap.containsKey(edge)) {
							edgeIndexMap.put(edge, j);
						}

						// Process the vertex if we haven't seen it before
						if (!vertexIndexMap.containsKey(edge.to())) {
							vertexIndexMap.put(edge.to(), j);
						}

						// Add the vertex to the new front, if we go into a new round
						if (j > 0) {
							newFrontV.add(edge.to());
						}
					}
				}
				frontV = newFrontV;
			}
		}		
	}



	private void relabelGraph2MultisetLabels(DTGraph<MapLabel,MapLabel> graph) {
		Map<String, Bucket<VertexIndexPair>> bucketsV = new HashMap<String, Bucket<VertexIndexPair>>();
		Map<String, Bucket<EdgeIndexPair>> bucketsE   = new HashMap<String, Bucket<EdgeIndexPair>>();

		// Initialize buckets
		for (int i = startLabel; i < labelCounter; i++) {
			bucketsV.put(Integer.toString(i), new Bucket<VertexIndexPair>(Integer.toString(i)));
			bucketsE.put(Integer.toString(i), new Bucket<EdgeIndexPair>(Integer.toString(i)));
		}

		// 1. Fill buckets 
		if (reverse) { // Labels "travel" to the root node

			// Add each edge source (i.e.) start vertex to the bucket of the edge label
			for (DTLink<MapLabel,MapLabel> edge : graph.links()) {
				// for each label we add a vertex-index-pair to the bucket
				for (int index : edge.tag().keySet()) {
					bucketsV.get(edge.tag().get(index).toString()).getContents().add(new VertexIndexPair(edge.from(), index + 1));
				}
			}

			// Add each incident edge to the bucket of the node label
			for (DTNode<MapLabel,MapLabel> vertex : graph.nodes()) {			
				for (int index : vertex.label().keySet()) {
					for (DTLink<MapLabel,MapLabel> e2 : vertex.linksIn()) {
						if (e2.tag().containsKey(index)) {
							bucketsE.get(vertex.label().get(index).toString()).getContents().add(new EdgeIndexPair(e2, index));
						}
					}
				}
			}

		} else { // Labels "travel" to the fringe nodes

			// Add each edge source (i.e.) start vertex to the bucket of the edge label
			for (DTLink<MapLabel,MapLabel> edge : graph.links()) {
				// for each label we add a vertex-index-pair to the bucket
				for (int index : edge.tag().keySet()) {
					bucketsV.get(edge.tag().get(index).toString()).getContents().add(new VertexIndexPair(edge.to(), index));
				}
			}

			// Add each incident edge to the bucket of the node label
			for (DTNode<MapLabel,MapLabel> vertex : graph.nodes()) {			
				for (int index : vertex.label().keySet()) {
					if (index > 0) { // If index is 0 then we treat it as a fringe node, thus the label will not be propagated to the edges
						for (DTLink<MapLabel,MapLabel> e2 : vertex.linksOut()) {
							bucketsE.get(vertex.label().get(index).toString()).getContents().add(new EdgeIndexPair(e2, index - 1));
						}
					}
				}
			}
		}

		// 2. add bucket labels to existing labels
		// Change the original label to a prefix label

		for (DTLink<MapLabel,MapLabel> edge : graph.links()) {
			for (int i : edge.tag().keySet()) {
				edge.tag().get(i).append("_");   
			}

		}
		for (DTNode<MapLabel,MapLabel> vertex : graph.nodes()) {
			for (int i : vertex.label().keySet()) {
				vertex.label().get(i).append("_"); 
			}
		}

		// 3. Relabel to the labels in the buckets
		for (int i = startLabel; i < labelCounter; i++) {
			// Process vertices
			Bucket<VertexIndexPair> bucketV = bucketsV.get(Integer.toString(i));			
			for (VertexIndexPair vp : bucketV.getContents()) {
				vp.getVertex().label().get(vp.getIndex()).append(bucketV.getLabel());
				vp.getVertex().label().get(vp.getIndex()).append("_");
			}
			// Process edges
			Bucket<EdgeIndexPair> bucketE = bucketsE.get(Integer.toString(i));			
			for (EdgeIndexPair ep : bucketE.getContents()) {
				ep.getEdge().tag().get(ep.getIndex()).append(bucketE.getLabel());
				ep.getEdge().tag().get(ep.getIndex()).append("_");
			}
		}
	}


	private void compressGraphLabels(DTGraph<MapLabel,MapLabel> graph) {
		String label;

		for (DTLink<MapLabel,MapLabel> edge : graph.links()) {
			for (int i : edge.tag().keySet()) {
				label = labelMap.get(edge.tag().get(i).toString());						
				if (label == null) {					
					label = Integer.toString(labelCounter);
					labelCounter++;
					labelMap.put(edge.tag().get(i).toString(), label);				
				}
				edge.tag().put(i, new StringBuilder(label));
			}
		}

		for (DTNode<MapLabel,MapLabel> vertex : graph.nodes()) {
			for (int i : vertex.label().keySet()) {
				label = labelMap.get(vertex.label().get(i).toString());
				if (label == null) {
					label = Integer.toString(labelCounter);
					labelCounter++;
					labelMap.put(vertex.label().get(i).toString(), label);
				}
				vertex.label().put(i, new StringBuilder(label));
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
	private void computeFVs(DTGraph<MapLabel,MapLabel> graph, List<Resource> instances, double weight, SparseVector[] featureVectors) {
		int index;
		Map<DTNode<MapLabel,MapLabel>, Integer> vertexIndexMap;
		Map<DTLink<MapLabel,MapLabel>, Integer> edgeIndexMap;

		for (int i = 0; i < instances.size(); i++) {
			featureVectors[i].setLastIndex(labelCounter - 1);

			vertexIndexMap = instanceVertexIndexMap.get(instances.get(i).toString());
			for (DTNode<MapLabel,MapLabel> vertex : vertexIndexMap.keySet()) {
				index = Integer.parseInt(vertex.label().get(vertexIndexMap.get(vertex)).toString());
				featureVectors[i].setValue(index, featureVectors[i].getValue(index) + weight);
			}
			edgeIndexMap = instanceEdgeIndexMap.get(instances.get(i).toString());
			for (DTLink<MapLabel,MapLabel> edge : edgeIndexMap.keySet()) {
				index = Integer.parseInt(edge.tag().get(edgeIndexMap.get(edge)).toString());
				featureVectors[i].setValue(index, featureVectors[i].getValue(index) + weight);
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

	private void setBlankLabels(DTGraph<MapLabel,MapLabel> graph) {
		for (DTNode<MapLabel,MapLabel> v : graph.nodes()) {
			for (int k : v.label().keySet()) {
				v.label().put(k, new StringBuilder(BLANK_VERTEX_LABEL));
			}
		}

		for (DTLink<MapLabel,MapLabel> e : graph.links()) {
			for (int k : e.tag().keySet()) {
				e.tag().put(k, new StringBuilder(BLANK_EDGE_LABEL));
			}
		}	
	}


	private DTGraph<String,String> copy2StringLabeledGraph(DTGraph<MapLabel,MapLabel> graph) {
		DTGraph<String,String> newGraph = new MapDTGraph<String,String>();

		for (DTNode<MapLabel,MapLabel> node : graph.nodes()) {
			String lab = "";
			for (int k : node.label().keySet()) { // any node label will do, we take the last.
				lab = node.label().get(k).toString();
			}			
			newGraph.add(lab);
		}

		for (DTLink<MapLabel,MapLabel> link : graph.links()) {
			String lab = "";
			for (int k : link.tag().keySet()){
				lab = link.tag().get(k).toString();
			}
			newGraph.nodes().get(link.from().index()).connect(newGraph.nodes().get(link.to().index()), lab);
		}
		return newGraph;
	}


	private class VertexIndexPair {
		private DTNode<MapLabel,MapLabel> vertex;
		private int index;

		public VertexIndexPair(DTNode<MapLabel,MapLabel> vertex, int index) {
			this.vertex = vertex;
			this.index = index;
		}

		public DTNode<MapLabel,MapLabel> getVertex() {
			return vertex;
		}
		public int getIndex() {
			return index;
		}		
	}

	private class EdgeIndexPair {
		private DTLink<MapLabel,MapLabel> edge;
		private int index;

		public EdgeIndexPair(DTLink<MapLabel,MapLabel> edge, int index) {
			this.edge = edge;
			this.index = index;
		}

		public DTLink<MapLabel,MapLabel> getEdge() {
			return edge;
		}
		public int getIndex() {
			return index;
		}		
	}

}
