package org.data2semantics.tools.graphs;

import java.io.File;
import java.util.List;

import org.data2semantics.tools.rdf.RDFDataSet;
import org.data2semantics.tools.rdf.RDFFileDataSet;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFFormat;

import edu.uci.ics.jung.graph.DirectedGraph;

public class Graphs
{
	/**
	 * Retrieves a graph from an rdf file, interpreting resources as nodes and 
	 * predicates as labeled edges. The property of RDF that resources can occur
	 * as predicates and vice versa is not reflected in the graph in any 
	 * structured way. 
	 * 
	 * The file format is assumed to be RDF XML
	 * @param rdfFile The file (in RDF XML) from which to read the graph.
	 * @return A directed multigraph with labeled nodes and edges
	 */
	public static DirectedGraph<Vertex<String>, Edge<String>> graphFromRDF(File rdfFile)
	{
		return graphFromRDF(rdfFile, RDFFormat.RDFXML);
	}
	
	/**
	 * Retrieves a graph from an rdf file
	 * @param rdfFile The file (in RDF XML) from which to read the graph.
	 * @param format
	 * @return A directed multigraph with labeled nodes and edges
	 */
	public static DirectedGraph<Vertex<String>, Edge<String>> graphFromRDF(File rdfFile, RDFFormat format)
	{
		RDFDataSet testSet = new RDFFileDataSet(rdfFile, format);

		//org.openrdf.model.Graph triples = testSet.getStatements(null, "http://swrc.ontoware.org/ontology#affiliation", null, true);
		List<Statement> triples = testSet.getFullGraph();
			
		DirectedGraph<Vertex<String>, Edge<String>> jungGraph = GraphFactory.createJUNGGraph(triples);
				
		return jungGraph;
	}
}
