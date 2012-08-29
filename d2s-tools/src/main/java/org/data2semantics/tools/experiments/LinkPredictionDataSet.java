package org.data2semantics.tools.experiments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.data2semantics.tools.graphs.DirectedMultigraphWithRoot;
import org.data2semantics.tools.graphs.Edge;
import org.data2semantics.tools.graphs.Vertex;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.util.Pair;

public class LinkPredictionDataSet {
	private String label;
	private List<DirectedMultigraphWithRoot<Vertex<String>, Edge<String>>> graphsA;
	private List<DirectedMultigraphWithRoot<Vertex<String>, Edge<String>>> graphsB;
	private Map<Pair<DirectedMultigraphWithRoot<Vertex<String>,Edge<String>>>, Boolean> labels;
	
	
	public LinkPredictionDataSet(LinkPredictionDataSet set) {
		this.label = new String(set.getLabel());
		this.graphsA = new ArrayList<DirectedMultigraphWithRoot<Vertex<String>, Edge<String>>>(set.getGraphsA());
		this.graphsB = new ArrayList<DirectedMultigraphWithRoot<Vertex<String>, Edge<String>>>(set.getGraphsB());
		this.labels = new HashMap<Pair<DirectedMultigraphWithRoot<Vertex<String>, Edge<String>>>, Boolean>(set.getLabels());
	}
		
	public LinkPredictionDataSet(
			String label,
			List<DirectedMultigraphWithRoot<Vertex<String>, Edge<String>>> graphsA,
			List<DirectedMultigraphWithRoot<Vertex<String>, Edge<String>>> graphsB,
			Map<Pair<DirectedMultigraphWithRoot<Vertex<String>, Edge<String>>>, Boolean> labels) {
		super();
		this.label = label;
		this.graphsA = graphsA;
		this.graphsB = graphsB;
		this.labels = labels;
	}
	
	public void shuffle(long seed) {
		Collections.shuffle(graphsA, new Random(seed));
		Collections.shuffle(graphsB, new Random(seed));
	}	
	
	public String getLabel() {
		return label;
	}
	
	public List<DirectedMultigraphWithRoot<Vertex<String>, Edge<String>>> getGraphsA() {
		return graphsA;
	}
	
	public List<DirectedMultigraphWithRoot<Vertex<String>, Edge<String>>> getGraphsB() {
		return graphsB;
	}

	public Map<Pair<DirectedMultigraphWithRoot<Vertex<String>, Edge<String>>>, Boolean> getLabels() {
		return labels;
	}
	
	
	
}
