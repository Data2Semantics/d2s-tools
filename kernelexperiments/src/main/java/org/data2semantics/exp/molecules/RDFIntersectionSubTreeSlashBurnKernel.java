package org.data2semantics.exp.molecules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.data2semantics.proppred.kernels.KernelUtils;
import org.data2semantics.proppred.kernels.rdfgraphkernels.RDFGraphKernel;
import org.data2semantics.tools.graphs.Edge;
import org.data2semantics.tools.graphs.Vertex;
import org.data2semantics.tools.rdf.RDFDataSet;
import org.nodes.DTGraph;
import org.nodes.DTLink;
import org.nodes.DTNode;
import org.nodes.MapDTGraph;
import org.nodes.algorithms.SlashBurn;
import org.nodes.util.Pair;
import org.nodes.util.Functions.Dir;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;

import edu.uci.ics.jung.graph.DelegateTree;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Tree;
import edu.uci.ics.jung.graph.util.EdgeType;

/**
 * Implementation of the Intersection SubTree kernel directly on the RDF graph, as suggested in the original paper.
 * 
 * @author Gerben
 *
 */
public class RDFIntersectionSubTreeSlashBurnKernel implements RDFGraphKernel {
	private static final int BLANK_VERTEX_LABEL = 1337;
	private static final int BLANK_EDGE_LABEL   = 1338;

	private Map<String, Integer> labelMap;
	private Map<Integer, String> invLabelMap;
	private Map<String, DTNode<StringLabel,StringLabel>> instanceVertices;
	private int labelCounter;

	private int depth;
	private boolean inference;
	private double discountFactor;
	protected String label;
	protected boolean normalize;

	private int hubThreshold;

	public RDFIntersectionSubTreeSlashBurnKernel() {
		this(2, 1, false, true);
	}

	public RDFIntersectionSubTreeSlashBurnKernel(int depth, double discountFactor, boolean inference, boolean normalize) {
		this.normalize = normalize;
		this.label = "RDF Intersection SubTree Kernel";

		labelMap = new HashMap<String, Integer>();
		invLabelMap = new HashMap<Integer, String>();
		instanceVertices = new HashMap<String, DTNode<StringLabel,StringLabel>>();
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

	public void setHubThreshold(int h) {
		hubThreshold = h;
	}

	public double[][] compute(RDFDataSet dataset, List<Resource> instances, List<Statement> blackList) {
		double[][] kernel = KernelUtils.initMatrix(instances.size(), instances.size());
		Tree<Vertex<Integer>, Edge<Integer>> tree;

		DTGraph<StringLabel,StringLabel> graph = createGraphFromRDF(dataset, instances, blackList);
		// --- SlashBurn baby!
		DTGraph<String,String> sGraph = copy2StringLabeledGraph(graph);
		List<DTNode<String,String>> hubs = SlashBurn.getHubs(sGraph, (int) Math.round(0.05 * sGraph.nodes().size()), true);

		// Remove hubs from list that are root nodes
		List<DTNode<String,String>> rn = new ArrayList<DTNode<String,String>>();
		Set<String> is = new HashSet<String>();
		for (Resource r : instances) {
			is.add(Integer.toString(labelMap.get(r.toString())));
		}
		for (DTNode<String,String> hub : hubs) {
			if (is.contains(hub.label())) {
				rn.add(hub);
			}
		}
		hubs.removeAll(rn);

		List<DTLink<StringLabel,StringLabel>> toRemove = new ArrayList<DTLink<StringLabel,StringLabel>>();
		int index = 0;
		hubThreshold = Math.min(hubThreshold, hubs.size());
		System.out.println("Removing " + hubThreshold + " hubs.");

		while (index < hubThreshold) {
			org.nodes.util.Pair<Dir,String> sig = SlashBurn.primeSignature(hubs.get(index));
			String newLabel = "";

			System.out.println("Removing: " + invLabelMap.get(Integer.parseInt(hubs.get(index).label())));

			DTNode<StringLabel,StringLabel> hubN = graph.get(hubs.get(index).index());
			Collection<? extends DTLink<StringLabel,StringLabel>> links;
			if (sig.first() == Dir.IN) {
				links = hubN.linksIn();
				newLabel = sig.second() + "_" + hubs.get(index).label();
			} else {
				links = hubN.linksOut();
				newLabel = hubs.get(index).label() + "_" + sig.second();
			}			
			if (!labelMap.containsKey(newLabel)) {
				labelMap.put(newLabel, labelCounter);
				invLabelMap.put(labelCounter, newLabel);
				labelCounter++;
			}
			newLabel = Integer.toString(labelMap.get(newLabel));

			for (DTLink<StringLabel,StringLabel> link : links) {
				if (link.tag().toString().equals(sig.second())) { // If the label is equal, then this is a link to cut
					toRemove.add(link);
					if (sig.first() == Dir.IN) {
						link.from().label().clear();
						link.from().label().append(newLabel);
					} else {
						link.to().label().clear();
						link.to().label().append(newLabel);
					}
				}
			}
			index++;
		}
		for (DTLink<StringLabel,StringLabel> l : toRemove) {
			l.remove();
		}
		// --- End SlashBurn

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


	private DTGraph<StringLabel,StringLabel> createGraphFromRDF(RDFDataSet dataset, List<Resource> instances, List<Statement> blackList) {
		Map<String, DTNode<StringLabel,StringLabel>> vertexMap  = new HashMap<String, DTNode<StringLabel,StringLabel>>();
		Map<String, DTNode<StringLabel,StringLabel>> literalMap = new HashMap<String, DTNode<StringLabel,StringLabel>>();
		Map<String, DTLink<StringLabel,StringLabel>>   edgeMap  = new HashMap<String, DTLink<StringLabel,StringLabel>>();

		DTGraph<StringLabel,StringLabel> graph = new MapDTGraph<StringLabel,StringLabel>();

		List<Resource> queryNodes = new ArrayList<Resource>();
		List<Resource> newQueryNodes;
		List<Statement> result;

		DTNode<StringLabel,StringLabel> startV;
		DTNode<StringLabel,StringLabel> newV;
		DTLink<StringLabel,StringLabel> newE;

		String idStr, idStr2;

		for (Resource instance : instances) {
			idStr = instance.toString();			

			if (vertexMap.containsKey(idStr)) {
				startV = vertexMap.get(idStr);
			} else {
				labelMap.put(instance.toString(), labelCounter);
				invLabelMap.put(labelCounter, instance.toString());
				startV = graph.add(new StringLabel(Integer.toString(labelCounter)));
				labelCounter++;
				vertexMap.put(idStr, startV);
			}
			instanceVertices.put(idStr, startV); 

			queryNodes.add(instance);

			for (int i = depth - 1; i >= 0; i--) {
				newQueryNodes = new ArrayList<Resource>();

				for (Resource queryNode : queryNodes) {
					result = dataset.getStatements(queryNode, null, null, inference);

					for (Statement stmt : result) {

						// Process new vertex
						if (stmt.getObject() instanceof Literal) {
							idStr = stmt.toString();
							idStr2 = stmt.getObject().toString();
							if (literalMap.containsKey(idStr)) {
								newV = literalMap.get(idStr);
							} else {
								newV = graph.add(new StringLabel());
								if (!labelMap.containsKey(idStr2)) {
									labelMap.put(idStr2, labelCounter);
									invLabelMap.put(labelCounter,idStr2);
									labelCounter++;
								}
								newV.label().append(Integer.toString(labelMap.get(idStr2)));
								literalMap.put(idStr, newV);
							}
							
						} else {

							idStr = stmt.getObject().toString();
							if (vertexMap.containsKey(idStr)) { // existing vertex
								newV = vertexMap.get(idStr);				 	
							} else { // New vertex
								labelMap.put(idStr, labelCounter);
								invLabelMap.put(labelCounter, idStr);
								newV = graph.add(new StringLabel(Integer.toString(labelCounter)));
								labelCounter++;
								vertexMap.put(idStr, newV);
							}
						}

						// Process new Edge
						idStr = stmt.toString();
						idStr2 = stmt.getPredicate().toString();
						if (edgeMap.containsKey(idStr)) { // existing edge
							newE = edgeMap.get(idStr);

						} else { // new edge
							if (!labelMap.containsKey(idStr2)) {
								labelMap.put(idStr2, labelCounter);
								invLabelMap.put(labelCounter, idStr2);
								labelCounter++;
							}
							newE = vertexMap.get(stmt.getSubject().toString()).connect(newV, new StringLabel(Integer.toString(labelMap.get(idStr2))));
							edgeMap.put(idStr, newE);
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
			if (edgeMap.containsKey(stmt.toString())) {
				edgeMap.get(stmt.toString()).remove();
			}
		}
		return graph;
	}

	private DTGraph<String,String> copy2StringLabeledGraph(DTGraph<StringLabel,StringLabel> graph) {
		DTGraph<String,String> newGraph = new MapDTGraph<String,String>();

		for (DTNode<StringLabel,StringLabel> node : graph.nodes()) {
			newGraph.add(node.label().toString());
		}

		for (DTLink<StringLabel,StringLabel> link : graph.links()) {
			newGraph.nodes().get(link.from().index()).connect(newGraph.nodes().get(link.to().index()), link.tag().toString());
		}
		return newGraph;
	}



	private Tree<Vertex<Integer>, Edge<Integer>> computeIntersectionTree(DTGraph<StringLabel,StringLabel> graph, DTNode<StringLabel,StringLabel> rootA, DTNode<StringLabel,StringLabel> rootB) {
		Tree<Vertex<Integer>, Edge<Integer>> iTree = new DelegateTree<Vertex<Integer>, Edge<Integer>>();

		// Search front is a map, because we are making a graph expansion, i.e. the same node can occur multiple times in a tree, thus we cannot use
		// the nodes directly, and we need to possibly store multiple references of the same node, hence the utility vertex tracker class.
		Map<VertexTracker, Vertex<Integer>> searchFront = new HashMap<VertexTracker, Vertex<Integer>>();
		Map<VertexTracker, Vertex<Integer>> newSearchFront, newSearchFrontPartial;
		int vtCount = 1;

		List<DTNode<StringLabel,StringLabel>> commonChilds = getCommonChilds(graph, rootA, rootB);

		VertexTracker newRoot = new VertexTracker(null, vtCount++); // null is the special root label :)
		searchFront.put(newRoot, new Vertex<Integer>(0));
		iTree.addVertex(searchFront.get(newRoot));

		for (int i = 0; i < depth; i++) {
			newSearchFront = new HashMap<VertexTracker, Vertex<Integer>>();

			for (VertexTracker vt : searchFront.keySet()) {
				newSearchFrontPartial = new HashMap<VertexTracker, Vertex<Integer>>();

				if (vt.getVertex() == null) { // root nodes
					for (DTNode<StringLabel,StringLabel> v : commonChilds) {
						newSearchFrontPartial.put(new VertexTracker(v, vtCount++), new Vertex<Integer>(v == null ? 0 : Integer.parseInt(v.label().toString())));					 
					}

				} else {
					for (DTLink<StringLabel,StringLabel> edge : vt.getVertex().linksOut()) {
						if (edge.to() == rootA || edge.to() == rootB) { // if we find a root node
							newSearchFrontPartial.put(new VertexTracker(null, vtCount++), new Vertex<Integer>(0));
						} else {
							newSearchFrontPartial.put(new VertexTracker(edge.to(),vtCount++), new Vertex<Integer>(Integer.parseInt(edge.to().label().toString())));
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

	private List<DTNode<StringLabel,StringLabel>> getCommonChilds(DTGraph<StringLabel,StringLabel> graph, DTNode<StringLabel,StringLabel> rootA, DTNode<StringLabel,StringLabel> rootB) {
		List<DTNode<StringLabel,StringLabel>> commonChilds = new ArrayList<DTNode<StringLabel,StringLabel>>();

		Set<Pair> childsA = new TreeSet<Pair>();
		Set<Pair> childsB = new TreeSet<Pair>();
		Map<Pair, DTNode<StringLabel,StringLabel>> pairMap = new TreeMap<Pair, DTNode<StringLabel,StringLabel>>();
		Pair pair;

		// We need common edge label pairs to find common children
		for (DTLink<StringLabel,StringLabel> edge : rootA.linksOut()) {
			pair = new Pair(Integer.parseInt(edge.tag().toString()), Integer.parseInt(edge.to().label().toString()));
			childsA.add(pair);
			pairMap.put(pair, edge.to());
		}

		for (DTLink<StringLabel,StringLabel> edge : rootB.linksOut()) {
			pair = new Pair(Integer.parseInt(edge.tag().toString()), Integer.parseInt(edge.to().label().toString()));
			childsB.add(pair);
			pairMap.put(pair, edge.to());
		}

		// If root nodes have an equivalence like relation
		for (Pair childA : childsA) {
			if (childA.getSecond() == Integer.parseInt(rootB.label().toString()) && childsB.contains(new Pair(childA.getFirst(), Integer.parseInt(rootA.label().toString())))) {
				commonChilds.add(null);
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
		DTNode<StringLabel,StringLabel> vertex;
		int count;

		public VertexTracker(DTNode<StringLabel,StringLabel> vertex, int count) {
			super();
			this.vertex = vertex;
			this.count = count;
		}

		public DTNode<StringLabel,StringLabel> getVertex() {
			return vertex;
		}

		public void setVertex(DTNode<StringLabel,StringLabel> vertex) {
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
