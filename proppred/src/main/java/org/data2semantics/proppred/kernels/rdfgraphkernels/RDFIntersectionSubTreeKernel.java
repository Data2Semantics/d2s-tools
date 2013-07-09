package org.data2semantics.proppred.kernels.rdfgraphkernels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.data2semantics.proppred.kernels.KernelUtils;
import org.data2semantics.tools.graphs.Edge;
import org.data2semantics.tools.graphs.Vertex;
import org.data2semantics.tools.rdf.RDFDataSet;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;

import edu.uci.ics.jung.graph.DelegateTree;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Tree;
import edu.uci.ics.jung.graph.util.EdgeType;

public class RDFIntersectionSubTreeKernel implements RDFGraphKernel {
	private static final int BLANK_VERTEX_LABEL = 1337;
	private static final int BLANK_EDGE_LABEL   = 1338;
	
	private Map<String, Integer> labelMap;
	private Map<String, Vertex<Integer>> instanceVertices;
	private int labelCounter;

	private int depth;
	private boolean inference;
	private double discountFactor;
	private boolean blankLabels;
	protected String label;
	protected boolean normalize;

	public RDFIntersectionSubTreeKernel() {
		this(2, 1, false, true);
	}

	public RDFIntersectionSubTreeKernel(int depth, double discountFactor, boolean inference, boolean normalize, boolean blankLabels) {
		this(depth, discountFactor, inference, normalize);
		this.blankLabels = blankLabels;
	}
	
	
	public RDFIntersectionSubTreeKernel(int depth, double discountFactor, boolean inference, boolean normalize) {
		this.normalize = normalize;
		this.label = "RDF Intersection SubTree Kernel";
		this.blankLabels = false;
		
		labelMap = new HashMap<String, Integer>();
		instanceVertices = new HashMap<String, Vertex<Integer>>();
		labelCounter = 1;
		this.depth = depth;
		this.inference = inference;
		this.discountFactor = discountFactor;
	}

	
	public String getLabel() {
		return label;
	}

	public void setNormalize(boolean normalize) {
		this.normalize = normalize;
	}

	public double[][] compute(RDFDataSet dataset, List<Resource> instances, List<Statement> blackList) {
		double[][] kernel = KernelUtils.initMatrix(instances.size(), instances.size());
		Tree<Vertex<Integer>, Edge<Integer>> tree;
		
		DirectedGraph<Vertex<Integer>,Edge<Integer>> graph = createGraphFromRDF(dataset, instances, blackList);
		
		if (blankLabels) {
			setBlankLabels(graph);
		}

		for (int i = 0; i < instances.size(); i++) {
			for (int j = i; j < instances.size(); j++) {
				tree = computeIntersectionTree(graph, instanceVertices.get(instances.get(i).toString()), instanceVertices.get(instances.get(j).toString()));
				kernel[i][j] = subTreeScore(tree, tree.getRoot(), discountFactor);
				kernel[j][i] = kernel[i][j];
			}
		}

		if (normalize) {
			return KernelUtils.normalize(kernel);
		} else {		
			return kernel;
		}
	}


	private DirectedGraph<Vertex<Integer>,Edge<Integer>> createGraphFromRDF(RDFDataSet dataset, List<Resource> instances, List<Statement> blackList) {
		Map<String, Vertex<Integer>> vertexMap = new HashMap<String, Vertex<Integer>>();
		Map<String, Edge<Integer>>   edgeMap   = new HashMap<String, Edge<Integer>>();

		DirectedGraph<Vertex<Integer>,Edge<Integer>> graph = new DirectedSparseMultigraph<Vertex<Integer>,Edge<Integer>>();

		List<Resource> queryNodes = new ArrayList<Resource>();
		List<Resource> newQueryNodes;
		List<Statement> result;

		Vertex<Integer> startV;
		Vertex<Integer> newV;
		Edge<Integer> newE;

		String idStr, idStr2;

		for (Resource instance : instances) {
			idStr = instance.toString();			

			if (vertexMap.containsKey(idStr)) {
				startV = vertexMap.get(idStr);
			} else {
				labelMap.put(instance.toString(), labelCounter);
				startV = new Vertex<Integer>(labelCounter);
				labelCounter++;

				vertexMap.put(idStr, startV);
				graph.addVertex(startV);			
			}
			instanceVertices.put(idStr, startV); 

			queryNodes.add(instance);

			for (int i = depth - 1; i >= 0; i--) {
				newQueryNodes = new ArrayList<Resource>();

				for (Resource queryNode : queryNodes) {
					result = dataset.getStatements(queryNode, null, null, inference);

					for (Statement stmt : result) {

						// Process new vertex
						idStr = stmt.getObject().toString();
						if (vertexMap.containsKey(idStr)) { // existing vertex
							newV = vertexMap.get(idStr);				 	
						} else { // New vertex
							labelMap.put(idStr, labelCounter);
							newV = new Vertex<Integer>(labelCounter);
							labelCounter++;

							vertexMap.put(idStr, newV);
							graph.addVertex(newV);
						}

						// Process new Edge
						idStr = stmt.toString();
						idStr2 = stmt.getPredicate().toString();
						if (edgeMap.containsKey(idStr)) { // existing edge
							newE = edgeMap.get(idStr);

						} else { // new edge
							if (!labelMap.containsKey(idStr2)) {
								labelMap.put(idStr2, labelCounter);
								labelCounter++;
							}

							newE = new Edge<Integer>(labelMap.get(idStr2));
							edgeMap.put(idStr, newE);
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
		return graph;
	}


	private Tree<Vertex<Integer>, Edge<Integer>> computeIntersectionTree(DirectedGraph<Vertex<Integer>,Edge<Integer>> graph, Vertex<Integer> rootA, Vertex<Integer> rootB) {
		Tree<Vertex<Integer>, Edge<Integer>> iTree = new DelegateTree<Vertex<Integer>, Edge<Integer>>();

		// Search front is a map, because we are making a graph expansion, i.e. the same node can occur multiple times in a tree, this we cannot use
		// the nodes directly, and we need to possible store multiple references of the same node, hence the utility vertex tracker class.
		Map<VertexTracker, Vertex<Integer>> searchFront = new HashMap<VertexTracker, Vertex<Integer>>();
		Map<VertexTracker, Vertex<Integer>> newSearchFront, newSearchFrontPartial;
		int vtCount = 1;

		List<Vertex<Integer>> commonChilds = getCommonChilds(graph, rootA, rootB);

		VertexTracker newRoot = new VertexTracker(new Vertex<Integer>(0), vtCount++); // 0 is the special root label :)
		searchFront.put(newRoot, new Vertex<Integer>(0));
		iTree.addVertex(searchFront.get(newRoot));

		for (int i = 0; i < depth; i++) {
			newSearchFront = new HashMap<VertexTracker, Vertex<Integer>>();

			for (VertexTracker vt : searchFront.keySet()) {
				newSearchFrontPartial = new HashMap<VertexTracker, Vertex<Integer>>();

				if (vt.getVertex().getLabel() == 0) { // root nodes
					for (Vertex<Integer> v : commonChilds) {
						newSearchFrontPartial.put(new VertexTracker(v, vtCount++), new Vertex<Integer>(v.getLabel()));					 
					}

				} else {
					for (Edge<Integer> edge : graph.getOutEdges(vt.getVertex())) {
						if (graph.getDest(edge) == rootA || graph.getDest(edge) == rootB) { // if we find a root node
							newSearchFrontPartial.put(new VertexTracker(new Vertex<Integer>(0), vtCount++), new Vertex<Integer>(0));
						} else {
							newSearchFrontPartial.put(new VertexTracker(graph.getDest(edge),vtCount++), new Vertex<Integer>(graph.getDest(edge).getLabel()));
						}
					}
				}			

				for (VertexTracker vt2 : newSearchFrontPartial.keySet()) {
					iTree.addEdge(new Edge<Integer>(1234), searchFront.get(vt), newSearchFrontPartial.get(vt2), EdgeType.DIRECTED);
				}
				newSearchFront.putAll(newSearchFrontPartial);
			}
			searchFront = newSearchFront;
		}
		return iTree;
	}

	private List<Vertex<Integer>> getCommonChilds(DirectedGraph<Vertex<Integer>,Edge<Integer>> graph, Vertex<Integer> rootA, Vertex<Integer> rootB) {
		List<Vertex<Integer>> commonChilds = new ArrayList<Vertex<Integer>>();

		Set<Pair> childsA = new TreeSet<Pair>();
		Set<Pair> childsB = new TreeSet<Pair>();
		Map<Pair, Vertex<Integer>> pairMap = new TreeMap<Pair, Vertex<Integer>>();
		Pair pair;

		for (Edge<Integer> edge : graph.getOutEdges(rootA)) {
			pair = new Pair(edge.getLabel(), graph.getDest(edge).getLabel());
			childsA.add(pair);
			pairMap.put(pair, graph.getDest(edge));
		}

		for (Edge<Integer> edge : graph.getOutEdges(rootB)) {
			pair = new Pair(edge.getLabel(), graph.getDest(edge).getLabel());
			childsB.add(pair);
			pairMap.put(pair, graph.getDest(edge));
		}

		// If root nodes have an equivalence like relation
		for (Pair childA : childsA) {
			if (childA.getSecond() == rootB.getLabel() && childsB.contains(new Pair(childA.getFirst(), rootA.getLabel()))) {
				commonChilds.add(new Vertex<Integer>(0));
			}
		}

		childsA.retainAll(childsB); // intersect the sets

		for (Pair common : childsA) {
			commonChilds.add(pairMap.get(common));
		}
		return commonChilds;
	}

	protected double subTreeScore(Tree<Vertex<Integer>, Edge<Integer>> tree, Vertex<Integer> currentVertex, double discountFactor) {
		// Base case of recursion
		if (tree.getSuccessors(currentVertex).isEmpty()) {
			return 1.0;
		} else { // recursive case
			double score = 0;
			for (Vertex<Integer> leaf: tree.getSuccessors(currentVertex)) {
				score += subTreeScore(tree, leaf, discountFactor);
			}
			return 1 + (discountFactor * score);
		}
	}
	
	
	private void setBlankLabels(DirectedGraph<Vertex<Integer>, Edge<Integer>> graph) {
		for (Vertex<Integer> v : graph.getVertices()) {
			v.setLabel(BLANK_VERTEX_LABEL);
		}
		
		for (Edge<Integer> e : graph.getEdges()) {
			e.setLabel(BLANK_EDGE_LABEL);
		}	
	}
	

	class VertexTracker {
		Vertex<Integer> vertex;
		int count;

		public VertexTracker(Vertex<Integer> vertex, int count) {
			super();
			this.vertex = vertex;
			this.count = count;
		}

		public Vertex<Integer> getVertex() {
			return vertex;
		}

		public void setVertex(Vertex<Integer> vertex) {
			this.vertex = vertex;
		}

		public int getCount() {
			return count;
		}

		public void setCount(int count) {
			this.count = count;
		}


	}


	class Pair implements Comparable<Pair> {
		int first;
		int second;

		public Pair(int first, int second) {
			this.first = first;
			this.second = second;
		}

		public int getFirst() {
			return first;
		}

		public int getSecond() {
			return second;
		}

		public boolean equals(Pair comp) {
			return (first == comp.getFirst() && second == comp.getSecond());
		}

		public int compareTo(Pair comp) {
			if (first == comp.getFirst()) {
				return second - comp.getSecond();
			} else {
				return first - comp.getFirst();
			}
		}

		public String toString() {
			return "(" + first + "," + second + ")";
		}
	}



}
