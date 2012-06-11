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

	@Ignore
	public void testRDFDataSet() {
		RDFDataSet testSet = new RDFFileDataSet("D:\\workspaces\\eclipse_workspace\\rdfgraphlearning\\src\\main\\resources\\aifb-fixed_complete.rdf", RDFFormat.RDFXML);

		List<Statement> triples = testSet.getStatementsFromStrings(null, "http://swrc.ontoware.org/ontology#affiliation", null, true);
		
		System.out.println("----- Triple test ----- #: " + triples.size());
		
		for (Statement triple : triples) {
			System.out.println(triple);
		}	
	}
	
	@Test
	public void testGetSubGraph() {
		RDFDataSet testSet = new RDFFileDataSet("D:\\workspaces\\eclipse_workspace\\rdfgraphlearning\\src\\main\\resources\\aifb-fixed_complete.rdf", RDFFormat.RDFXML);

		List<Statement> triples = testSet.getStatementsFromStrings(null, "http://swrc.ontoware.org/ontology#affiliation", null, true);
		List<Statement> triples2 = testSet.getSubGraph((URI) triples.get(0).getSubject(), 1, false);
		
		for (Statement triple : triples2) {
			System.out.println(triple);
		}		
	}

}
