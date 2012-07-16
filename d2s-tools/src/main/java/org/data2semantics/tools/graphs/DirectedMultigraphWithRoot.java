package org.data2semantics.tools.graphs;

import edu.uci.ics.jung.graph.DirectedSparseMultigraph;

public class DirectedMultigraphWithRoot<V, E> extends
		DirectedSparseMultigraph<V, E> {

	private V rootVertex;
	
	public DirectedMultigraphWithRoot() {
		super();
	}
	
	public void setRootVertex(V vertex) {
		rootVertex = vertex;
	}
	
	public V getRootVertex() {
		return rootVertex;
	}
}
