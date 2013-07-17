package org.data2semantics.tools.graphs;


import java.util.ArrayList;
import java.util.List;

import org.data2semantics.tools.rdf.RDFFileDataSet;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFFormat;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;

public class GraphFactoryTest {

	@Ignore
	public void test() {
		RDFFileDataSet testSetA = new RDFFileDataSet("D:\\workspaces\\datasets\\bible\\NTN-individuals.owl", RDFFormat.RDFXML);
		testSetA.addFile("D:\\workspaces\\datasets\\bible\\NTNames.owl", RDFFormat.RDFXML);
		
			//org.openrdf.model.Graph triples = testSet.getStatements(null, "http://swrc.ontoware.org/ontology#affiliation", null, true);
		List<Statement> triples = testSetA.getStatementsFromStrings("http://semanticbible.org/ns/2006/NTNames#Christianity", null,null, true);

		for (Statement triple : triples) {
			System.out.println(triple);
		}
		//System.out.println("Total humans: " + triples.size());
		
		
		List<Statement> triples2 = testSetA.getStatementsFromStrings(null, "http://semanticbible.org/ns/2006/NTNames#ethnicity", "http://semanticbible.org/ns/2006/NTNames#Jewish", true);			
		for (Statement triple2: triples2) {
			//System.out.println(triple2.getSubject() + " " + triple2.getPredicate() + " " + triple2.getObject());
			
			List<Statement> triples3 = testSetA.getStatementsFromStrings(triple2.getSubject().toString(), "http://semanticbible.org/ns/2006/NTNames#religiousBelief", "http://semanticbible.org/ns/2006/NTNames#Christianity", true);
			
			for (Statement triple3 : triples3) {
				System.out.println(triple3.getSubject() + " " + triple3.getPredicate() + " " + triple3.getObject());
			}
			
		}
		
		/*
		triples = testSetA.getStatementsFromStrings(null, "http://www.w3.org/1999/02/22-rdf-syntax-ns#type","http://semanticbible.org/ns/2006/NTNames#ReligiousBelief", true);

		for (Statement triple : triples) {
			System.out.println(triple.getSubject());
		}
		System.out.println("Total religions: " + triples.size());
		
		/*
		triples = testSetA.getStatementsFromStrings(null, "http://semanticbible.org/ns/2006/NTNames#religiousBelief", null, true);

		for (Statement triple : triples) {
			System.out.println(triple.getSubject() + " -> " + triple.getObject());
		}
		System.out.println("Total humans with religious belief: " + triples.size());
		*/
		
		/*
		triples = testSetA.getStatementsFromStrings(null, "http://semanticbible.org/ns/2006/NTNames#ethnicity", null, true);

		for (Statement triple : triples) {
			System.out.println(triple.getSubject() + " -> " + triple.getObject());
		}
		System.out.println("Total humans member of: " + triples.size());
		*/
		
		/*
		
		triples = testSetA.getStatementsFromStrings(null, null, "http://semanticbible.org/ns/2006/NTNames#GroupOfPeople", true);

		for (Statement triple : triples) {
			System.out.println(triple.getSubject() + " -> " + triple.getObject());
		}
		System.out.println("groups of people " + triples.size());
		
		/*
		triples = testSetA.getStatementsFromStrings(null, null, null, false);

		for (Statement triple : triples) {
			System.out.println(triple.getSubject() + " " + triple.getPredicate() + " " + triple.getObject());
		}
		System.out.println("groups of people " + triples.size());
		*/
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
