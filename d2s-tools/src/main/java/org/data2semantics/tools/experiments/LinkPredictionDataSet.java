package org.data2semantics.tools.experiments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.data2semantics.tools.graphs.Edge;
import org.data2semantics.tools.graphs.Vertex;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.util.Pair;

public class LinkPredictionDataSet {
	private String label;
	private List<DirectedGraph<Vertex<String>, Edge<String>>> graphsA;
	private List<DirectedGraph<Vertex<String>, Edge<String>>> graphsB;
	private List<Vertex<String>> rootVerticesA;
	private List<Vertex<String>> rootVerticesB;
	private Map<Pair<DirectedGraph<Vertex<String>,Edge<String>>>, Boolean> labels;
	
	
	public LinkPredictionDataSet(LinkPredictionDataSet set) {
		this.label = new String(set.getLabel());
		this.graphsA = new ArrayList<DirectedGraph<Vertex<String>, Edge<String>>>(set.getGraphsA());
		this.graphsB = new ArrayList<DirectedGraph<Vertex<String>, Edge<String>>>(set.getGraphsB());
		this.rootVerticesA = new ArrayList<Vertex<String>>(set.getRootVerticesA());
		this.rootVerticesB = new ArrayList<Vertex<String>>(set.getRootVerticesB());
		this.labels = new HashMap<Pair<DirectedGraph<Vertex<String>, Edge<String>>>, Boolean>(set.getLabels());
	}
		
	public LinkPredictionDataSet(
			String label,
			List<DirectedGraph<Vertex<String>, Edge<String>>> graphsA,
			List<DirectedGraph<Vertex<String>, Edge<String>>> graphsB,
			List<Vertex<String>> rootVerticesA,
			List<Vertex<String>> rootVerticesB,
			Map<Pair<DirectedGraph<Vertex<String>, Edge<String>>>, Boolean> labels) {
		super();
		this.label = label;
		this.graphsA = graphsA;
		this.graphsB = graphsB;
		this.rootVerticesA = rootVerticesA;
		this.rootVerticesB = rootVerticesB;
		this.labels = labels;
	}
	
	public void shuffle(long seed) {
		Collections.shuffle(graphsA, new Random(seed));
		Collections.shuffle(graphsB, new Random(seed));
		Collections.shuffle(rootVerticesA, new Random(seed));
		Collections.shuffle(rootVerticesB, new Random(seed));
	}	
	
	public String getLabel() {
		return label;
	}
	public List<DirectedGraph<Vertex<String>, Edge<String>>> getGraphsA() {
		return graphsA;
	}
	public List<DirectedGraph<Vertex<String>, Edge<String>>> getGraphsB() {
		return graphsB;
	}
	public List<Vertex<String>> getRootVerticesA() {
		return rootVerticesA;
	}
	public List<Vertex<String>> getRootVerticesB() {
		return rootVerticesB;
	}
	public Map<Pair<DirectedGraph<Vertex<String>, Edge<String>>>, Boolean> getLabels() {
		return labels;
	}
	
	
	
}
