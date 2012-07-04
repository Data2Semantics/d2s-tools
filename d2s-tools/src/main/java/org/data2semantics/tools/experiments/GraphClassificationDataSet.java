package org.data2semantics.tools.experiments;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.data2semantics.tools.graphs.Edge;
import org.data2semantics.tools.graphs.Vertex;

import edu.uci.ics.jung.graph.DirectedGraph;

public class GraphClassificationDataSet {
	private String label;
	private List<DirectedGraph<Vertex<String>, Edge<String>>> graphs;
	private List<String> labels;
	private List<Vertex<String>> rootVertices;
	
	public GraphClassificationDataSet(String label,
			List<DirectedGraph<Vertex<String>, Edge<String>>> graphs,
			List<String> labels, List<Vertex<String>> rootVertices) {
		super();
		this.label = label;
		this.graphs = graphs;
		this.labels = labels;
		this.rootVertices = rootVertices;
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
	
	
		
	public List<Vertex<String>> getRootVertices() {
		return rootVertices;
	}


	public void shuffle(long seed) {
		Collections.shuffle(graphs, new Random(seed));
		Collections.shuffle(labels, new Random(seed));
		Collections.shuffle(rootVertices, new Random(seed));
		
	}
}
