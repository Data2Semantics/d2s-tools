package org.data2semantics.tools.kernels;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.data2semantics.tools.graphs.Edge;
import org.data2semantics.tools.graphs.GraphFactory;
import org.data2semantics.tools.graphs.Vertex;

import edu.uci.ics.jung.graph.DirectedGraph;

public abstract class GraphKernel {
	protected double[][] kernel;
	protected List<DirectedGraph<Vertex<String>, Edge<String>>> graphs;
	protected String label;
	
	public GraphKernel(List<DirectedGraph<Vertex<String>, Edge<String>>> graphs) {
		kernel = new double[graphs.size()][graphs.size()];
		for (int i = 0; i < graphs.size(); i++) {
			Arrays.fill(kernel[i], 0.0);
		}
		setGraphs(graphs);
		this.label = "Graph Kernel";
	}
	
	
	public abstract void compute();
	
	public void normalize() {
		double[] ss = new double[kernel.length];
		
		for (int i = 0; i < ss.length; i++) {
			ss[i] = kernel[i][i];
		}
			
		for (int i = 0; i < kernel.length; i++) {
			for (int j = i; j < kernel[i].length; j++) {
				kernel[i][j] /= Math.sqrt(ss[i] * ss[j]);
				kernel[j][i] = kernel[i][j];
			}
		}
	}
	

	public double[][] getKernel() {
		return kernel;
	}
	
	public String getLabel() {
		return label;
	}

	private void setGraphs(List<DirectedGraph<Vertex<String>, Edge<String>>> graphs) {
		this.graphs = new ArrayList<DirectedGraph<Vertex<String>, Edge<String>>>();	
		for(DirectedGraph<Vertex<String>, Edge<String>> graph : graphs) {
			this.graphs.add(GraphFactory.copyDirectedGraph(graph));				
		}
	}
}
