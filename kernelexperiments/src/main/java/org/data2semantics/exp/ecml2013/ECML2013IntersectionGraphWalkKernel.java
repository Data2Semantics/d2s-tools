package org.data2semantics.exp.ecml2013;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.data2semantics.tools.graphs.Edge;
import org.data2semantics.tools.graphs.Vertex;

import edu.uci.ics.jung.graph.DirectedGraph;

/**
 * Extension of the {@link ECML2013IntersectionGraphKernel} into the Intersection Graph Walk kernel.
 * 
 * 
 * 
 * @author Gerben
 *
 */
public class ECML2013IntersectionGraphWalkKernel extends ECML2013IntersectionGraphKernel {
	
	public ECML2013IntersectionGraphWalkKernel(int maxLength, double discountFactor) {
		this(maxLength, discountFactor, true);
	}
	
	public ECML2013IntersectionGraphWalkKernel(int maxLength, double discountFactor, boolean normalize) {
		super(maxLength, discountFactor, normalize);
		this.label = "Intersection Graph Walk Kernel, maxLength=" + maxLength + ", lambda=" + discountFactor;
	}

	protected double subGraphCount(DirectedGraph<Vertex<String>, Edge<String>> graph, int maxLength, double discountFactor) { 
		double score = 0;
		Set<List<Edge<String>>> newPaths, paths = new HashSet<List<Edge<String>>>();
		for (Edge<String> edge: graph.getEdges()) {
			List<Edge<String>> path = new ArrayList<Edge<String>>();
			path.add(edge);
			paths.add(path);
		}
		if (maxLength > 0) {
			score = Math.pow(discountFactor, 1) * paths.size();
		}
				
		List<Edge<String>> newPath;
		for (int i = 1; i < maxLength; i++) {
			newPaths = new HashSet<List<Edge<String>>>();
			for (List<Edge<String>> path : paths) {
				newPath = new ArrayList<Edge<String>>(path);
				for (Edge<String> edge : graph.getOutEdges(graph.getDest(path.get(path.size()-1)))) {
					newPath.add(edge);
					if (!newPaths.contains(newPath)) {
						newPaths.add(newPath);
					}
				}
			}
			score += Math.pow(discountFactor, i+1) * newPaths.size();
			paths = newPaths;
		}
		return score;
	}
}
