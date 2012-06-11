package org.data2semantics.tools.graphs;

public class Node<Label> {
	private Label label;

	public Node(Label label) {
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
