package org.data2semantics.tools.graphs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.openrdf.model.Literal;
import org.openrdf.model.Statement;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;


public class GraphFactory {

	
	
	public static DirectedGraph<Vertex<String>, Edge<String>> createDirectedGraph(List<Statement> sesameGraph) 
	{
		return createDirectedGraph(sesameGraph, null, null);
	}

	public static DirectedGraph<Vertex<String>, Edge<String>> createDirectedGraph(
			List<Statement> sesameGraph, 
			List<String> vWhiteList,
			List<String> eWhiteList)
			{
		List<Pattern> vertexWhiteList = null;

		if(vWhiteList != null) 
		{
			vertexWhiteList = new ArrayList<Pattern>(vWhiteList.size());
			for(String patternString : vWhiteList)
				vertexWhiteList.add(Pattern.compile(patternString));
		}


		List<Pattern> edgeWhiteList = null;
		if(eWhiteList != null)
		{
			edgeWhiteList = new ArrayList<Pattern>(eWhiteList.size());
			for(String patternString : eWhiteList)
				edgeWhiteList.add(Pattern.compile(patternString));
		}

		DirectedGraph<Vertex<String>, Edge<String>> graph = new DirectedSparseMultigraph<Vertex<String>, Edge<String>>();
		Map<String, Vertex<String>> nodes = new HashMap<String, Vertex<String>>();	
		Vertex<String> node1, node2;

		for (Statement statement : sesameGraph) 
		{
			if(vWhiteList != null)
			{
				if(! Graphs.matches(statement.getObject().toString(), vertexWhiteList))
					continue;
				if(! Graphs.matches(statement.getSubject().toString(), vertexWhiteList))
					continue;
			}

			if(eWhiteList != null)
				if(! Graphs.matches(statement.getPredicate().toString(), edgeWhiteList))
					continue;

			node1 = nodes.get(statement.getSubject().toString());
			node2 = nodes.get(statement.getObject().toString());

			if (node1 == null) {
				node1 = new Vertex<String>(statement.getSubject().toString());
				nodes.put(node1.getLabel(), node1);
				graph.addVertex(node1);
			}

			if (statement.getObject()instanceof Literal) { // Literals are not unique
				node2 = new Vertex<String>(statement.getObject().toString());
				graph.addVertex(node2);
			} else if (node2 == null) {
				node2 = new Vertex<String>(statement.getObject().toString());
				nodes.put(node2.getLabel(), node2);
				graph.addVertex(node2);
			}			

			graph.addEdge(new Edge<String>(statement.getPredicate().toString()), node1, node2, EdgeType.DIRECTED);
		}	

		return graph;
			}

	public static <L> DirectedGraph<Vertex<L>, Edge<L>> copyDirectedGraph(DirectedGraph<Vertex<L>, Edge<L>> graph) {
		DirectedGraph<Vertex<L>, Edge<L>> newGraph = new DirectedSparseMultigraph<Vertex<L>, Edge<L>>();

		Map<Vertex<L>, Vertex<L>> nodes = new HashMap<Vertex<L>, Vertex<L>>();

		for (Vertex<L> vertex : graph.getVertices()) {
			nodes.put(vertex, new Vertex<L>(vertex));
			newGraph.addVertex(nodes.get(vertex));
		}		
		for (Edge<L> edge : graph.getEdges()) {
			newGraph.addEdge(new Edge<L>(edge), nodes.get(graph.getSource(edge)), nodes.get(graph.getDest(edge)), EdgeType.DIRECTED);
		}	
		return newGraph;
	}

	public static <L> DirectedMultigraphWithRoot<Vertex<L>, Edge<L>> copyDirectedGraph2GraphWithRoot(DirectedGraph<Vertex<L>, Edge<L>> graph) {
		DirectedMultigraphWithRoot<Vertex<L>, Edge<L>> newGraph = new DirectedMultigraphWithRoot<Vertex<L>, Edge<L>>();

		Map<Vertex<L>, Vertex<L>> nodes = new HashMap<Vertex<L>, Vertex<L>>();

		for (Vertex<L> vertex : graph.getVertices()) {
			nodes.put(vertex, new Vertex<L>(vertex));
			newGraph.addVertex(nodes.get(vertex));
		}		
		for (Edge<L> edge : graph.getEdges()) {
			newGraph.addEdge(new Edge<L>(edge), nodes.get(graph.getSource(edge)), nodes.get(graph.getDest(edge)), EdgeType.DIRECTED);
		}	
		return newGraph;
	}

	
	public static <L> DirectedMultigraphWithRoot<Vertex<L>, Edge<L>> copyDirectedGraph2GraphWithRoot(DirectedGraph<Vertex<L>, Edge<L>> graph, L rootLabel) {
		DirectedMultigraphWithRoot<Vertex<L>, Edge<L>> newGraph = copyDirectedGraph2GraphWithRoot( graph);

		for (Vertex<L> vertex : graph.getVertices()) {
			if (vertex.getLabel().equals(rootLabel)) {
				newGraph.setRootVertex(vertex);
				break;
			}
		}
		return newGraph;
	}
}
