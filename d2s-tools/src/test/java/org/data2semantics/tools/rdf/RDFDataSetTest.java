package org.data2semantics.tools.rdf;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFFormat;

public class RDFDataSetTest {

	@Test
	public void test() {
		RDFFileDataSet testSet = new RDFFileDataSet("D:\\workspaces\\eclipse_workspace\\rdfgraphlearning\\src\\main\\resources\\aifb-fixed_complete.rdf", RDFFormat.RDFXML);

		List<Statement> triples = testSet.getInstanceURIs("http://swrc.ontoware.org/ontology#affiliation", null);
		
		System.out.println("----- Triple test ----- #: " + triples.size());
		
		for (Statement triple : triples) {
			System.out.println("\n"+ triple.getSubject().stringValue() + " - " + triple.getPredicate().stringValue() + " - " + triple.getObject().stringValue());
			
			if (triple.getObject() instanceof Resource) {
				List<Statement> triples2 = testSet.getStatements((Resource) triple.getObject(), false);
				
				System.out.println("------------ Triples from the triple test ----------- #1");
				for (Statement triple2 : triples2) {
					System.out.println(triple2.getSubject().stringValue() + " - " + triple2.getPredicate().stringValue() + " - " + triple2.getObject().stringValue());
				}
				
				triples2 = testSet.getStatements((Resource) triple.getObject(), true);
				
				System.out.println("------------ Triples from the triple test ----------- #2");
				for (Statement triple2 : triples2) {
					System.out.println(triple2.getSubject().stringValue() + " - " + triple2.getPredicate().stringValue() + " - " + triple2.getObject().stringValue());
				}
			}
		}	
	}

}
