package org.data2semantics.tools.experiments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

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

	public GraphClassificationDataSet(GraphClassificationDataSet set) {
		this.label = new String(set.getLabel());
		this.graphs = new ArrayList<DirectedGraph<Vertex<String>, Edge<String>>>(set.getGraphs());
		this.labels = new ArrayList<String>(set.getLabels());
		this.rootVertices = new ArrayList<Vertex<String>>(set.getRootVertices());
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
	
	public void removeSmallClasses(int threshold) {
		Map<String, Integer> classCounts = computeClassCounts();
		List<DirectedGraph<Vertex<String>, Edge<String>>> newGraphs = new ArrayList<DirectedGraph<Vertex<String>,Edge<String>>>();
		List<String> newLabels = new ArrayList<String>();
		List<Vertex<String>> newRootVertices = new ArrayList<Vertex<String>>();
		
		for (int i = 0; i < labels.size(); i++) {
			if (classCounts.get(labels.get(i)) >= threshold) {
				newGraphs.add(graphs.get(i));
				newLabels.add(labels.get(i));
				newRootVertices.add(rootVertices.get(i));		
			}
		}
		
		graphs = newGraphs;
		labels = newLabels;
		rootVertices = newRootVertices;
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
