package org.data2semantics.tools.kernels;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.data2semantics.tools.graphs.Edge;
import org.data2semantics.tools.graphs.Vertex;

import edu.uci.ics.jung.graph.DirectedGraph;

public class IntersectionGraphPathKernel extends IntersectionGraphKernel {

	public IntersectionGraphPathKernel(int maxLength, double discountFactor) {
		this(new ArrayList<DirectedGraph<Vertex<String>,Edge<String>>>(), maxLength, discountFactor);
	}
	
	public IntersectionGraphPathKernel(List<DirectedGraph<Vertex<String>, Edge<String>>> graphs, int maxLength, double discountFactor) {
		super(graphs, maxLength, discountFactor);
		this.label = "Intersection Graph Path Kernel, maxLength=" + maxLength + ", lambda=" + discountFactor;
	}

	public double subGraphCount(DirectedGraph<Vertex<String>, Edge<String>> graph, int maxLength, double discountFactor) { 
		double score = 0;
		Set<List<Vertex<String>>> newPaths, paths = new HashSet<List<Vertex<String>>>();
		for (Edge<String> edge: graph.getEdges()) {
			List<Vertex<String>> path = new ArrayList<Vertex<String>>();
			path.add(graph.getSource(edge));
			path.add(graph.getDest(edge));
			paths.add(path);
		}
		if (maxLength > 0) {
			score = Math.pow(discountFactor, 1) * paths.size();
		}

		List<Vertex<String>> newPath;
		for (int i = 1; i < maxLength; i++) {
			newPaths = new HashSet<List<Vertex<String>>>();
			for (List<Vertex<String>> path : paths) {
				newPath = new ArrayList<Vertex<String>>(path);

				for (Edge<String> edge : graph.getOutEdges(path.get(path.size()-1))) {
					if (!path.contains(graph.getDest(edge))) {
						newPath.add(graph.getDest(edge));
						if (!newPaths.contains(newPath)) {
							newPaths.add(newPath);
						}
					}
				}
			}
			score += Math.pow(discountFactor, i+1) * newPaths.size();
			paths = newPaths;
		}
		return score;
	}
}
