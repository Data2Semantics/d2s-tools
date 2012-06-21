package org.data2semantics.tools.experiments;

import java.util.List;

import org.data2semantics.tools.graphs.Edge;
import org.data2semantics.tools.graphs.Vertex;

import edu.uci.ics.jung.graph.DirectedGraph;

public class GraphClassificationDataSet {
	private String label;
	private List<DirectedGraph<Vertex<String>, Edge<String>>> graphs;
	private List<String> labels;
	
	
	public GraphClassificationDataSet(String label,
			List<DirectedGraph<Vertex<String>, Edge<String>>> graphs,
			List<String> labels) {
		super();
		this.label = label;
		this.graphs = graphs;
		this.labels = labels;
	}


	public String getLabel() {
		return label;
	}


	public List<DirectedGraph<Vertex<String>, Edge<String>>> getGraphs() {
		return graphs;
	}


	public List<String> getLabels() {
		return labels;
	}	
}
