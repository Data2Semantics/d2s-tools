package org.data2semantics.tools.kernels;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.data2semantics.tools.graphs.Edge;
import org.data2semantics.tools.graphs.Vertex;

import edu.uci.ics.jung.graph.DirectedGraph;

public class WLSubTreeKernel implements GraphKernel {
	private double[][] kernel;
	private List<List<DirectedGraph<Vertex<String>, Edge<String>>>> graphs;
	private Map<String, String> labelDict;
	private int startLabel;
	
	public WLSubTreeKernel(List<DirectedGraph<Vertex<String>, Edge<String>>> graphs) {
		kernel = new double[graphs.size()][graphs.size()];
		this.graphs = new ArrayList<List<DirectedGraph<Vertex<String>, Edge<String>>>>();
		this.graphs.add(graphs);
		labelDict = new HashMap<String,String>();
		startLabel = 1;
	}
	
	private void relabelGraphs() {
		List<DirectedGraph<Vertex<String>, Edge<String>>> graphs = this.graphs.get(this.graphs.size()-1);
		
		for (DirectedGraph<Vertex<String>, Edge<String>> graph : graphs) {
			for (Vertex<String> node : graph.getVertices()) {
				
			}
			
			for (Edge<String> edge : graph.getEdges()) {
				
			}
		}
		
	}
	
	
	private void processGraph(DirectedGraph<Vertex<String>, Edge<String>> graph, int iterations) {
		Collection<Vertex<String>> nodes = graph.getVertices();
		
		for (int i = 0; i < iterations; i++) {
			for (Vertex<String> node : nodes) {
				// TODO relabel nodes & edges
			}
		}
		
		
	}
	
	
	public double[][] getKernel() {
		return kernel;
	}
	
	
	
//	private List<Graph> graphs;
	
//	public WLSubTreeKernel(List<Graph> graphs) {
//		this.graphs = graphs;
//	}
	
//	public void compute() {
//		
//	}
		

}
