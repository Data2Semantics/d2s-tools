package org.data2semantics.proppred.kernels.rdfgraphkernels;

import java.util.HashMap;

import org.data2semantics.tools.graphs.Edge;
import org.data2semantics.tools.graphs.Vertex;

import edu.uci.ics.jung.graph.Tree;

public class RDFIntersectionPartialSubTreeKernel extends
		RDFIntersectionSubTreeKernel {
	
	public RDFIntersectionPartialSubTreeKernel() {
		this(2, 0.01, false, true, false);
	}

	public RDFIntersectionPartialSubTreeKernel(int depth, double discountFactor, boolean inference, boolean normalize, boolean blankLabels) {
		super(depth, discountFactor, inference, normalize, blankLabels);
		this.label = "RDF Intersection Partial SubTree Kernel";
	}
	
	public RDFIntersectionPartialSubTreeKernel(int depth, double discountFactor, boolean inference, boolean normalize) {
		this(depth, discountFactor, inference, normalize, false);
	}
	
	
	protected double subTreeScore(Tree<Vertex<Integer>, Edge<Integer>> tree, Vertex<Integer> currentVertex, double discountFactor) {
		// Base case of recursion
		if (tree.getSuccessors(currentVertex).isEmpty()) {
			return 1.0;
		} else { // recursive case
			double score = 1;
			for (Vertex<Integer> leaf: tree.getSuccessors(currentVertex)) {
				score *= discountFactor * subTreeScore(tree, leaf, discountFactor) + 1;
			}
			return score;
		}
	}

}
