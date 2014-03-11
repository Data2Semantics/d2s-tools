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

import org.data2semantics.proppred.kernels.Kernel;
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
public class RDFDTGraphIntersectionSubTreeKernel implements Kernel {
	private int depth;
	private double discountFactor;
	protected String label;
	protected boolean normalize;

	public RDFDTGraphIntersectionSubTreeKernel() {
		this(2, 1, true);
	}

	public RDFDTGraphIntersectionSubTreeKernel(int depth, double discountFactor, boolean normalize) {
		this.normalize = normalize;
		this.label = "RDF Intersection SubTree Kernel" + depth + "_" + discountFactor + "_" + normalize;

		this.depth = depth;
		this.discountFactor = discountFactor;
	}


	public String getLabel() {
		return label;
	}

	public void setNormalize(boolean normalize) {
		this.normalize = normalize;
	}


	public double[][] compute(DTGraph<String,String> graph, List<DTNode<String,String>> iNodes) {
		double[][] kernel = KernelUtils.initMatrix(iNodes.size(), iNodes.size());
		Tree<Vertex<Integer>, Edge<Integer>> tree;
		
		DTGraph<String,String> newG = toIntGraph(graph,iNodes);
			
		for (int i = 0; i < iNodes.size(); i++) {
			for (int j = i; j < iNodes.size(); j++) {
				tree = computeIntersectionTree(newG, iNodes.get(i), iNodes.get(j));
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
	
	private DTGraph<String,String> toIntGraph(DTGraph<String,String> graph, List<DTNode<String,String>> iNodes) {
		DTGraph<String,String> newG = new MapDTGraph<String,String>();
		Map<String,String> labelMap = new HashMap<String,String>();
		
		Map<DTNode<String,String>,Integer> iNodeMap = new HashMap<DTNode<String,String>,Integer>();	
		for (int i = 0; i < iNodes.size(); i++) {
			iNodeMap.put(iNodes.get(i), i);
		}	
		
		
		for (DTNode<String,String> n : graph.nodes()) {
			String lab = labelMap.get(n.label());
			if (lab == null) {
				lab = Integer.toString(labelMap.size()+1);
				labelMap.put(n.label(), lab);		
			}
			DTNode<String,String> newN = newG.add(lab);
			
			if (iNodeMap.containsKey(n)) {
				iNodes.set(iNodeMap.get(n), newN);
			}
		}
		
		for (DTLink<String,String> l : graph.links()) {
			String lab = labelMap.get(l.tag());
			if (lab == null) {
				lab = Integer.toString(labelMap.size()+1);
				labelMap.put(l.tag(), lab);		
			}
			newG.nodes().get(l.from().index()).connect(newG.nodes().get(l.to().index()), lab);
		}
		
		return newG;
	}
	


	private Tree<Vertex<Integer>, Edge<Integer>> computeIntersectionTree(DTGraph<String,String> graph, DTNode<String,String> rootA, DTNode<String,String> rootB) {
		Tree<Vertex<Integer>, Edge<Integer>> iTree = new DelegateTree<Vertex<Integer>, Edge<Integer>>();

		// Search front is a map, because we are making a graph expansion, i.e. the same node can occur multiple times in a tree, thus we cannot use
		// the nodes directly, and we need to possibly store multiple references of the same node, hence the utility vertex tracker class.
		Map<VertexTracker, Vertex<Integer>> searchFront = new HashMap<VertexTracker, Vertex<Integer>>();
		Map<VertexTracker, Vertex<Integer>> newSearchFront, newSearchFrontPartial;
		int vtCount = 1;

		List<DTNode<String,String>> commonChilds = getCommonChilds(graph, rootA, rootB);

		VertexTracker newRoot = new VertexTracker(null, vtCount++); // null is the special root label :)
		searchFront.put(newRoot, new Vertex<Integer>(0));
		iTree.addVertex(searchFront.get(newRoot));

		for (int i = 0; i < depth; i++) {
			newSearchFront = new HashMap<VertexTracker, Vertex<Integer>>();

			for (VertexTracker vt : searchFront.keySet()) {
				newSearchFrontPartial = new HashMap<VertexTracker, Vertex<Integer>>();

				if (vt.getVertex() == null) { // root nodes
					for (DTNode<String,String> v : commonChilds) {
						newSearchFrontPartial.put(new VertexTracker(v, vtCount++), new Vertex<Integer>(v == null ? 0 : Integer.parseInt(v.label())));					 
					}

				} else {
					for (DTLink<String,String> edge : vt.getVertex().linksOut()) {
						if (edge.to() == rootA || edge.to() == rootB) { // if we find a root node
							newSearchFrontPartial.put(new VertexTracker(null, vtCount++), new Vertex<Integer>(0));
						} else {
							newSearchFrontPartial.put(new VertexTracker(edge.to(),vtCount++), new Vertex<Integer>(Integer.parseInt(edge.to().label())));
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

	private List<DTNode<String,String>> getCommonChilds(DTGraph<String,String> graph, DTNode<String,String> rootA, DTNode<String,String> rootB) {
		List<DTNode<String,String>> commonChilds = new ArrayList<DTNode<String,String>>();

		Set<Pair> childsA = new TreeSet<Pair>();
		Set<Pair> childsB = new TreeSet<Pair>();
		Map<Pair, DTNode<String,String>> pairMap = new TreeMap<Pair, DTNode<String,String>>();
		Pair pair;

		// We need common edge label pairs to find common children
		for (DTLink<String,String> edge : rootA.linksOut()) {
			pair = new Pair(Integer.parseInt(edge.tag().toString()), Integer.parseInt(edge.to().label().toString()));
			childsA.add(pair);
			pairMap.put(pair, edge.to());
		}

		for (DTLink<String,String> edge : rootB.linksOut()) {
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
	
	class VertexTracker {
		DTNode<String,String> vertex;
		int count;

		public VertexTracker(DTNode<String,String> vertex, int count) {
			super();
			this.vertex = vertex;
			this.count = count;
		}

		public DTNode<String,String> getVertex() {
			return vertex;
		}

		public void setVertex(DTNode<String,String> vertex) {
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
