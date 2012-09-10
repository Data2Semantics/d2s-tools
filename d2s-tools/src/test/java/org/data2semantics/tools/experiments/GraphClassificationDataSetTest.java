package org.data2semantics.tools.experiments;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.data2semantics.tools.rdf.RDFDataSet;
import org.data2semantics.tools.rdf.RDFFileDataSet;
import org.junit.Test;
import org.openrdf.rio.RDFFormat;

public class GraphClassificationDataSetTest {

	@Test
	public void test() {
		
		//RDFDataSet testSetA = new RDFFileDataSet("D:\\workspaces\\eclipse_workspace\\rdfgraphlearning\\src\\main\\resources\\aifb-fixed_complete.rdf", RDFFormat.RDFXML);
		//RDFDataSet testSetB = new RDFFileDataSet("D:\\workspaces\\eclipse_workspace\\rdfgraphlearning\\src\\main\\resources\\aifb-fixed_no_schema.n3", RDFFormat.N3);
		
		RDFDataSet testSetC = new RDFFileDataSet("D:\\workspaces\\datasets\\eswc-2012-complete.rdf", RDFFormat.RDFXML);
		
		
		List<String> bl = new ArrayList<String>();
		bl.add("http://swrc.ontoware.org/ontology#affiliation");
		bl.add("http://swrc.ontoware.org/ontology#employs");
		bl.add("http://data.semanticweb.org/ns/swc/ontology#holdsRole");
		bl.add("http://data.semanticweb.org/ns/swc/ontology#heldBy");
		
				
		System.out.println(DataSetFactory.createClassificationDataSet(testSetC, "http://data.semanticweb.org/ns/swc/ontology#holdsRole", "http://data.semanticweb.org/conference/eswc/2012/research-track-committee-member", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://xmlns.com/foaf/0.1/Person", bl, 2, false, false).getLabel());
	}

}
