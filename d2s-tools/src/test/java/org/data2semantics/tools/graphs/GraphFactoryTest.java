package org.data2semantics.tools.graphs;


import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFFormat;

import org.data2semantics.tools.rdf.*;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;

public class GraphFactoryTest {

	@Ignore
	public void test() {
		RDFDataSet testSet = new RDFFileDataSet("D:\\workspaces\\eclipse_workspace\\rdfgraphlearning\\src\\main\\resources\\aifb-fixed_complete.rdf", RDFFormat.RDFXML);
		//org.openrdf.model.Graph triples = testSet.getStatements(null, "http://swrc.ontoware.org/ontology#affiliation", null, true);
		List<Statement> triples = testSet.getFullGraph();
			
		DirectedGraph<Vertex<String>, Edge<String>> jungGraph = GraphFactory.createDirectedGraph(triples);
		
		for (Edge<String> edge: jungGraph.getEdges()) {
			System.out.println(jungGraph.getEndpoints(edge).getFirst() + " " + edge + " " + jungGraph.getEndpoints(edge).getSecond());
		}
	}
	
	@Test
	public void test2() {
		List<DirectedGraph<Vertex<String>, Edge<String>>> graphs = new ArrayList<DirectedGraph<Vertex<String>, Edge<String>>>();
		
		
		DirectedGraph<Vertex<String>, Edge<String>> graphA, graphB, graphC;
		graphA = new DirectedSparseMultigraph<Vertex<String>, Edge<String>>();
		graphB = new DirectedSparseMultigraph<Vertex<String>, Edge<String>>();
		graphC = new DirectedSparseMultigraph<Vertex<String>, Edge<String>>();

		Vertex<String> v1, v2, v3, v4;
		v1 = new Vertex<String>("a");
		v2 = new Vertex<String>("b");
		v3 = new Vertex<String>("c");
		v4 = new Vertex<String>("d");

		Edge<String> e1, e2, e3, e4;

		e1 = new Edge<String>("A");
		e2 = new Edge<String>("B");
		e3 = new Edge<String>("C");
		e4 = new Edge<String>("D");

		graphA.addVertex(v1);
		graphA.addVertex(v2);
		graphA.addVertex(v3);

		graphA.addEdge(e1, v1, v2, EdgeType.DIRECTED);
		graphA.addEdge(e2, v2, v3, EdgeType.DIRECTED);
		graphA.addEdge(e3, v3, v1, EdgeType.DIRECTED);

		v1 = new Vertex<String>("a");
		v2 = new Vertex<String>("b");
		v3 = new Vertex<String>("c");
		v4 = new Vertex<String>("a");

		e1 = new Edge<String>("A");
		e2 = new Edge<String>("B");
		e3 = new Edge<String>("C");
		e4 = new Edge<String>("A");

		graphB.addVertex(v1);
		graphB.addVertex(v2);
		graphB.addVertex(v3);
		graphB.addVertex(v4);

		graphB.addEdge(e1, v1, v2, EdgeType.DIRECTED);
		graphB.addEdge(e2, v2, v3, EdgeType.DIRECTED);
		graphB.addEdge(e3, v3, v1, EdgeType.DIRECTED);
		graphB.addEdge(e4, v1, v4, EdgeType.DIRECTED);

		v1 = new Vertex<String>("b");
		v2 = new Vertex<String>("b");
		v3 = new Vertex<String>("c");
		v4 = new Vertex<String>("b");

		e1 = new Edge<String>("E");
		e2 = new Edge<String>("E");
		e3 = new Edge<String>("C");
		e4 = new Edge<String>("A");

		graphC.addVertex(v1);
		graphC.addVertex(v2);
		graphC.addVertex(v3);
		graphC.addVertex(v4);

		graphC.addEdge(e1, v1, v2, EdgeType.DIRECTED);
		graphC.addEdge(e2, v2, v3, EdgeType.DIRECTED);
		graphC.addEdge(e3, v3, v1, EdgeType.DIRECTED);
		graphC.addEdge(e4, v1, v4, EdgeType.DIRECTED);

		graphs.add(graphA);
		graphs.add(graphB);		
		graphs.add(graphC);	
		
		System.out.println(graphA);
		System.out.println(GraphFactory.copyDirectedGraph(graphA));
		System.out.println(graphB);
		System.out.println(GraphFactory.copyDirectedGraph(graphB));
		System.out.println(graphC);
		System.out.println(GraphFactory.copyDirectedGraph(graphC));
		
	}		
}
