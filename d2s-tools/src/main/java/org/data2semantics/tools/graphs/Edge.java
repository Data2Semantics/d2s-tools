package org.data2semantics.tools.graphs;

public class Edge {
	private String label;
	private Node node1;
	private Node node2;
	
	public Edge(String label, Node node1, Node node2) {
		this.label = label;
		this.node1 = node1;
		this.node2 = node2;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public Node getNode1() {
		return node1;
	}

	public Node getNode2() {
		return node2;
	}
	

}
