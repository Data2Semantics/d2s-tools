package org.data2semantics.tools.kernels;

import java.util.ArrayList;
import java.util.List;

import org.data2semantics.tools.graphs.Edge;
import org.data2semantics.tools.graphs.Vertex;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.Tree;

public class IntersectionPartialSubTreeKernel extends IntersectionSubTreeKernel {

	public IntersectionPartialSubTreeKernel(int depth, double discountFactor) {
		this(new ArrayList<DirectedGraph<Vertex<String>, Edge<String>>>(), new ArrayList<Vertex<String>>(), depth, discountFactor);
	}
	
	public IntersectionPartialSubTreeKernel(List<DirectedGraph<Vertex<String>, Edge<String>>> graphs, List<Vertex<String>> rootVertices, int depth, double discountFactor) {
		super(graphs, rootVertices, depth, discountFactor);
		this.label = "Intersection Partial SubTree Kernel, depth=" + depth + ", lambda=" + discountFactor;
	}

	public double subTreeScore(Tree<Vertex<String>, Edge<String>> tree, Vertex<String> currentVertex, double discountFactor) {
		// Base case of recursion
		if (tree.getSuccessors(currentVertex).isEmpty()) {
			return 1.0;
		} else { // recursive case
			double score = 1;
			for (Vertex<String> leaf: tree.getSuccessors(currentVertex)) {
				score *= discountFactor * subTreeScore(tree, leaf, discountFactor) + 1;
			}
			return score;
		}
	}
}
