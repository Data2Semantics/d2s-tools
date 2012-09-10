package org.data2semantics.tools.rdf;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.rio.RDFFormat;

public class RDFDataSetTest {

	@Test
	public void testRDFDataSet() {
		RDFDataSet testSet = new RDFFileDataSet("D:\\workspaces\\datasets\\eswc-2012-complete.rdf", RDFFormat.RDFXML);

		
		
		//List<Statement> triples = testSet.getStatementsFromStrings(null, "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://xmlns.com/foaf/0.1/Person", true);
		List<Statement> triples = testSet.getStatementsFromStrings("http://data.semanticweb.org/conference/eswc/2012/research-track-committee-member", "http://data.semanticweb.org/ns/swc/ontology#heldBy", null, false);
		//List<Statement> triples = testSet.getStatementsFromStrings(null, "http://swrc.ontoware.org/ontology#affiliation", null, true);
		
		System.out.println("----- Triple test ----- #: " + triples.size());
		
		for (Statement triple : triples) {
			System.out.println(triple);
		}	
	}
	
	@Ignore
	public void testGetSubGraph() {
		RDFDataSet testSet = new RDFFileDataSet("D:\\workspaces\\eclipse_workspace\\rdfgraphlearning\\src\\main\\resources\\aifb-fixed_complete.rdf", RDFFormat.RDFXML);

		List<Statement> triples = testSet.getStatementsFromStrings(null, "http://swrc.ontoware.org/ontology#affiliation", null, true);
		List<Statement> triples2 = testSet.getSubGraph((URI) triples.get(0).getSubject(), 1, false);
		
		for (Statement triple : triples2) {
			System.out.println(triple);
		}		
	}

}
