package org.data2semantics.tools.graphs;


import static org.junit.Assert.*;
import java.util.List;

import org.junit.Test;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFFormat;

import org.data2semantics.tools.rdf.*;

import edu.uci.ics.jung.graph.DirectedGraph;

public class GraphFactoryTest {

	@Test
	public void test() {
		RDFDataSet testSet = new RDFFileDataSet("D:\\workspaces\\eclipse_workspace\\rdfgraphlearning\\src\\main\\resources\\aifb-fixed_complete.rdf", RDFFormat.RDFXML);
		//org.openrdf.model.Graph triples = testSet.getStatements(null, "http://swrc.ontoware.org/ontology#affiliation", null, true);
		List<Statement> triples = testSet.getFullGraph();
			
		DirectedGraph<Vertex<String>, Edge<String>> jungGraph = GraphFactory.createDirectedGraph(triples);
		
		for (Edge<String> edge: jungGraph.getEdges()) {
			System.out.println(jungGraph.getEndpoints(edge).getFirst() + " " + edge + " " + jungGraph.getEndpoints(edge).getSecond());
		}
	}
	
	
}
