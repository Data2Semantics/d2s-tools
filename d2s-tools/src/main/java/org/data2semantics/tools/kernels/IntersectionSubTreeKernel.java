package org.data2semantics.tools.kernels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.data2semantics.tools.graphs.Edge;
import org.data2semantics.tools.graphs.Vertex;

import edu.uci.ics.jung.graph.DelegateTree;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.Tree;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;

public class IntersectionSubTreeKernel extends GraphKernel {
	private static final String ROOTID = "ROOT1337";

	private List<Vertex<String>> rootVertices;
	private int depth;
	private double discountFactor;



	public IntersectionSubTreeKernel(List<DirectedGraph<Vertex<String>, Edge<String>>> graphs, List<Vertex<String>> rootVertices, int depth, double discountFactor) {
		super(graphs);
		this.rootVertices = rootVertices;
		this.depth = depth;
		this.discountFactor = discountFactor;
		this.label = "Intersection Full SubTree Kernel, depth=" + depth + ", lambda=" + discountFactor;
	}

	public void compute() {
		Tree<Vertex<String>, Edge<String>> tree;
		
		for (int i = 0; i < graphs.size(); i++) {
			for (int j = i; j < graphs.size(); j++) {				
				tree = computeIntersectionTree(graphs.get(i), graphs.get(j), rootVertices.get(i), rootVertices.get(j), depth);
				kernel[i][j] = subTreeScore(tree, tree.getRoot(), discountFactor);
				kernel[j][i] = kernel[i][j];
			}
		}
	}


	public Tree<Vertex<String>, Edge<String>> computeIntersectionTree(DirectedGraph<Vertex<String>, Edge<String>> graphA, DirectedGraph<Vertex<String>, Edge<String>> graphB, Vertex<String> rootA, Vertex<String> rootB, int depth) {
		Vertex<String> newRoot = new Vertex<String>(ROOTID);
		List<Vertex<String>> searchFront = new ArrayList<Vertex<String>>();
		List<Vertex<String>> newSearchFront;
		Tree<Vertex<String>, Edge<String>> iTree = new DelegateTree<Vertex<String>, Edge<String>>();
		Map<Vertex<String>, Pair<Vertex<String>>> vertexMap, childTemp;
		vertexMap = new HashMap<Vertex<String>, Pair<Vertex<String>>>(); 

		searchFront.add(newRoot);
		vertexMap.put(newRoot, new Pair<Vertex<String>>(rootA, rootB));
		iTree.addVertex(newRoot);
		
		for (int i = 0; i < depth; i++) {
			newSearchFront = new ArrayList<Vertex<String>>();
			for (Vertex<String> vertex : searchFront) {
				childTemp = findCommonChildren(graphA, graphB, vertexMap.get(vertex).getFirst(), vertexMap.get(vertex).getSecond(), rootA, rootB);	
				vertexMap.putAll(childTemp);
				
				for (Vertex<String> newV : childTemp.keySet()) {
					newSearchFront.add(newV);
					iTree.addEdge(new Edge<String>(""), vertex, newV, EdgeType.DIRECTED);
				}
			}
			searchFront = newSearchFront;
		}
		return iTree;
	}
	
	private Map<Vertex<String>, Pair<Vertex<String>>> findCommonChildren(DirectedGraph<Vertex<String>, Edge<String>> graphA, DirectedGraph<Vertex<String>, Edge<String>> graphB, Vertex<String> vertexA, Vertex<String> vertexB, Vertex<String> rootA, Vertex<String> rootB) {
		Map<Vertex<String>, Pair<Vertex<String>>> children = new HashMap<Vertex<String>, Pair<Vertex<String>>>();		
		
		List<String> evA = new ArrayList<String>();
		List<String> evB = new ArrayList<String>();
		Map<String, Edge<String>> eMapA = new HashMap<String, Edge<String>>();
		Map<String, Edge<String>> eMapB = new HashMap<String, Edge<String>>();
		List<String> edgeLabels = new ArrayList<String>();
		
		for (Edge<String> edgeA : graphA.getOutEdges(vertexA)) {
			evA.add(edgeA.getLabel() + graphA.getDest(edgeA).getLabel());
			eMapA.put(edgeA.getLabel() + graphA.getDest(edgeA).getLabel(), edgeA);
			
			if (vertexA == rootA && graphA.getDest(edgeA).getLabel().equals(rootB.getLabel())) {
				edgeLabels.add(edgeA.getLabel());
			}
		}
		
		for (Edge<String> edgeB : graphB.getOutEdges(vertexB)) {
			evB.add(edgeB.getLabel() + graphB.getDest(edgeB).getLabel());
			eMapB.put(edgeB.getLabel() + graphB.getDest(edgeB).getLabel(), edgeB);
			
			// Special case of roots have an equivalence-like relation with each other
			if (vertexB == rootB && graphB.getDest(edgeB).getLabel().equals(rootA.getLabel())) {
				if (edgeLabels.contains(edgeB.getLabel())) {
					children.put(new Vertex<String>(ROOTID), new Pair<Vertex<String>>(rootA, rootB));
				}
			}
		}
		
		Collections.sort(evA);
		Collections.sort(evB);	
		
		Iterator<String> itA = evA.iterator();
		Iterator<String> itB = evB.iterator();
			
		int comparison = 0;
		String edgeA = null;
		String edgeB = null;
		boolean stop = false;
		while (!stop) {
			if (comparison == 0 && itA.hasNext() && itB.hasNext()) {
				edgeA = itA.next();
				edgeB = itB.next();
			} else if (comparison < 0 && itA.hasNext()) {
				edgeA = itA.next();
			} else if (comparison > 0 && itB.hasNext()){
				edgeB = itB.next();
			} else {
				stop = true;
			}
			
			if (!stop) {
			comparison = edgeA.compareTo(edgeB);
				if (comparison == 0) {
					if(graphA.getDest(eMapA.get(edgeA)) == rootA) {
						children.put(new Vertex<String>(ROOTID), new Pair<Vertex<String>>(rootA, rootB));
					} else {
						children.put(new Vertex<String>(graphA.getDest(eMapA.get(edgeA)).getLabel()), new Pair<Vertex<String>>(graphA.getDest(eMapA.get(edgeA)), graphB.getDest(eMapB.get(edgeB))));
					}
				} 
			}
		}
				
		return children;
	}
	
	public double subTreeScore(Tree<Vertex<String>, Edge<String>> tree, Vertex<String> currentVertex, double discountFactor) {
		// Base case of recursion
		if (tree.getSuccessors(currentVertex).isEmpty()) {
			return 1.0;
		} else { // recursive case
			double score = 0;
			for (Vertex<String> leaf: tree.getSuccessors(currentVertex)) {
				score += subTreeScore(tree, leaf, discountFactor);
			}
			return 1 + (discountFactor * score);
		}
	}
}
