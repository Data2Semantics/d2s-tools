package org.data2semantics.tools.graphs;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.data2semantics.tools.rdf.RDFSingleDataSet;
import org.data2semantics.tools.rdf.RDFFileDataSet;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFFormat;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;

public class GraphsTest
{

	@Ignore
	public void test()
	{
		File file = new File("/Users/Peter/Documents/datasets/rdf/aifb/aifb-fixed_complete.owl");

		DirectedGraph<Vertex<String>, Edge<String>> jungGraph = Graphs.graphFromRDF(file);
		
		jungGraph = Graphs.graphFromRDF(file, RDFFormat.RDFXML, null, Arrays.asList(".*ontology#year.*"));
		
		for(Edge<String> edge : jungGraph.getEdges())
			System.out.println(edge);
	}

	@Test
	public void testSubGraph()
	{
		Graph<String, String> graph = new UndirectedSparseGraph<String, String>();
		
		graph.addEdge("0", "a", "b");
		graph.addEdge("1", "b", "c");
		graph.addEdge("2", "c", "d");
		graph.addEdge("3", "d", "e");
		graph.addEdge("4", "e", "a");
		
		graph.addEdge("5", "a", "f");
		graph.addEdge("6", "f", "g");
		graph.addEdge("7", "g", "h");
		graph.addEdge("8", "h", "i");
		graph.addEdge("9", "i", "j");
		graph.addEdge("10", "j", "k");
		
		
		Graph<String, String> sub = Graphs.undirectedSubgraph(graph, Arrays.asList("a", "b", "c", "k", "j", "e"));
		System.out.println(sub);
	}
	
	@Test
	public void testCC()
	{
		Graph<String, String> graph = new UndirectedSparseGraph<String, String>();
		
		graph.addEdge("0", "a", "b");
		graph.addEdge("1", "b", "c");
		graph.addEdge("2", "c", "d");
		graph.addEdge("3", "d", "a");
		graph.addEdge("4", "a", "c");
		

		System.out.println(Graphs.clusteringCoefficient(graph));
	}	
	
}
