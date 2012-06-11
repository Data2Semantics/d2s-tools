package org.data2semantics.tools.graphs;

import org.openrdf.model.Statement;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;


public class GraphFactory {

	public static DirectedGraph<String, Edge<String>> createJUNGGraph(org.openrdf.model.Graph sesameGraph) {
		DirectedGraph<String, Edge<String>> graph = new DirectedSparseMultigraph<String, Edge<String>>();
		
		for (Statement statement : sesameGraph) {
			graph.addVertex(statement.getSubject().toString());
			graph.addVertex(statement.getObject().toString());		
			graph.addEdge(new Edge<String>(statement.getPredicate().toString()), statement.getSubject().toString(), statement.getObject().toString(), EdgeType.DIRECTED);
			
		}	
		
		return graph;
	}

}
