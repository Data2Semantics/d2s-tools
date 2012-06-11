package org.data2semantics.tools.kernels;

import java.util.Collection;
import java.util.List;

import org.data2semantics.tools.graphs.Edge;
import org.data2semantics.tools.graphs.Node;

import edu.uci.ics.jung.graph.DirectedGraph;

public class WLSubTreeKernel implements GraphKernel {
	private double[][] kernel;
	private List<DirectedGraph<Node<String>, Edge<String>>> graphs;
	
	public WLSubTreeKernel(List<DirectedGraph<Node<String>, Edge<String>>> graphs) {
		kernel = new double[graphs.size()][graphs.size()];
		this.graphs = graphs;		
	}
	
	
	private void processGraph(DirectedGraph<Node<String>, Edge<String>> graph, int iterations) {
		Collection<Node<String>> nodes = graph.getVertices();
		
		for (int i = 0; i < iterations; i++) {
			for (Node<String> node : nodes) {
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
