package org.data2semantics.tools.experiments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import org.data2semantics.tools.graphs.DirectedMultigraphWithRoot;
import org.data2semantics.tools.graphs.Edge;
import org.data2semantics.tools.graphs.Vertex;

import edu.uci.ics.jung.graph.DirectedGraph;

public class GraphClassificationDataSet {
	private static final String BLANK_VERTEX_LABEL = "blank_vertex_1337";
	private static final String BLANK_EDGE_LABEL   = "blank_edge_1337";
	private String label;
	private List<DirectedMultigraphWithRoot<Vertex<String>, Edge<String>>> graphs;
	private List<String> labels;
	
	public GraphClassificationDataSet(String label,
			List<DirectedMultigraphWithRoot<Vertex<String>, Edge<String>>> graphs,
			List<String> labels) {
		super();
		this.label = label;
		this.graphs = graphs;
		this.labels = labels;
	}

	public GraphClassificationDataSet(GraphClassificationDataSet set) {
		this.label = new String(set.getLabel());
		this.graphs = new ArrayList<DirectedMultigraphWithRoot<Vertex<String>, Edge<String>>>(set.getGraphs());
		this.labels = new ArrayList<String>(set.getLabels());
	}
	

	public String getLabel() {
		return label;
	}


	public List<DirectedMultigraphWithRoot<Vertex<String>, Edge<String>>> getGraphs() {
		return graphs;
	}


	public List<String> getLabels() {
		return labels;
	}
	
	


	public void shuffle(long seed) {
		Collections.shuffle(graphs, new Random(seed));
		Collections.shuffle(labels, new Random(seed));
	}
	
	public void removeSmallClasses(int threshold) {
		Map<String, Integer> classCounts = computeClassCounts();
		List<DirectedMultigraphWithRoot<Vertex<String>, Edge<String>>> newGraphs = new ArrayList<DirectedMultigraphWithRoot<Vertex<String>,Edge<String>>>();
		List<String> newLabels = new ArrayList<String>();
		
		for (int i = 0; i < labels.size(); i++) {
			if (classCounts.get(labels.get(i)) >= threshold) {
				newGraphs.add(graphs.get(i));
				newLabels.add(labels.get(i));
			}
		}
		
		graphs = newGraphs;
		labels = newLabels;
	}
	
	public void removeVertexAndEdgeLabels() {
		for (DirectedMultigraphWithRoot<Vertex<String>, Edge<String>> graph : graphs) {
			for (Vertex<String> vertex : graph.getVertices()) {
				vertex.setLabel(BLANK_VERTEX_LABEL);
			}
			for (Edge<String> edge : graph.getEdges()) {
				edge.setLabel(BLANK_EDGE_LABEL);
			}
		}
	}
	
	
	
	private Map<String, Integer> computeClassCounts() {
		Map<String, Integer> counts = new TreeMap<String, Integer>();

		for (int i = 0; i < labels.size(); i++) {
			if (!counts.containsKey(labels.get(i))) {
				counts.put(labels.get(i), 1);
			} else {
				counts.put(labels.get(i), counts.get(labels.get(i)) + 1);
			}
		}
		return counts;
	}
}
