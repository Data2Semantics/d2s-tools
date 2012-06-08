package org.data2semantics.tools.graphs;

import java.util.HashMap;
import java.util.Map;

public class Graph {
	private Map<String, Node> nodes;
	private Map<String, Edge> edges;
	
	public Graph() {
		nodes = new HashMap<String, Node>();
		edges = new HashMap<String, Edge>();	
	}
	
	public void addNode(String label) {
		if (!nodes.containsKey(label)) {
			nodes.put(label, new Node(label));
		}
	}
	
		
	public void addEdge(String label, String node1, String node2) {
		
		// Get nodes first, nodes should be added first, else crash, etc... 	
		Node n1 = nodes.get(node1);
		Node n2 = nodes.get(node2);
		
		Edge edge = new Edge(label, n1, n2);
		
		if (!edges.containsKey(label)) {
			edges.put(label, edge);
			n1.addEdge(edge);
			n2.addEdge(edge);
		}
	}

	public Map<String, Node> getNodes() {
		return nodes;
	}

	public Map<String, Edge> getEdges() {
		return edges;
	}
	
	public String toString() {
		StringBuffer str = new StringBuffer();
		str.append("Nodes: ");
		
		for (String nodeLabel : nodes.keySet()) {
			str.append(nodeLabel + ", ");
		}
		
		str.append("\nEdges: ");
		
		for (String edgeLabel : edges.keySet()) {
			str.append(edgeLabel + ", ");
		}
	
		return str.toString();
	}
	

}
