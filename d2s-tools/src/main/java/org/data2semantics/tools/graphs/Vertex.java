package org.data2semantics.tools.graphs;

public class Vertex<Label> {
	private Label label;

	public Vertex(Label label) {
		this.label = label;
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
