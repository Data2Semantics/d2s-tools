package org.data2semantics.tools.experiments;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.data2semantics.tools.graphs.Edge;
import org.data2semantics.tools.graphs.Vertex;
import org.data2semantics.tools.rdf.RDFDataSet;
import org.data2semantics.tools.rdf.RDFFileDataSet;
import org.junit.Test;
import org.openrdf.rio.RDFFormat;

import edu.uci.ics.jung.graph.DirectedGraph;

public class GraphClassificationDataSetTest {

	@Test
	public void test() {
		
		RDFDataSet testSetA = new RDFFileDataSet("D:\\workspaces\\datasets\\aifb\\aifb-fixed_complete.rdf", RDFFormat.RDFXML);
		//RDFDataSet testSetB = new RDFFileDataSet("D:\\workspaces\\eclipse_workspace\\rdfgraphlearning\\src\\main\\resources\\aifb-fixed_no_schema.n3", RDFFormat.N3);
		
		RDFDataSet testSetC = new RDFFileDataSet("D:\\workspaces\\datasets\\eswc-2012-complete.rdf", RDFFormat.RDFXML);	
		
		PropertyPredictionDataSet dataset = DataSetFactory.createPropertyPredictionDataSet(testSetA, "http://swrc.ontoware.org/ontology#affiliation", "http://swrc.ontoware.org/ontology#employs", 3, false, false);
		//PropertyPredictionDataSet dataset = DataSetFactory.createPropertyPredictionDataSet(testSetC, "http://data.semanticweb.org/ns/swc/ontology#holdsRole", "http://data.semanticweb.org/ns/swc/ontology#heldBy", "http://data.semanticweb.org/conference/eswc/2012/research-track-committee-member", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://xmlns.com/foaf/0.1/Person", 2, false, false);
		
		for (DirectedGraph<Vertex<String>, Edge<String>> graph : dataset.getGraphs()) {
			for (Vertex<String> vertex : graph.getVertices()) {
				if (vertex.getLabel().startsWith("\"")) {
					System.out.println(vertex.getLabel());
				}
			}
		}
		
		
				
		//System.out.println(DataSetFactory.createPropertyPredictionDataSet(testSetC, "http://data.semanticweb.org/ns/swc/ontology#holdsRole", "http://data.semanticweb.org/ns/swc/ontology#heldBy", "http://data.semanticweb.org/conference/eswc/2012/research-track-committee-member", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://xmlns.com/foaf/0.1/Person", 2, false, false).getLabel());
	}

}
