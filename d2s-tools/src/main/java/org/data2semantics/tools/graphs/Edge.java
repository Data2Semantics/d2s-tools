package org.data2semantics.tools.graphs;

public class Edge<Label> {
	private Label label;

	public Edge(Label label) {
		this.label = label;
	}

	
	/**
	 * Produces a new Edge which is a copy of the Edge e.
	 * The Label label is shallow copied
	 * 
	 * @param e Edge to copy
	 */
	public Edge(Edge<Label> e) {
		this.label = e.label;
	}
	
	public Label getLabel() {
		return label;
	}

	public void setLabel(Label label) {
		this.label = label;
	}
	
	public String toString() {
		return label.toString();
	}
}
