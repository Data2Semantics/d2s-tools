package org.data2semantics.tools.graphs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.openrdf.model.Statement;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;


public class GraphFactory {

	public static DirectedGraph<Vertex<String>, Edge<String>> createJUNGGraph(List<Statement> sesameGraph) 
	{
		return createJUNGGraph(sesameGraph, null, null);
	}
	
	public static DirectedGraph<Vertex<String>, Edge<String>> createJUNGGraph(
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
			
			if (node2 == null) {
				node2 = new Vertex<String>(statement.getObject().toString());
				nodes.put(node2.getLabel(), node1);
				graph.addVertex(node2);
			}			
							
			graph.addEdge(new Edge<String>(statement.getPredicate().toString()), node1, node2, EdgeType.DIRECTED);			
		}	
		
		return graph;
	}

}
