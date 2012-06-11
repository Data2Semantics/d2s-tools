package org.data2semantics.tools.graphs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openrdf.model.Statement;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;


public class GraphFactory {

	public static DirectedGraph<Node<String>, Edge<String>> createJUNGGraph(List<Statement> sesameGraph) {
		DirectedGraph<Node<String>, Edge<String>> graph = new DirectedSparseMultigraph<Node<String>, Edge<String>>();
		Map<String, Node<String>> nodes = new HashMap<String, Node<String>>();	
		Node<String> node1, node2;
		
		for (Statement statement : sesameGraph) {
			node1 = nodes.get(statement.getSubject().toString());
			node2 = nodes.get(statement.getObject().toString());
		
			if (node1 == null) {
				node1 = new Node<String>(statement.getSubject().toString());
				nodes.put(node1.getLabel(), node1);
			}
			graph.addVertex(node1);
			
			if (node2 == null) {
				node2 = new Node<String>(statement.getObject().toString());
				nodes.put(node2.getLabel(), node1);
			}			
			graph.addVertex(node2);
						
			graph.addEdge(new Edge<String>(statement.getPredicate().toString()), node1, node2, EdgeType.DIRECTED);			
		}	
		
		return graph;
	}

}
