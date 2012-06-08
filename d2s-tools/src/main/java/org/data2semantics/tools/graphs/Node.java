package org.data2semantics.tools.graphs;

import java.util.ArrayList;
import java.util.List;

public class Node {
	private String label;
	private List<Edge> edges;
	
	public Node(String label) {
		this.label = label;
		this.edges = new ArrayList<Edge>();
	}
	
	public void addEdge(Edge edge) {
		edges.add(edge);
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}	
}
